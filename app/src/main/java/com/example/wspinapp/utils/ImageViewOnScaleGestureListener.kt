package com.example.wspinapp.utils

import android.view.ScaleGestureDetector
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min


// TODO maybe this is better:https://developer.android.com/develop/ui/views/touch-and-input/gestures/scroll
/**
 * This class is responsible for scaling of provided [ImageView].
 * It is assumed that all detector focus points are performed inside this [ImageView].
 *
 * Outside tools/functionalities can figure out current view frame using [ViewFrame] class.
 */
class ImageViewOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    lateinit var imageView: ImageView
    private var pivotX2 = 0f
    private var pivotY2 = 0f
    private var pivotX1 = 0f
    private var pivotY1 = 0f
    private var scaleFactor2 = 1f
    private var scaleFactor1 = 1f

    fun init(imageView: ImageView) {
        this.imageView = imageView
    }

    /**
     * We want current [ViewFrame] to be always within [ImageView] bounds.
      */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        // performing scaling
        scaleFactor2 *= detector.scaleFactor
        scaleFactor2 = min(scaleFactor2, 4f)
        scaleFactor2 = max(1f, scaleFactor2)

        val denominator = scaleFactor2 - 1f

        if (denominator < 0.00001) {
            imageView.pivotX = imageView.width / 2f
            imageView.pivotY = imageView.height / 2f
            scaleFactor2 = 1f
        } else {
            imageView.pivotX =
                (scaleFactor1 * pivotX1 - pivotX1 + scaleFactor2 * pivotX2 - scaleFactor1 * pivotX2) / denominator
            imageView.pivotY =
                (scaleFactor1 * pivotY1 - pivotY1 + scaleFactor2 * pivotY2 - scaleFactor1 * pivotY2) / denominator
        }

        // adjust pivot so that we don't go out of frame - math could fail then
        imageView.pivotX = min(imageView.pivotX, imageView.width.toFloat())
        imageView.pivotX = max(imageView.pivotX, 0f)
        imageView.pivotY = min(imageView.pivotY, imageView.height.toFloat())
        imageView.pivotY = max(imageView.pivotY, 0f)


        imageView.scaleX = scaleFactor2
        imageView.scaleY = scaleFactor2

        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        pivotX1 = imageView.pivotX
        pivotY1 = imageView.pivotY
        scaleFactor1 = scaleFactor2

        pivotX2 = (pivotX1 * (scaleFactor1 - 1) + detector.focusX) / scaleFactor1
        pivotY2 = (pivotY1 * (scaleFactor1 - 1) + detector.focusY) / scaleFactor1

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }
}