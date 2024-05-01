package com.example.wspinapp.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.wspinapp.R
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.HoldShape


data class HoldSpecification (
    var size: Float = 0f,
    var angle: Float = 0f,
    var shape: String = HoldShape.CIRCLE.str,
)

class HoldDrawer(context: Context, color: Int = R.color.white, alpha: Int = 255) {
    private val paint: Paint = Paint()
    private var circleDrawer: CircleDrawer

    init {
        paint.color = ContextCompat.getColor(context, color)
        paint.alpha = alpha
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        circleDrawer = CircleDrawer(context, paint)
    }

    /*
    TODO
        var triangleDrawer = TriangleDrawer(context)
        var rectangleDrawer = RectangleDrawer(context)

        also all triangle, circle and rectangle drawers should implement an interface draw
     */

    fun draw(hold: Hold, canvas: Canvas) {
        val cx = hold.X * canvas.width
        val cy = hold.Y * canvas.height
        val size = hold.Size * canvas.width

        assert(hold.Shape.lowercase() == HoldShape.CIRCLE.str) { hold.Shape } // for now we can only add circle but it will change in future
        circleDrawer.drawCircle(canvas, cx, cy, size)
    }

    fun draw(state: HoldDrawingState, canvas: Canvas) {
        assert(state.holdShape == HoldShape.CIRCLE) { state.holdShape }
        circleDrawer.drawCircle(canvas, state.touchX, state.touchY, state.draggedCircleRadius) // why statet.draggedCircleRadius? this is not clear at all
    }

    fun draw(holdSpecification: HoldSpecification, x: Float, y: Float, canvas: Canvas) {
        assert(holdSpecification.shape == HoldShape.CIRCLE.str) // for now we can only add circle but it will change in future
        circleDrawer.drawCircle(canvas, x, y, holdSpecification.size)
    }
}


class CircleDrawer(context: Context, private val paint: Paint) {
    fun drawCircle(canvas: Canvas?, x: Float, y: Float, radius: Float) {
        canvas?.drawCircle(x, y, radius, paint)
    }

}