package com.example.wspinapp

import android.content.Intent
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
const val IMAGE_URL_MESSAGE = "com.example.wspinapp.IMAGE_URL"


class ImagesActivity : AppCompatActivity() {
    private var imageUris: MutableMap<Int, Uri> = mutableMapOf()
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
        val uri = getUri()
        findViewById<ImageView>(R.id.imageView).setImageURI(uri)
        imageUris[R.id.imageView] = uri
    }

    fun uploadImage(view: View) {
        uploadImageLauncher.launch(imageFolder)
    }

    fun takePicture(view: View) {
        takePictureLauncher.launch(getUri())
    }

    fun popUpImage(view: View) {
        val stringUri = imageUris[view.id].toString()
        val intent = Intent(this, PopUpImageActivity::class.java).putExtra(IMAGE_URL_MESSAGE, stringUri)
        startActivity(intent)
    }

    private fun getUri(): Uri {
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "current_picture.jpeg"
        )

        return FileProvider.getUriForFile(this, "com.example.wspinapp", file)
    }
}