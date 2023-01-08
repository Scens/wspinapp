package com.example.wspinapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView

class PopUpImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up_image)

        setImageView()
    }

    fun finish(view: View) {
        finish()
    }

    private fun setImageView() {
        val uri = Uri.parse(intent.getStringExtra(IMAGE_URL_MESSAGE))
        findViewById<ImageView>(R.id.popUpImageView).setImageURI(uri)
    }
}