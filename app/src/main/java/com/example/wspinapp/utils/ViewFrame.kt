package com.example.wspinapp.utils

import android.widget.ImageView


/**
 * Class that represents what current frame of [ImageView] is visible.
 */
class ViewFrame(
    var minX: Float,
    var minY: Float,
    var maxX: Float,
    var maxY: Float,
    var scaleFactor: Float
) {
    companion object {
        fun from(imageView: ImageView?): ViewFrame{
            if (imageView == null) {
                return ViewFrame(0f, 0f, 0f, 0f, 0f)
            }

            val scaleFactor = imageView.scaleX // scaleY should be same
            val width = imageView.width
            val height = imageView.height

            val l = imageView.pivotX * (scaleFactor - 1f) / scaleFactor
            val t = imageView.pivotY * (scaleFactor - 1f) / scaleFactor
            return ViewFrame(
                minX = l,
                maxX = l + width / scaleFactor,
                minY = t,
                maxY = t + height / scaleFactor,
                scaleFactor = scaleFactor
            )
        }
    }

    /** Transforms any point to corresponding point within ImageView based on ViewFrame **/
    fun xToImageView(x: Float): Float {
        return x / scaleFactor + minX
    }

    fun yToImageView(y: Float): Float {
        return y / scaleFactor + minY
    }

    /** Transforms any point from ImageView to corresponding point on screen based on ViewFrame **/
    fun xToScreen(x: Float): Float {
        return (x - minX) * scaleFactor
    }

    fun yToScreen(y: Float): Float {
        return (y - minY) * scaleFactor
    }
}