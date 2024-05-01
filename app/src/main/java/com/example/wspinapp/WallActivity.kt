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
import com.example.wspinapp.utils.CircleDrawer
import com.example.wspinapp.utils.HoldDrawer
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

const val WALL_ID_MESSAGE = "com.example.wspinapp.WALL_ID"
var wall_invalid: Boolean = false

class WallActivity : AppCompatActivity() {
    lateinit var routesRV: RecyclerView
    var wallID: UInt = 0u
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wall)

        routesRV = findViewById(R.id.routes_recycler_view)

        wallID = intent.getStringExtra(WALL_ID_MESSAGE)!!.toUInt()
        val wall: Wall = walls[wallID]!!

        setImageView(wall.ImageUrl)

        val gridLayoutManager = GridLayoutManager(this, 3)
        routesRV.layoutManager = gridLayoutManager

        runBlocking {
            // TODO if connection fails the exit gracefully instead of crashing app
            routes_dataset = backendClient.fetchRoutes(wallID)
            routesRV.adapter = RouteAdapter(this)
        }

        setHoldOverlay(wall.Holds)
    }

    override fun onResume() {
        super.onResume()
        if (!wall_invalid) {
            return
        }
        routesRV.adapter?.notifyDataSetChanged()
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
    private val circleDrawer = HoldDrawer(context, alpha = 80)
    private val holdsDrawer = HoldDrawer(context, R.color.rock_sunny)
    private val startHoldsDrawer = HoldDrawer(context, R.color.horizon_pink)
    private val topHoldsDrawer = HoldDrawer(context, R.color.horizon_green)


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
            circleDrawer.draw(it, canvas)
        }
        holds.forEach {
            holdsDrawer.draw(it, canvas)
        }
        startHolds.forEach {
            startHoldsDrawer.draw(it, canvas)
        }
        topHolds.forEach {
            topHoldsDrawer.draw(it, canvas)
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
var routes_dataset: MutableList<Route> = ArrayList()

class RouteAdapter(
    private val context: CoroutineScope
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
        val item = routes_dataset[position]

        val routeGrade: TextView = holder.routeView.getViewById(R.id.grade) as TextView
        routeGrade.text = item.ID.toString()
        routes[item.ID!!] = item
        holder.setTag(item.ID)
    }

    override fun getItemCount(): Int {
        return routes_dataset.size
    }
}
