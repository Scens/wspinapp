package com.example.wspinapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri

import android.widget.VideoView

class WatchVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_video)

        val uri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
        findViewById<VideoView>(R.id.videoView).apply {
            setVideoURI(uri)
            start()
        }
    }
}