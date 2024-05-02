package com.example.wspinapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wspinapp.utils.FetchWallsActivity
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
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

    override fun onResume() {
        super.onResume()

        findViewById<Button>(R.id.main_button).isEnabled = true
    }

    fun showWalls(view: View) {
        findViewById<Button>(R.id.main_button).isEnabled = false
        val fetchWallIntent = Intent(this, FetchWallsActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(fetchWallIntent)
    }


    fun changeBackgroundColor(view: View) {
        val logoColors = resources.getIntArray(R.array.logo_colors)
        val randomLogoColor = logoColors[Random.nextInt(logoColors.size)]
        view.setBackgroundColor(randomLogoColor)
    }
}
