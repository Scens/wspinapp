package com.example.wspinapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// TODO Refactor so that it's easier to understand what's going on in here
class AddWallActivity : AppCompatActivity() {
    private var circleRadius = 50f
    private var listenerScale = false
    private var imageDealer = ImageDealer(this)

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

        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        seekBar.max = (maxValue - minValue).toInt()
        seekBar.progress = (circleRadius - minValue).toInt()
        val circleView = findViewById<CircleView>(R.id.circle_view)
        circleView.setHoldSize(circleRadius)
        val circleOverlayView = findViewById<CircleOverlayView>(R.id.holds_canvas)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                circleRadius = minValue + progress
                circleView.setHoldSize(circleRadius)
                circleOverlayView.setCircleRadius(circleRadius)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun submitWall(view: View) {
        findViewById<Button>(R.id.create_wall).isEnabled = false
        Log.println(Log.DEBUG, " submit wall ", "submitting wall")
        val holds = findViewById<CircleOverlayView>(R.id.holds_canvas).getHolds()
        val wallId: UInt
        var wall: Wall
        runBlocking {
            wall = backendClient.addWall(Wall(holds.toTypedArray()))
        }
        wallId = wall.ID!!

        if (wallId == 0u) {
            // TODO handle error and present it to user
            finish()
            return
        }

        GlobalScope.launch {
            wall.ImageUrl = imageDealer.uploadCompressedImage(wallId)
        }

        runBlocking {
            wall.ImagePreviewUrl = imageDealer.uploadCompressedImagePreview(wallId) // this weights up to 10kb so it should be fairly fast
            Log.println(Log.INFO, "add_wall_activity", "imagePreviewUrl = ${wall.ImagePreviewUrl}")

        }

        dataset.add(wall)
        invalid = true
        finish() // probably need to do sth else though :)

    }

    fun switchListener(view: View) {
        listenerScale = !listenerScale
        findViewById<CircleOverlayView>(R.id.holds_canvas).listenGesturesMode = listenerScale
    }
}

class CircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val holdDrawer = HoldDrawer(context)
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

// TODO maybe this should be implemented using ScaleGestureDetector somehow?
class CircleOverlayView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var listenGesturesMode = false
    private var wallOnScaleGestureListener = WallOnScaleGestureListener()
    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context,
        wallOnScaleGestureListener
    )
    private var holdJuggler: HoldJuggler = HoldJuggler(context)
    private var frame: ViewFrame? = null

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    fun init(imageView: ImageView) {
        wallOnScaleGestureListener.init(imageView)
    }

    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (listenGesturesMode) {
            if (scaleGestureDetector.onTouchEvent(event)) {
                frame = wallOnScaleGestureListener.frame
                invalidate()
            }
        } else {
            Log.println(Log.DEBUG, "circle-overlay-view", "x:${event.x}, y:${event.y}")

            val acted = holdJuggler.onTouchEvent(event, frame!!)
            if (acted) {
                invalidate()
            }
        }
        Log.println(Log.DEBUG, "debug-frame", "xmin:${frame?.minX}, xmax:${frame?.maxX}\n" +
                "ymin:${frame?.minY}, ymax:${frame?.maxY}")

        val imageView = wallOnScaleGestureListener.imageView
        val scale = wallOnScaleGestureListener.scaleFactor2


        val minX = imageView.pivotX - imageView.width / (2 * scale)
        val maxX = imageView.pivotX + imageView.width / (2 * scale)
        val minY = imageView.pivotY - imageView.height / (2 * scale)
        val maxY = imageView.pivotY + imageView.height / (2 * scale)
        Log.println(Log.DEBUG, "debug-image", "xmin:${minX}, xmax:${maxX}\n" +
                "ymin:${minY}, ymax:${maxY}")
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frame == null) { // dirty hack this should be done in slightly different way
            frame = ViewFrame(0f, 0f, this.width.toFloat(), this.height.toFloat(), 1f)
        }
        holdJuggler.onDraw(canvas, frame!!)
    }

    fun setCircleRadius(circleRadius: Float) {
        holdJuggler.setCircleRadius(circleRadius)
    }

    fun getHolds() : List<Hold> {
        return holdJuggler.getHolds()
    }
}






