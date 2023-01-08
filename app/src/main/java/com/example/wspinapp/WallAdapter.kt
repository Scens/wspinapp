package com.example.wspinapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wspinapp.model.Wall
import kotlinx.coroutines.CoroutineScope

class WallAdapter(
    private val context: CoroutineScope,
    private val dataset: List<Wall>
    ) : RecyclerView.Adapter<WallAdapter.WallViewHolder>() {

    class WallViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.wall_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_wall, parent, false)
        
        return WallViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: WallViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView.text = item.CreatedAt
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}