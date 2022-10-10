package com.example.wspinapp

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisplayMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_message)

        setTextView()
        setImageView()
    }

    private fun setTextView() {
        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        // Capture the layout's TextView and set the string as its text
        findViewById<TextView>(R.id.textView).apply {
            text = message
        }
    }

    private fun setImageView() {
        val imageUri: String? = intent.getStringExtra(IMAGE_URI)
        if (imageUri?.isBlank() == false) {
            findViewById<ImageView>(R.id.imageView3).setImageURI(Uri.parse(imageUri))
        }
    }
}