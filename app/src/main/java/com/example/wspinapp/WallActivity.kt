package com.example.wspinapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import coil.load
class WallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wall)

        setImageView()
    }

    private fun setImageView() {
        val imageUrl = intent.getStringExtra(IMAGE_URL_MESSAGE)
        val imageView = findViewById<ImageView>(R.id.wall_image)
        if (imageUrl != null) {
            if (imageUrl.startsWith("https://res.cloudinary.com")) {
                imageView.load(imageUrl)
            }
        }
//        val overlay = CircleOverlayView(this)
//        overlay.setImageView(imageView)
//        addContentView(overlay, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }
}
