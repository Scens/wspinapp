package com.example.wspinapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

var invalid = false


// TODO I have a feeling logic below can be faster - there are probably some tools that can be easily used here
class WallsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walls)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        runBlocking {
            val walls = backendClient.fetchWalls()
            recyclerView.adapter = WallAdapter(this, walls)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!invalid) {
            return
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        runBlocking {
            val walls = backendClient.fetchWalls()
            recyclerView.adapter = WallAdapter(this, walls)
        }
    }

    fun openWall(view: View) {
        val wallId = view.tag
        val intent = Intent(this, WallActivity::class.java).putExtra(WALL_ID_MESSAGE, wallId.toString())
        startActivity(intent)
    }

    fun addWall(view: View) {
        val intent = Intent(this, AddWallActivity::class.java)
        startActivity(intent)
    }
}

val walls: MutableMap<UInt, Wall> = mutableMapOf()

class WallAdapter(
    private val context: CoroutineScope,
    private val dataset: List<Wall>
) : RecyclerView.Adapter<WallAdapter.WallViewHolder>() {

    class WallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wallView: ConstraintLayout = view.findViewById(R.id.wall_preview)

        fun setTag(tag: UInt) {
            wallView.tag = tag
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.wall_preview, parent, false)

        return WallViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: WallViewHolder, position: Int) {
        val item = dataset[position]
        val textView: TextView = holder.wallView.getViewById(R.id.wall_id) as TextView
        textView.text = item.ID.toString()

        if (item.ImageUrl?.isBlank() == false) {
            holder.wallView.findViewById<ImageView>(R.id.wall_image).load(item.ImageUrl)
        }
        walls[item.ID!!] = item
        holder.setTag(item.ID)

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}