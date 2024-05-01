package com.example.wspinapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// TODO Refactor so that it's easier to understand what's going on in here
class AddWallActivity : AppCompatActivity() {
    private var circleRadius = 50f
    private var listenerScale = false
    private var imageDealer = ImageDealer(this)
    private var prevView: View? = null
    private var holds: List<Hold>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_wall)

        setImageView()
        setSeekBar()
    }

    private fun setImageView() {
        val imageView = findViewById<ImageView>(R.id.add_wall_image)
        imageDealer.takePicture(imageView)
        val overlay = findViewById<CircleOverlayView>(R.id.holds_canvas)
        overlay.init(imageView)
        overlay.setCircleRadius(circleRadius)
    }


    private fun setSeekBar() {
        val minValue = 10f
        val maxValue = 200f

        val verticalSeekBar = findViewById<VerticalSeekBar>(R.id.seek_bar)

        verticalSeekBar.max = (maxValue - minValue).toInt()
        verticalSeekBar.progress = (circleRadius - minValue).toInt()
        val circleView = findViewById<CircleView>(R.id.circle_view)
        circleView.setHoldSize(circleRadius)
        val circleOverlayView = findViewById<CircleOverlayView>(R.id.holds_canvas)
        verticalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                circleRadius = minValue + progress
                circleView.setHoldSize(circleRadius)
                circleOverlayView.setCircleRadius(circleRadius)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    // Define a custom exception
    class WallCreationException : Exception("Failed to create the wall in the backend.")

    @SuppressLint("MissingInflatedId")
    fun next(view: View) {
        holds = findViewById<CircleOverlayView>(R.id.holds_canvas).getHolds()

        if (prevView != null) {
            val viewToSet = prevView
            prevView = findViewById(R.id.add_wall_layout)
            setContentView(viewToSet)
        } else {
            prevView = findViewById(R.id.add_wall_layout)
            setContentView(R.layout.submit_wall)
        }
    }

    @SuppressLint("MissingInflatedId")
    fun back(view: View) {
        val viewToSet = prevView
        prevView = findViewById(R.id.submit_wall_layout)

        setContentView(viewToSet)
    }


    fun submitWall(view: View) {
        findViewById<Button>(R.id.submit_button).isEnabled = false
        Log.println(Log.DEBUG, "submit wall", "submitting wall")

        this.executeNonSuspendWithHandling {
            val wallId: UInt
            var wall: Wall
            runBlocking {
                wall = backendClient.addWall(Wall(holds!!.toTypedArray()))
            }
            wallId = wall.ID!!

            if (wallId == 0u) {
                // Throw the custom exception
                throw WallCreationException()
            }

            lifecycleScope.launch {
                wall.ImageUrl = imageDealer.uploadCompressedImage(wallId)
            }

            runBlocking {
                wall.ImagePreviewUrl = imageDealer.uploadCompressedImagePreview(wallId)
                Log.println(
                    Log.INFO,
                    "add_wall_activity",
                    "imagePreviewUrl = ${wall.ImagePreviewUrl}"
                )
            }

            WallManager.addWall(wall)
        }
        finish()
    }

    fun switchListener(view: View) {
        listenerScale = !listenerScale
        findViewById<CircleOverlayView>(R.id.holds_canvas).listenGesturesMode = listenerScale

        // This can probably be done better using some kind of ScrollerImageView or sth but this seems ok.
        if (listenerScale) {
            findViewById<ImageView>(R.id.current_mode).setImageResource(R.drawable.control_image_dark)
            findViewById<ImageView>(R.id.control_image).setImageResource(R.drawable.control_image_dark)
            findViewById<ImageView>(R.id.pick_image).setImageResource(R.drawable.pick_image_light)
        } else {
            findViewById<ImageView>(R.id.current_mode).setImageResource(R.drawable.pick_image_dark)
            findViewById<ImageView>(R.id.control_image).setImageResource(R.drawable.control_image_light)
            findViewById<ImageView>(R.id.pick_image).setImageResource(R.drawable.pick_image_dark)
        }
    }
}

class CircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val holdDrawer = HoldDrawer(context, color = androidx.appcompat.R.color.material_grey_800)
    private var hold = HoldSpecification()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        holdDrawer.draw(hold, width / 2f, height / 2f, canvas!!)
    }

    fun setHoldSize(size: Float) {
        hold.size = size
        invalidate()
    }
}

class CircleOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var imageView: ImageView? = null
    var listenGesturesMode = false
    private var imageViewOnScaleGestureListener = ImageViewOnScaleGestureListener()
    private var imageViewOnGestureListener = ImageViewOnGestureListener()
    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(
        context, imageViewOnScaleGestureListener
    )
    private var gestureDetector: GestureDetector = GestureDetector(
        context, imageViewOnGestureListener
    )
    private var holdJuggler: HoldJuggler = HoldJuggler(context)

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    fun init(imageView: ImageView) {
        imageViewOnScaleGestureListener.init(imageView)
        imageViewOnGestureListener.init(imageView)

        scaleGestureDetector.isStylusScaleEnabled = false
        scaleGestureDetector.isQuickScaleEnabled = false

        this.imageView = imageView
    }

    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (listenGesturesMode) {
            var res = scaleGestureDetector.onTouchEvent(event)
            if (!scaleGestureDetector.isInProgress) {
                res = gestureDetector.onTouchEvent(event)
            }
            if (res) {
                invalidate()
            }
        } else {
            if (holdJuggler.onTouchEvent(event, ViewFrame.from(imageView))) {
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        holdJuggler.onDraw(canvas, ViewFrame.from(imageView))
    }

    fun setCircleRadius(circleRadius: Float) {
        holdJuggler.setCircleRadius(circleRadius)
    }

    fun getHolds(): List<Hold> {
        return holdJuggler.getHolds()
    }
}






