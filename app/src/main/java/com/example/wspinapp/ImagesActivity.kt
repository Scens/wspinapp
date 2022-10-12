package com.example.wspinapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

const val imageFolder = "image/*"


class ImagesActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private val uploadImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            Log.println(Log.INFO, "take_picture", "saved correctly:$it")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        setImageView()
    }

    private fun setImageView() {
        findViewById<ImageView>(R.id.imageView).setImageURI(getUri())
    }

    fun uploadImage(view: View) {
        uploadImageLauncher.launch(imageFolder)
    }

    fun takePicture(view: View) {
        takePictureLauncher.launch(getUri())
    }

    private fun getUri(): Uri {
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "current_picture.jpeg"
        )

        return FileProvider.getUriForFile(this, "com.example.wspinapp", file)
    }
}