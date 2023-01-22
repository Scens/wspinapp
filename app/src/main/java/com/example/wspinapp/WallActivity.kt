package com.example.wspinapp

import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.Route
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

const val WALL_ID_MESSAGE = "com.example.wspinapp.WALL_ID"

class WallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wall)

        val wallID: UInt = intent.getStringExtra(WALL_ID_MESSAGE)!!.toUInt()
        val wall: Wall = walls[wallID]!!

        setImageView(wall.ImageUrl)

        val recyclerView = findViewById<RecyclerView>(R.id.routes_recycler_view)

        runBlocking {
            val routes = backendClient.fetchRoutes(wallID)
            recyclerView.adapter = RouteAdapter(this, routes)
        }

        setHoldOverlay(wall.Holds) // this is not that obvious actually
    }

    private fun setImageView(imageUrl: String?) {
        val imageView = findViewById<ImageView>(R.id.wall_image)
        if (imageUrl != null) {
            imageView.load(imageUrl)
        }
    }

    private fun setHoldOverlay(holds: Array<Hold>) {
        val overlay = findViewById<HoldsOverlay>(R.id.wall_holds_canvas)
        overlay.setHolds(holds)
    }
}

class HoldsOverlay constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val circleDrawer = CircleDrawer(context)

    private var circles : Array<Hold> = emptyArray()

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        circles.forEach {
            circleDrawer.drawCircle(canvas, it.X, it.Y, it.Size)
        }
    }

    fun setHolds(holds: Array<Hold>) {
        circles = holds
    }
}

val routes: MutableMap<UInt, Route> = mutableMapOf()

class RouteAdapter(
    private val context: CoroutineScope,
    private val dataset: List<Route>
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeView: ConstraintLayout = view.findViewById(R.id.route_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.route_preview, parent, false)

        return RouteViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val item = dataset[position]

        val routeGrade: TextView = holder.routeView.getViewById(R.id.grade) as TextView
        routeGrade.text = item.ID.toString()
        routes[item.ID!!] = item
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
