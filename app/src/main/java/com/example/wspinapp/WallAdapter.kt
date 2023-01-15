package com.example.wspinapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.wspinapp.model.Wall
import kotlinx.coroutines.CoroutineScope


val imageUrls: MutableMap<UInt, String> = mutableMapOf()

class WallAdapter(
    private val context: CoroutineScope,
    private val dataset: List<Wall>
) : RecyclerView.Adapter<WallAdapter.WallViewHolder>() {

    class WallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wallView: ConstraintLayout = view.findViewById(R.id.wall_preview)
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


        if (item.Image.startsWith("https://res.cloudinary.com")) {
            holder.wallView.findViewById<ImageView>(R.id.wall_image).load(item.Image)
        }
        imageUrls[item.ID] = item.Image
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}