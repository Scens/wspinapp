package com.example.wspinapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.launch
import kotlin.random.Random

suspend fun <T> Context.executeWithHandling(function: suspend () -> T?): T? {
    return try {
        function()
    } catch (e: Exception) {
        Toast.makeText(this, "An unknown error occurred: ${e::class.simpleName}", Toast.LENGTH_LONG).show()
        Log.println(Log.ERROR, "application", e.stackTraceToString())
        null
    }
}



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showWalls(view: View) {
        val activity = this
        lifecycleScope.launch {
            activity.executeWithHandling {
                val dataset = backendClient.fetchWalls() // fetchWalls is a suspend function
                if (dataset == null) {
                    Toast.makeText(
                        activity,
                        "Connection error. Try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val intent = Intent(activity, WallsActivity::class.java)
                    intent.putParcelableArrayListExtra("dataset", ArrayList(dataset))
                    startActivity(intent)
                }
            }

        }
    }


    fun changeBackgroundColor(view: View) {
        val logoColors = resources.getIntArray(R.array.logo_colors)
        val randomLogoColor = logoColors[Random.nextInt(logoColors.size)]
        view.setBackgroundColor(randomLogoColor)
    }
}
