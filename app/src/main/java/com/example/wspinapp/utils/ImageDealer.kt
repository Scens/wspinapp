package com.example.wspinapp.utils

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

const val IMAGE_PATH = "image.png"
const val COMPRESSED_IMAGE_PATH = "compressed_image.png"
const val COMPRESSED_IMAGE_PREVIEW_FILE = "compressed_image_preview.png"

class ImageDealer(private val activity: AppCompatActivity) {
    private lateinit var imageFile: File
    private lateinit var compressedImageFile: File
    private lateinit var compressedImagePreviewFile: File

//    init {
//        imageFile = getImageFile(IMAGE_PATH)
//        compressedImageFile = getImageFile(COMPRESSED_IMAGE_PATH)
//        compressedImagePreviewFile = getImageFile(COMPRESSED_IMAGE_PREVIEW_FILE)
//    }

    @OptIn(DelicateCoroutinesApi::class)
    fun takePicture(imageView: ImageView) {

        imageFile = getImageFile(IMAGE_PATH)
        compressedImageFile = getImageFile(COMPRESSED_IMAGE_PATH)
        compressedImagePreviewFile = getImageFile(COMPRESSED_IMAGE_PREVIEW_FILE)

        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
            Log.println(Log.INFO, "take_picture", "saved correctly:$it")
            imageView.setImageURI(getUri(imageFile))
            GlobalScope.launch {
                compressImageFile()
                compressImagePreviewFile()

                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed image size ${compressedImageFile.length()}."
                )
                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed imagePreview size ${compressedImagePreviewFile.length()}."
                )


                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed image path ${compressedImageFile.path}"
                )
                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed imagePreview path ${compressedImagePreviewFile.path}"
                )

            }
        }.launch(getUri(imageFile))
    }

    private suspend fun compressImageFile() {
        Compressor.compress(activity, imageFile) {
            resolution(1280, 720)
            quality(80)
            format(Bitmap.CompressFormat.WEBP)
            size(500_000) // 0.5 MB
            default()
            destination(compressedImageFile)
        }
    }

    private suspend fun compressImagePreviewFile() {
        Compressor.compress(activity, imageFile) {
            resolution(128, 72)
            quality(40)
            format(Bitmap.CompressFormat.WEBP)
            size(10_000) // 10kb
            default()
            destination(compressedImagePreviewFile)
        }
    }

    suspend fun uploadCompressedImage(wallId: UInt) {
        // TODO if compressing didn't make it in time do sth
        backendClient.addImageToWall(wallId, compressedImageFile, "image")
    }

    suspend fun uploadCompressedImagePreview(wallId: UInt) {
        // TODO if compressing didn't make it in time do sth
        backendClient.addImageToWall(wallId, compressedImagePreviewFile, "imagepreview")
    }

    private fun getImageFile(path: String): File {
        return File(
            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            path
        )
    }

    private fun getUri(file: File): Uri {
        return FileProvider.getUriForFile(activity, "com.example.wspinapp", file)
    }
}
