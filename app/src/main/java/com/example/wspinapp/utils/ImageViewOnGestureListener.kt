package com.example.wspinapp.utils

import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min

// TODO maybe this is better:https://developer.android.com/develop/ui/views/touch-and-input/gestures/scroll
class ImageViewOnGestureListener : GestureDetector.SimpleOnGestureListener() {
    lateinit var imageView: ImageView
    fun init(imageView: ImageView) {
        this.imageView = imageView
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // TODO math needed to be done here :)
        imageView.pivotX += distanceX
        imageView.pivotY += distanceY

        // adjust pivot so that we don't go out of frame - math could fail then
        imageView.pivotX = min(imageView.pivotX, imageView.width.toFloat())
        imageView.pivotX = max(imageView.pivotX, 0f)
        imageView.pivotY = min(imageView.pivotY, imageView.height.toFloat())
        imageView.pivotY = max(imageView.pivotY, 0f)

        return true
    }
}