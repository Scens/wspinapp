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
    }

    private fun setSeekBar() {
        val minValue = 10f
        val maxValue = 200f

        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        seekBar.max = (maxValue - minValue).toInt()
        seekBar.progress = (circleRadius - minValue).toInt()
        val circleView = findViewById<CircleView>(R.id.circle_view)
        circleView.setCircleRadius(circleRadius)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                circleRadius = minValue + progress
                circleView.setCircleRadius(circleRadius)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}


class CircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var circleRadius: Float = 0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas?.drawCircle(centerX, centerY, circleRadius, paint)
    }

    fun setCircleRadius(circleRadius: Float) {
        this.circleRadius = circleRadius
        invalidate()
    }
}




class CircleOverlayView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var touchX = 0f
    private var touchY = 0f
    private val circles = mutableListOf<Pair<Float, Float>>()

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    private fun coordinatesInsideImageView(x: Float, y: Float, r: Float) : Boolean {
        return x >= r && x + r <= this.width && y >= r && y + r <= this.height
    }

    private fun pointInCircle(x: Float, y: Float, circle: Pair<Float, Float>) : Boolean {
        val x_diff = abs(circle.first - x)
        val y_diff = abs(circle.second - y)

        return sqrt(x_diff * x_diff + y_diff * y_diff) <= 50f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchX = event.x
        touchY = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            val toRemove = mutableListOf<Pair<Float, Float>>()
            for (circle in circles) {
                if (pointInCircle(touchX, touchY, circle)) {
                    toRemove.add(circle)
                }
            }
            circles.removeAll(toRemove) // actually would be better to only remove one circle - the one that is closest to center
        }

        if (event.action == MotionEvent.ACTION_UP) {
            if (coordinatesInsideImageView(touchX, touchY, 50f)) {
                circles.add(Pair(touchX,touchY))
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        circles.forEach {
            canvas.drawCircle(it.first, it.second, 50f, paint)
        }
        if (coordinatesInsideImageView(touchX, touchY, 50f))
            canvas.drawCircle(touchX, touchY, 50f, paint)

    }


}






