package com.example.wspinapp.utils

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wspinapp.R
import com.example.wspinapp.WallManager
import com.example.wspinapp.WallsActivity
import com.example.wspinapp.executeWithHandling
import kotlinx.coroutines.launch

class FetchWallsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.request_in_progress)

        val activity = this
        lifecycleScope.launch {
            activity.executeWithHandling {
                WallManager.fetchWalls() // fetchWalls is a suspend function
            }
            finish()


            val intent = Intent(activity, WallsActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }
    }

}