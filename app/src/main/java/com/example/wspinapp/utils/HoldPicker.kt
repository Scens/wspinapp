package com.example.wspinapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
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

    private fun getClickedHolds(): List<Hold> {
        val clicked = mutableListOf<Hold>()
        for (circle in holds) {
            if (pointInCircle(touchX, touchY, circle.key)) {
                clicked.add(circle.key)
            }
        }
        return clicked
    }

    private fun pointInCircle(x: Float, y: Float, circle: Hold) : Boolean {
        val distX = abs(circle.X - x)
        val distY = abs(circle.Y - y)

        return sqrt(distX * distX + distY * distY) <= circle.Size
    }


    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    fun onTouchEvent(event: MotionEvent, frame: ViewFrame): Boolean {
        // we register coordinates of last touch
        touchX = event.x
        touchY = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            val clickedHolds = getClickedHolds()
            clickedHolds.forEach { hold ->
                when (holds[hold]) {
                    HoldType.WALL_HOLD -> holds[hold] = HoldType.HOLD
                    HoldType.HOLD -> holds[hold] = HoldType.START_HOLD
                    HoldType.START_HOLD -> holds[hold] = HoldType.TOP_HOLD
                    HoldType.TOP_HOLD -> holds[hold] = HoldType.WALL_HOLD
                    else -> {} // shouldn't happen TODO
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