package com.example.wspinapp.utils

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.HoldShape
import kotlin.math.abs
import kotlin.math.sqrt

class HoldDrawingState {
    var touchX: Float = 0f
    var touchY: Float = 0f
    var circleRadius: Float = 0f // set by seekBar todo should be settings
    var angle: Float = 0f // todo should be settings as well
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

    private fun pointInHold(x: Float, y: Float, circle: Hold, frame: ViewFrame): Boolean {
        val distX = abs(circle.getAbsoluteX(frame) - x)
        val distY = abs(circle.getAbsoluteY(frame) - y)

        return sqrt(distX * distX + distY * distY) <= circle.Size
    }

    // we're either dragging an existing circle or creating a new circle with current radius
    private fun getCircleRadius(frame: ViewFrame): Float {
        val toRemove = mutableListOf<Hold>()
        for (hold in holds) {
            assert(hold.Shape == HoldShape.CIRCLE.str) // TODO more hold shapes in future
            if (pointInHold(state.touchX, state.touchY, hold, frame)) {
                toRemove.add(hold) // todo don't remove all holds just the one with mid closes to point
            }
        }
        holds.removeAll(toRemove) // actually would be better to only remove one circle - the one that is closest to center
        return if (toRemove.size > 0) {
            toRemove[0].Size // for now simply take first one
        } else {
            state.circleRadius / frame.scaleFactor
        }
    }

    fun onTouchEvent(event: MotionEvent, frame: ViewFrame): Boolean { // most likely some kind of limits are needed here
        state.touchX = frame.xToImageView(event.x)
        state.touchY = frame.yToImageView(event.y)


        if (event.action == MotionEvent.ACTION_DOWN) {
            dragging = true
            state.draggedCircleRadius = getCircleRadius(frame)
        }
        if (event.action == MotionEvent.ACTION_UP && dragging) {
            if (holdInsideViewFrame(frame)) {
                holds.add(
                    Hold(
                        X = state.touchX / frame.parentWidth,
                        Y = state.touchY / frame.parentHeight,
                        Size = state.draggedCircleRadius / frame.parentWidth,
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
                // scale Hold
                val holdSpecification = HoldSpecification(
                    size = it.getAbsoluteSize(frame) * frame.scaleFactor,
                    angle = it.Angle,
                    shape = it.Shape,
                )
                val x = frame.xToScreen(it.getAbsoluteX(frame))
                val y = frame.yToScreen(it.getAbsoluteY(frame))
                holdDrawer.draw(holdSpecification, x, y, canvas)
            }
        }

        // drawing circle that is being dragged
        if (dragging && holdInsideViewFrame(frame)) {
            val holdSpecification = HoldSpecification(
                size = state.draggedCircleRadius * frame.scaleFactor,
                angle = state.angle,
                shape = state.holdShape.str,
            )
            val x = frame.xToScreen(state.touchX)
            val y = frame.yToScreen(state.touchY)
            holdDrawer.draw(holdSpecification, x, y, canvas)
        }
    }

    private fun insideFrame(hold: Hold, frame: ViewFrame): Boolean {
        val cx = hold.getAbsoluteX(frame)
        val cy = hold.getAbsoluteY(frame)
        val size = hold.getAbsoluteSize(frame)
        return frame.minX < cx + size && cx - size < frame.maxX && frame.minY < cy + size && cy - size < frame.maxY
    }

    fun setCircleRadius(circleRadius: Float) { // not really circle radius but size in general
        state.circleRadius = circleRadius
    }

    fun getHolds(): List<Hold> {
        return holds.toList()
    }
}
