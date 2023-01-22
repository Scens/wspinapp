package com.example.wspinapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

class AddWallActivity : AppCompatActivity() {
    private var circleRadius = 50f

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            Log.println(Log.INFO, "take_picture", "saved correctly:$it")
            val imageView = findViewById<ImageView>(R.id.add_wall_image)
            imageView.setImageURI(getUri())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_wall)

        setImageView()
        setSeekBar()
    }

    private fun setImageView() {
        takePictureLauncher.launch(getUri())
        val overlay = findViewById<CircleOverlayView>(R.id.holds_canvas)
        overlay.setCircleRadius(circleRadius)
    }

    private fun getUri(): Uri {
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "current_picture.jpeg"
        )

        return FileProvider.getUriForFile(this, "com.example.wspinapp", file)
    }

    private fun setSeekBar() {
        val minValue = 10f
        val maxValue = 200f

        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        seekBar.max = (maxValue - minValue).toInt()
        seekBar.progress = (circleRadius - minValue).toInt()
        val circleView = findViewById<CircleView>(R.id.circle_view)
        circleView.setCircleRadius(circleRadius)
        val circleOverlayView = findViewById<CircleOverlayView>(R.id.holds_canvas)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                circleRadius = minValue + progress
                circleView.setCircleRadius(circleRadius)
                circleOverlayView.setCircleRadius(circleRadius)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun submitWall(view: View) {
        Log.println(Log.DEBUG, " submit wall ", "submitting wall")
        val holds = findViewById<CircleOverlayView>(R.id.holds_canvas).getHolds()
        var wallId: UInt
        runBlocking {
            wallId = backendClient.addWall(Wall(holds.toTypedArray()))
        }

        runBlocking {
            backendClient.addImageToWall(wallId, File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "current_picture.jpeg"
            ))
        }

        // TODO instead of fetching walls again we can simply use the response and add it by hand here
        invalid = true
        finish() // probably need to do sth else though :)

    }
}


class CircleDrawer(context: Context) {
    private val paint: Paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context, R.color.yellow)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
    }

    fun drawCircle(canvas: Canvas?, x: Float, y: Float, radius: Float) {
        canvas?.drawCircle(x, y, radius, paint)
    }

}

class CircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var circleRadius: Float = 0f
    private val circleDrawer = CircleDrawer(context)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        circleDrawer.drawCircle(canvas,width / 2f, height / 2f, circleRadius)
    }

    fun setCircleRadius(circleRadius: Float) {
        this.circleRadius = circleRadius
        invalidate()
    }
}


// TODO maybe this should be implemented using ScaleGestureDetector somehow?
class CircleOverlayView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val circleDrawer = CircleDrawer(context)

    // STATE
    private var touchX = 0f
    private var touchY = 0f
    private var circleRadius: Float = 0f // set by seekBar

    private var draggedCircleRadius = 0f
    private var finishedDragging: Boolean = false

    private val circles = mutableListOf<Hold>()

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    private fun coordinatesInsideImageView(x: Float, y: Float) : Boolean {
        return x >= draggedCircleRadius && x + draggedCircleRadius <= this.width && y >= draggedCircleRadius && y + draggedCircleRadius <= this.height
    }

    private fun pointInCircle(x: Float, y: Float, circle: Hold) : Boolean {
        val distX = abs(circle.X - x)
        val distY = abs(circle.Y - y)

        return sqrt(distX * distX + distY * distY) <= circle.Size
    }

    fun setCircleRadius(circleRadius: Float) {
        this.circleRadius = circleRadius
    }


    // we're either dragging an existing circle or creating a new circle with current radius
    private fun getCircleRadius(): Float {
        val toRemove = mutableListOf<Hold>()
        for (circle in circles) {
            if (pointInCircle(touchX, touchY, circle)) {
                toRemove.add(circle)
            }
        }
        circles.removeAll(toRemove) // actually would be better to only remove one circle - the one that is closest to center
        return if (toRemove.size > 0) {
            toRemove[0].Size // for now simply take first one
        } else {
            circleRadius
        }
    }


    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // we register coordinates of last touch
        touchX = event.x
        touchY = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            draggedCircleRadius = getCircleRadius()
        }

        if (event.action == MotionEvent.ACTION_UP) {
            if (coordinatesInsideImageView(touchX, touchY)) {
                circles.add(
                    Hold(
                        X = touchX,
                        Y = touchY,
                        Size = draggedCircleRadius,
                        Shape = "Circle",
                        Angle = 0f
                    )
                )
            }
            finishedDragging = true
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        circles.forEach {
            circleDrawer.drawCircle(canvas, it.X, it.Y, it.Size)
        }

        // drawing circle that is being dragged
        if (coordinatesInsideImageView(touchX, touchY))
            if (finishedDragging) {
                finishedDragging = false
            } else {
                circleDrawer.drawCircle(canvas, touchX, touchY, draggedCircleRadius)
            }
    }

    fun getHolds() : List<Hold> {
        return circles.toList()
    }


}






