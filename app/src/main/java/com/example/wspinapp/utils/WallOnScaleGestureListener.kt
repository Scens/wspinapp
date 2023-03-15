package com.example.wspinapp.utils

import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min




class WallOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
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

    override fun onScale(detector: ScaleGestureDetector): Boolean {
//        Log.println(Log.DEBUG, "scale-detector", "pivotx:${imageView.pivotX}, pivoty:${imageView.pivotY}\n" +
//                "focusX: ${detector.focusX}, focusY: ${detector.focusY}\n" +
//                "imageWidth: ${imageView.width}, imageHeight: ${imageView.height}\n" +
//                ".... ${imageView.scaleType}\n" +
//                "{${imageView.translationX}, ${imageView.translationY}")
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

        // adjust pivot so that we dont go out of frame - math could fail then
        imageView.pivotX = min(imageView.pivotX, imageView.width.toFloat())
        imageView.pivotX = max(imageView.pivotX, 0f)
        imageView.pivotY = min(imageView.pivotY, imageView.height.toFloat())
        imageView.pivotY = max(imageView.pivotY, 0f)


        imageView.scaleX = scaleFactor2
        imageView.scaleY = scaleFactor2

        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        // what's the current frame?
//        frame = ViewFrame(
//            imageView.pivotX - imageView.pivotX / scaleFactor,
//            imageView.pivotY - imageView.pivotY / scaleFactor,
//            imageView.pivotX + (imageView.width - imageView.pivotX) / scaleFactor,
//            imageView.pivotY + (imageView.height - imageView.pivotY) / scaleFactor,
//            scaleFactor
//        )

        pivotX1 = imageView.pivotX
        pivotY1 = imageView.pivotY
        scaleFactor1 = scaleFactor2

        // bruh here I have to adjust focusX and focusY :)

        pivotX2 = (pivotX1 * (scaleFactor1 - 1) + detector.focusX) / scaleFactor1
        pivotY2 = (pivotY1 * (scaleFactor1 - 1) + detector.focusY) / scaleFactor1

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

//
//    private fun adjustFrame() {
//        val diffX = imageView.width / (2f * scaleFactor)
//        val diffY = imageView.height / (2f * scaleFactor)
//        frame = ViewFrame(
//            minX = imageView.pivotX - diffX,
//            maxX = imageView.pivotX + diffX,
//            minY = imageView.pivotY - diffY,
//            maxY = imageView.pivotY + diffY,
//            scaleFactor = scaleFactor,
//        )
//    }

}