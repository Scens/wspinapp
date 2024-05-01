package com.example.wspinapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import com.example.wspinapp.R
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.HoldType
import kotlin.math.abs
import kotlin.math.sqrt

class HoldPicker(context: Context) {
    private val defaultDrawer = HoldDrawer(context, alpha = 30)
    private val holdsDrawer = HoldDrawer(context, R.color.rock_sunny)
    private val startHoldsDrawer = HoldDrawer(context, R.color.horizon_pink)
    private val topHoldsDrawer = HoldDrawer(context, R.color.horizon_green)

    private var holds : MutableMap<Hold, HoldType> = mutableMapOf()
    private var touchX : Float = 0f
    private var touchY : Float = 0f

    private fun getClickedHolds(frame: ViewFrame): List<Hold> {
        val clicked = mutableListOf<Hold>()
        for (circle in holds) {
            if (pointInCircle(touchX, touchY, circle.key, frame)) {
                clicked.add(circle.key)
            }
        }
        return clicked
    }

    private fun pointInCircle(x: Float, y: Float, circle: Hold, frame: ViewFrame) : Boolean {
        val distX = abs(circle.getAbsoluteX(frame) - x)
        val distY = abs(circle.getAbsoluteY(frame) - y)

        return sqrt(distX * distX + distY * distY) <= circle.getAbsoluteSize(frame)
    }


    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    fun onTouchEvent(event: MotionEvent, frame: ViewFrame, selectedHoldType: HoldType): Boolean {
        // we register coordinates of last touch
        touchX = event.x
        touchY = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            val clickedHolds = getClickedHolds(frame)
            clickedHolds.forEach { hold ->
                when (holds[hold]) {
                    selectedHoldType -> holds[hold] = HoldType.WALL_HOLD
                    else -> {
                        holds[hold] = selectedHoldType
                    }
                }
            }
        }
        return true
    }

    fun onDraw(canvas: Canvas, frame: ViewFrame) {
        holds.forEach { holdEntry ->
            val hold: Hold = holdEntry.key
            when (holdEntry.value) {
                HoldType.WALL_HOLD -> defaultDrawer.draw(hold, canvas)
                HoldType.HOLD -> holdsDrawer.draw(hold, canvas)
                HoldType.START_HOLD -> startHoldsDrawer.draw(hold, canvas)
                HoldType.TOP_HOLD -> topHoldsDrawer.draw(hold, canvas)
            }
        }

    }

    fun setHolds(wallHolds: Array<Hold>) {
        wallHolds.forEach {
            this.holds[it] = HoldType.WALL_HOLD
        }
    }

    fun getHolds() : MutableMap<Hold, HoldType> {
        return holds
    }
}