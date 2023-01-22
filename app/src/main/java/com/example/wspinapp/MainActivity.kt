package com.example.wspinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showWalls(view: View) {
        val intent = Intent(this, WallsActivity::class.java)
        startActivity(intent)
    }

    fun changeBackgroundColor(view: View) {
        val logoColors = resources.getIntArray(R.array.logo_colors)
        val randomLogoColor = logoColors[Random.nextInt(logoColors.size)]
        view.setBackgroundColor(randomLogoColor)
    }
}
