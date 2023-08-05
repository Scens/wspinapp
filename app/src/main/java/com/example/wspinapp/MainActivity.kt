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
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.launch
import java.net.ConnectException
import kotlin.random.Random

// Exception handling function
private fun Context.handleException(e: Exception) {
    when(e) {
        is HttpRequestTimeoutException,
        is ConnectTimeoutException,
        is SocketTimeoutException,
        is ConnectException -> {
            Toast.makeText(
                this,
                "Connection error. Try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
        else -> {
            Toast.makeText(
                this,
                "An unknown error occurred: ${e::class.simpleName}",
                Toast.LENGTH_LONG
            ).show()
            Log.println(Log.ERROR, "application", e.stackTraceToString())
        }
    }
}

// Suspend function version
suspend fun <T> Context.executeWithHandling(function: suspend () -> T?): T? {
    return try {
        function()
    } catch (e: Exception) {
        handleException(e)
        null
    }
}

// Non-suspend function version
fun Context.executeNonSuspendWithHandling(action: () -> Unit) {
    try {
        action()
    } catch (e: Exception) {
        handleException(e)
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
                WallManager.fetchWalls() // fetchWalls is a suspend function
                val intent = Intent(activity, WallsActivity::class.java)
                startActivity(intent)
            }

        }
    }


    fun changeBackgroundColor(view: View) {
        val logoColors = resources.getIntArray(R.array.logo_colors)
        val randomLogoColor = logoColors[Random.nextInt(logoColors.size)]
        view.setBackgroundColor(randomLogoColor)
    }
}
