package com.example.wspinapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import coil.load
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.HoldType
import com.example.wspinapp.model.Route
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.sqrt

class AddRouteActivity : AppCompatActivity() {
    var wallID: UInt = 0u
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_route)

        wallID = intent.getStringExtra(WALL_ID_MESSAGE)!!.toUInt()
        val wall: Wall = walls[wallID]!!

        setImageView(wall.ImageUrl)

        findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setHolds(wall.Holds)
    }

    private fun setImageView(imageUrl: String?) {
        val imageView = findViewById<ImageView>(R.id.add_route_wall_image)
        if (imageUrl != null) {
            imageView.load(imageUrl)
        }
    }

    fun submitRoute(view: View) {
        val holds: MutableMap<Hold, HoldType> =
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).getHolds()

        var routeHolds = emptyArray<Hold>()
        var startHolds = emptyArray<Hold>()
        var topHold = emptyArray<Hold>()

        holds.forEach {
            when (it.value) {
                HoldType.WALL_HOLD -> {}
                HoldType.HOLD -> routeHolds += it.key
                HoldType.START_HOLD -> startHolds += it.key
                HoldType.TOP_HOLD -> topHold += it.key
            }
        }

        runBlocking {
            backendClient.addRoute(
                Route(
                    Holds = routeHolds,
                    StartHolds = startHolds,
                    TopHold = topHold,
                    WallID = wallID
                )
            )
        }

        finish()

    }
}


// TODO maybe this should be implemented using ScaleGestureDetector somehow?
class AddHoldsOverlayView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val circleDrawer = CircleDrawer(context, alpha = 30)
    private val holdsDrawer = CircleDrawer(context, R.color.rock_sunny)
    private val startHoldsDrawer = CircleDrawer(context, R.color.horizon_pink)
    private val topHoldsDrawer = CircleDrawer(context, R.color.horizon_green)

    private var holds : MutableMap<Hold, HoldType> = mutableMapOf()
    private var touchX : Float = 0f
    private var touchY : Float = 0f

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)
    // other constructors can be added here as well, depending on your requirements

    private fun getClickedHolds(): List<Hold> {
        val clicked = mutableListOf<Hold>()
        for (circle in holds) {
            if (pointInCircle(touchX, touchY, circle.key)) {
                clicked.add(circle.key)
            }
        }
        return clicked
    }

    private fun pointInCircle(x: Float, y: Float, circle: Hold) : Boolean {
        val distX = abs(circle.X - x)
        val distY = abs(circle.Y - y)

        return sqrt(distX * distX + distY * distY) <= circle.Size
    }


    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // we register coordinates of last touch
        touchX = event.x
        touchY = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            val clickedHolds = getClickedHolds()
            clickedHolds.forEach { hold ->
                when (holds[hold]) {
                    HoldType.WALL_HOLD -> holds[hold] = HoldType.HOLD
                    HoldType.HOLD -> holds[hold] = HoldType.START_HOLD
                    HoldType.START_HOLD -> holds[hold] = HoldType.TOP_HOLD
                    HoldType.TOP_HOLD -> holds[hold] = HoldType.WALL_HOLD
                    else -> {} // shouldn't happen TODO
                }
            }
        }
        println("ABC")
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        holds.forEach { holdEntry ->
            val hold: Hold = holdEntry.key
            when (holdEntry.value) {
                HoldType.WALL_HOLD -> circleDrawer.drawCircle(canvas, hold.X, hold.Y, hold.Size)
                HoldType.HOLD -> holdsDrawer.drawCircle(canvas, hold.X, hold.Y, hold.Size)
                HoldType.START_HOLD -> startHoldsDrawer.drawCircle(canvas, hold.X, hold.Y, hold.Size)
                HoldType.TOP_HOLD -> topHoldsDrawer.drawCircle(canvas, hold.X, hold.Y, hold.Size)
            }
        }

    }

    fun setHolds(wallHolds: Array<Hold>) {
        wallHolds.forEach {
            this.holds[it] = HoldType.WALL_HOLD
        }
    }

    fun getHolds() : MutableMap<Hold, HoldType> {
        return holds
    }
}






