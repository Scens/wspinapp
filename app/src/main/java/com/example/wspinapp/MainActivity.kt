package com.example.wspinapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File


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
        Log.println(Log.INFO, "take_picture", "saved correctly:$it")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when the user taps the Send button */
    fun sendMessage(@Suppress("UNUSED_PARAMETER") view: View) {
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

    fun watchVideo(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, WatchVideoActivity::class.java)
        startActivity(intent)
    }

    fun uploadImage(@Suppress("UNUSED_PARAMETER") view: View) {
        uploadImageLauncher.launch(imageFolderPattern)
    }

    fun takePicture(@Suppress("UNUSED_PARAMETER") view: View) {
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "current_picture.jpeg"
        )

        val uri = FileProvider.getUriForFile(this, "com.example.wspinapp", file)
        imageUri = uri

        takePictureLauncher.launch(uri)
    }
}
