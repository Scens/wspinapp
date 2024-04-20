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


class ImageDealer(private val activity: AppCompatActivity) {
    private var imageFile = SyncFile("image.jpg", activity)
    private var compressedImageFile = SyncFile("compressed_image.jpg", activity)
    private var compressedImagePreviewFile = SyncFile("compressed_image_preview.jpg", activity)

    @OptIn(DelicateCoroutinesApi::class)
    fun takePicture(imageView: ImageView) {
        imageFile.init()
        compressedImageFile.init()
        compressedImagePreviewFile.init()


        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
            Log.println(Log.INFO, "take_picture", "saved correctly:$it")
            imageView.setImageURI(getUri(imageFile.getFile()))
            GlobalScope.launch {
                compressedImageFile.compressFile(imageFile.getFile(), SyncFile.CompressionType.NORMAL)
                compressedImagePreviewFile.compressFile(imageFile.getFile(), SyncFile.CompressionType.HARD)

                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed image size ${compressedImageFile.getFile().length()}."
                )
                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed imagePreview size ${compressedImagePreviewFile.getFile().length()}."
                )


                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed image path ${compressedImageFile.getFile().path}"
                )
                Log.println(
                    Log.INFO,
                    "take_picture",
                    "Compressed imagePreview path ${compressedImagePreviewFile.getFile().path}"
                )

            }
        }.launch(getUri(imageFile.getFile()))
    }


    suspend fun uploadCompressedImage(wallId: UInt): String {
        return backendClient.addImageToWall(wallId, compressedImageFile.attainFile(), "image")
    }

    suspend fun uploadCompressedImagePreview(wallId: UInt): String {
        return backendClient.addImageToWall(wallId, compressedImagePreviewFile.attainFile(), "imagepreview")
    }

    private fun getUri(file: File): Uri {
        return FileProvider.getUriForFile(activity, "com.example.wspinapp", file)
    }
}

class SyncFile(private var path: String, private val activity: AppCompatActivity) {
    private var file: File? = null
    private var compressed: Boolean = false

    enum class CompressionType {
        HARD, NORMAL
    }

    fun init() {
        file = File(
            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            path
        )
    }

    fun getFile(): File {
        return file!!
    }

    fun attainFile(): File {
        synchronized(this) {
            while (!compressed) {
                Thread.sleep(1000)
            }

            compressed = false
            return file!!
        }
    }

    suspend fun compressFile(originalFile: File, compressionType: CompressionType) {
        if (compressionType == CompressionType.NORMAL) {
            Compressor.compress(activity, originalFile) {
                resolution(1280, 720)
                quality(80)
                format(Bitmap.CompressFormat.JPEG)
                size(500_000) // 0.5 MB
                default()
                destination(file!!)
            }
        } else if (compressionType == CompressionType.HARD) {
            Compressor.compress(activity, originalFile) {
                resolution(128, 72)
                quality(40)
                format(Bitmap.CompressFormat.JPEG)
                size(10_000) // 10kb
                default()
                destination(file!!)
            }
        }
        compressed = true
    }
}
