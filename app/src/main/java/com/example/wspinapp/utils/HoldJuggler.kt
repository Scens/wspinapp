package com.example.wspinapp.utils

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.HoldShape
import kotlin.math.abs
import kotlin.math.sqrt

data class ViewFrame(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
    val scaleFactor: Float,
)

class HoldDrawingState {
    var touchX: Float = 0f
    var touchY: Float = 0f
    var circleRadius: Float = 0f // set by seekBar todo should be settings
    var draggedCircleRadius: Float = 0f // todo should be settings/config as well
    var holdShape: HoldShape = HoldShape.CIRCLE
}


class HoldJuggler(context: Context) {
    private val holdDrawer = HoldDrawer(context)
    // STATE
    private val state: HoldDrawingState = HoldDrawingState()
    private var dragging: Boolean = false
    private val holds: MutableList<Hold> = mutableListOf()

    private fun holdInsideViewFrame(frame: ViewFrame) : Boolean {
        return state.touchX >= state.draggedCircleRadius &&
                state.touchX + state.draggedCircleRadius <= frame.maxX &&
                state.touchY >= state.draggedCircleRadius &&
                state.touchY + state.draggedCircleRadius <= frame.maxY
    }

    private fun pointInHold(x: Float, y: Float, circle: Hold) : Boolean {
        val distX = abs(circle.X - x)
        val distY = abs(circle.Y - y)

        return sqrt(distX * distX + distY * distY) <= circle.Size
    }

    // we're either dragging an existing circle or creating a new circle with current radius
    private fun getCircleRadius(): Float {
        val toRemove = mutableListOf<Hold>()
        for (hold in holds) {
            assert(hold.Shape == HoldShape.CIRCLE.str) // TODO more hold shapes in future
            if (pointInHold(state.touchX, state.touchY, hold)) {
                toRemove.add(hold) // todo don't remove all holds just the one with mid closes to point
            }
        }
        holds.removeAll(toRemove) // actually would be better to only remove one circle - the one that is closest to center
        return if (toRemove.size > 0) {
            toRemove[0].Size // for now simply take first one
        } else {
            state.circleRadius
        }
    }

    fun onTouchEvent(event: MotionEvent, frame: ViewFrame): Boolean { // most likely some kind of limits are needed here
        state.touchX = event.x
        state.touchY = event.y


        if (event.action == MotionEvent.ACTION_DOWN) {
            dragging = true
            state.draggedCircleRadius = getCircleRadius()
        }
        if (event.action == MotionEvent.ACTION_UP && dragging) {
            if (holdInsideViewFrame(frame)) {
                holds.add(
                    Hold(
                        X = state.touchX,
                        Y = state.touchY,
                        Size = state.draggedCircleRadius,
                        Shape = HoldShape.CIRCLE.str,
                        Angle = 0f
                    )
                ) // type of holds should depend on Selected configuration
            }
            dragging = false
        }

        return true // maybe for some event we dont want to invalidate canvas
    }

    fun onDraw(canvas: Canvas, frame: ViewFrame) {
        holds.forEach {
            if (insideFrame(it, frame)) {

                Log.println(Log.INFO, "IMPORTANTE", "from ${it.X};${it.Y}")
                // scale Hold
                val holdSpecification = HoldSpecification(
                    size = it.Size * frame.scaleFactor,
                    angle = it.Angle,
                    shape = it.Shape,
                )
                // hack
                val x = (it.X - frame.minX) * frame.scaleFactor
                val y = (it.Y - frame.minY) * frame.scaleFactor


                Log.println(Log.INFO, "IMPORTANTE", "to ${x};${y}")
//                , x: Float, y: Float
                // now our x and y XD

                holdDrawer.draw(holdSpecification, x, y, canvas)
            } // todo all that fcuking scaling ...
        }

        // drawing circle that is being dragged
        if (dragging && holdInsideViewFrame(frame)) {
            holdDrawer.draw(state, canvas)
        }

        // (846.0 - 455.731) / (1080.0 - 455.731)
        //
    }

    private fun insideFrame(hold: Hold, frame: ViewFrame) : Boolean {
        return frame.minX < hold.X && hold.X < frame.maxX && frame.minY < hold.Y && hold.Y < frame.maxY
    }

    fun setCircleRadius(circleRadius: Float) { // not really circle radius but size in general
        state.circleRadius = circleRadius
    }

    fun getHolds(): List<Hold> {
        return holds.toList()
    }
}
