package com.example.wspinapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.example.wspinapp.model.AddHold
import com.example.wspinapp.model.AddWall
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.sqrt

class AddWallActivity : AppCompatActivity() {
    private var circleRadius = 50f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_wall)

        setImageView()
        setSeekBar()
    }

    private fun setImageView() {
        val imageView = findViewById<ImageView>(R.id.add_wall_image)
        val overlay = findViewById<CircleOverlayView>(R.id.holds_canvas)
        overlay.setCircleRadius(circleRadius)
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
        val holds = findViewById<CircleOverlayView>(R.id.holds_canvas).getHolds()
        runBlocking {
            Datasource().addWall(AddWall(holds.toTypedArray()))
        }
        runBlocking {
            // update image here
        }
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

class CircleOverlayView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val circleDrawer = CircleDrawer(context)

    // STATE
    private var touchX = 0f
    private var touchY = 0f
    private var circleRadius: Float = 0f // set by seekBar

    private var draggedCircleRadius = 0f
    private var finishedDragging: Boolean = false

    private val circles = mutableListOf<AddHold>()

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    private fun coordinatesInsideImageView(x: Float, y: Float) : Boolean {
        return x >= circleRadius && x + circleRadius <= this.width && y >= circleRadius && y + circleRadius <= this.height
    }

    private fun pointInCircle(x: Float, y: Float, circle: AddHold) : Boolean {
        val distX = abs(circle.x - x)
        val distY = abs(circle.y - y)

        return sqrt(distX * distX + distY * distY) <= circle.radius
    }

    fun setCircleRadius(circleRadius: Float) {
        this.circleRadius = circleRadius
    }


    // we're either dragging an existing circle or creating a new circle with current radius
    private fun getCircleRadius(): Float {
        val toRemove = mutableListOf<AddHold>()
        for (circle in circles) {
            if (pointInCircle(touchX, touchY, circle)) {
                toRemove.add(circle)
            }
        }
        circles.removeAll(toRemove) // actually would be better to only remove one circle - the one that is closest to center
        return if (toRemove.size > 0) {
            toRemove[0].radius // for now simply take first one
        } else {
            circleRadius
        }
    }

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
                circles.add(AddHold(touchX, touchY, draggedCircleRadius))
            }
            finishedDragging = true
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        circles.forEach {
            circleDrawer.drawCircle(canvas, it.x, it.y, it.radius)
        }

        // drawing circle that is being dragged
        if (coordinatesInsideImageView(touchX, touchY))
            if (finishedDragging) {
                finishedDragging = false
            } else {
                circleDrawer.drawCircle(canvas, touchX, touchY, draggedCircleRadius)
            }
    }

    fun getHolds() : List<AddHold> {
        return circles.toList()
    }


}






