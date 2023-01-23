package com.example.wspinapp

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.GridLayoutManager
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
    var wallID: UInt = 0u
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wall)


        wallID = intent.getStringExtra(WALL_ID_MESSAGE)!!.toUInt()
        val wall: Wall = walls[wallID]!!

        setImageView(wall.ImageUrl)

        val recyclerView = findViewById<RecyclerView>(R.id.routes_recycler_view)
        val gridLayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = gridLayoutManager

        runBlocking {
            val routes = backendClient.fetchRoutes(wallID)
            recyclerView.adapter = RouteAdapter(this, routes)
        }

        setHoldOverlay(wall.Holds)
    }

    private fun setImageView(imageUrl: String?) {
        val imageView = findViewById<ImageView>(R.id.wall_image)
        if (imageUrl != null) {
            imageView.load(imageUrl)
        }
    }

    private fun setHoldOverlay(holds: Array<Hold>) {
        val overlay = findViewById<HoldsOverlay>(R.id.wall_holds_canvas)
        overlay.setWallHolds(holds)
    }

    fun pickRoute(view: View) {
        val routeId = view.tag
        val route : Route = routes[routeId]!!
        val overlay = findViewById<HoldsOverlay>(R.id.wall_holds_canvas)

        overlay.setHolds(route.Holds)
        overlay.setStartHolds(route.StartHolds)
        overlay.setTopHolds(route.TopHold)
        overlay.invalidate()
    }

    fun unselectRoute(view: View) {
        val overlay = findViewById<HoldsOverlay>(R.id.wall_holds_canvas)
        overlay.resetHolds()
        overlay.invalidate()
    }

    fun addRoute(view: View) {
        val intent =
            Intent(this, AddRouteActivity::class.java).putExtra(WALL_ID_MESSAGE, wallID.toString())
        startActivity(intent)
    }
}

class HoldsOverlay constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val circleDrawer = CircleDrawer(context, alpha = 15)
    private val holdsDrawer = CircleDrawer(context, R.color.rock_sunny)
    private val startHoldsDrawer = CircleDrawer(context, R.color.horizon_pink)
    private val topHoldsDrawer = CircleDrawer(context, R.color.horizon_green)


    private var wallHolds : Array<Hold> = emptyArray()
    private var holds : Array<Hold> = emptyArray()
    private var startHolds : Array<Hold> = emptyArray()
    private var topHolds : Array<Hold> = emptyArray()

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        wallHolds.forEach {
            circleDrawer.drawCircle(canvas, it.X, it.Y, it.Size)
        }
        holds.forEach {
            holdsDrawer.drawCircle(canvas, it.X, it.Y, it.Size)
        }
        startHolds.forEach {
            startHoldsDrawer.drawCircle(canvas, it.X, it.Y, it.Size)
        }
        topHolds.forEach {
            topHoldsDrawer.drawCircle(canvas, it.X, it.Y, it.Size)
        }
    }

    fun setWallHolds(wallHolds: Array<Hold>) {
        this.wallHolds = wallHolds
    }

    fun setHolds(holds: Array<Hold>) {
        this.holds = holds
    }
    fun setStartHolds(startHolds: Array<Hold>) {
        this.startHolds = startHolds
    }
    fun setTopHolds(topHolds: Array<Hold>) {
        this.topHolds = topHolds
    }

    fun resetHolds() {
        holds = emptyArray()
        startHolds = emptyArray()
        topHolds = emptyArray()
    }
}

val routes: MutableMap<UInt, Route> = mutableMapOf()

class RouteAdapter(
    private val context: CoroutineScope,
    private val dataset: List<Route>
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeView: ConstraintLayout = view.findViewById(R.id.route_preview)

        fun setTag(tag: UInt) {
            routeView.tag = tag
        }
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
        holder.setTag(item.ID)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
