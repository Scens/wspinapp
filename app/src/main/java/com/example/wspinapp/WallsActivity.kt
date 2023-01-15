package com.example.wspinapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking

class WallsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walls)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        runBlocking {
            val walls = Datasource().loadWalls()
            recyclerView.adapter = WallAdapter(this, walls)

            recyclerView.setHasFixedSize(true)
        }
    }

    fun openWall(view: View) {
        val wallId = view.findViewById<TextView>(R.id.wall_id).text.toString().toInt().toUInt()
        val imageUrl = imageUrls[wallId]
        val intent = Intent(this, WallActivity::class.java).putExtra(IMAGE_URL_MESSAGE, imageUrl)
        startActivity(intent)
    }

    fun addWall(view: View) {
        val intent = Intent(this, AddWallActivity::class.java)
        startActivity(intent)
    }
}