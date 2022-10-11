package com.example.wspinapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts

const val EXTRA_MESSAGE = "com.example.wspinapp.MESSAGE"
const val IMAGE_URI = "com.example.wspinapp.IMAGE_URI"

const val imageFolder = "image"
const val imageFolderPattern = "$imageFolder/*"

class MainActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private val uploadImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        TODO("how to get the reason for failing to save to be printed...")
        Log.println(Log.INFO, "", "saved correctly:$it")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        val editText = findViewById<EditText>(R.id.textBox)
        val message = editText.text.toString()
        val intent = Intent(this, DisplayMessageActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
            if (imageUri != null) {
                putExtra(IMAGE_URI, imageUri.toString())
            }
        }
        startActivity(intent)
    }

    fun watchVideo(view: View) {
        val intent = Intent(this, WatchVideoActivity::class.java)
        startActivity(intent)
    }

    fun uploadImage(view: View) {
        uploadImageLauncher.launch(imageFolderPattern)
    }

    fun takePicture(view: View) {
        TODO("make it work ffs")
        println(view.context.dataDir)
        val dir = view.context.dataDir
        val uri = Uri.parse("${dir}/wallImage.png")
        imageUri = uri // set imageUri here, so that when you click send uploaded image is already selected
        takePictureLauncher.launch(uri)
    }
}
