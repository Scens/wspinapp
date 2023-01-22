package com.example.wspinapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import coil.load

const val IMAGE_URL_MESSAGE = "com.example.wspinapp.IMAGE_URL"

class WallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wall)

        setImageView()
//        getWallAndDisplayHolds()
    }

    private fun setImageView() {
        val imageUrl = intent.getStringExtra(IMAGE_URL_MESSAGE)
        val imageView = findViewById<ImageView>(R.id.wall_image)
        if (imageUrl != null) {
            if (imageUrl.startsWith("https://res.cloudinary.com")) {
                imageView.load(imageUrl)
            }
        }
    }
}
