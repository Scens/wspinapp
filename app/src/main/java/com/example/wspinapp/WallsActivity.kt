package com.example.wspinapp

import android.os.Bundle
import android.util.Log
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
            Log.println(Log.DEBUG, "recycler_view ", recyclerView.toString())
            recyclerView.adapter = WallAdapter(this, walls)

            recyclerView.setHasFixedSize(true)
        }
    }
}