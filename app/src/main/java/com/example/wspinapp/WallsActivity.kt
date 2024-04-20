package com.example.wspinapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

var invalid = false


// TODO I have a feeling logic below can be faster - there are probably some tools that can be easily used here
class WallsActivity : AppCompatActivity() {    // on below line we are creating variables for
    // our swipe to refresh layout, recycler view, adapter and list.
    lateinit var wallsRV: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walls)
        wallsRV = findViewById(R.id.recycler_view)
        wallsRV.adapter = WallAdapter()

        setupItemTouchHelper()
    }

    private fun setupItemTouchHelper() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val activityContext = this@WallsActivity // Capture the activity's context

                lifecycleScope.launch {
                    activityContext.executeWithHandling {
                        val wallView: ConstraintLayout =
                            viewHolder.itemView.findViewById(R.id.wall_preview)
                        backendClient.deleteWall(wallView.tag as UInt)
                        (wallsRV.adapter as? WallAdapter)?.removeItem(position)
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(wallsRV)
    }

    override fun onResume() {
        super.onResume()
        if (WallManager.haveNewItems)
            wallsRV.adapter?.notifyDataSetChanged()
    }

    fun openWall(view: View) {
        val wallId = view.tag
        val intent =
            Intent(this, WallActivity::class.java).putExtra(WALL_ID_MESSAGE, wallId.toString())
        startActivity(intent)
    }

    fun addWall(view: View) {
        val intent = Intent(this, AddWallActivity::class.java)
        startActivity(intent)
    }
}

val walls: MutableMap<UInt, Wall> = mutableMapOf()

class WallAdapter : RecyclerView.Adapter<WallAdapter.WallViewHolder>() {

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
        val item = WallManager.dataset[position]
        val textView: TextView = holder.wallView.getViewById(R.id.wall_id) as TextView
        textView.text = item.ID.toString()

        Log.println(Log.INFO, "Adapter", "imagePreviewUrl is ${item.ImagePreviewUrl}")
        if (item.ImagePreviewUrl?.isBlank() == false) {
            holder.wallView.findViewById<ImageView>(R.id.wall_image).load(item.ImagePreviewUrl)
        }
        walls[item.ID!!] = item
        holder.setTag(item.ID)
    }

    fun removeItem(position: Int) {
        WallManager.dataset.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return WallManager.dataset.size
    }
}