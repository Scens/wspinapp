package com.example.wspinapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts

const val EXTRA_MESSAGE = "com.example.wspinapp.MESSAGE"
const val IMAGE_URI = "com.example.wspinapp.IMAGE_URI"

class MainActivity : AppCompatActivity() {
    private var imageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectButton = findViewById<Button>(R.id.button3)

        selectButton.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        println(imageUri)
        val editText = findViewById<EditText>(R.id.textBox)
        val message = editText.text.toString()
        val intent = Intent(this, DisplayMessageActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
            if (imageUri?.isNotEmpty() == true) {
                putExtra(IMAGE_URI, imageUri.toString())
            }
        }
        startActivity(intent)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri.toString()
    }

    fun watchVideo(view: View) {
        val intent = Intent(this, WatchVideoActivity::class.java)
        startActivity(intent)
    }
}
