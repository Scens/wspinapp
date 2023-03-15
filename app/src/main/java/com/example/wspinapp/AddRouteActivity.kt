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
import com.example.wspinapp.utils.HoldPicker
import com.example.wspinapp.utils.ViewFrame
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.runBlocking

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
            val route = backendClient.addRoute(
                Route(
                    Holds = routeHolds,
                    StartHolds = startHolds,
                    TopHold = topHold,
                    WallID = wallID
                )
            )
                ?: // TODO HANDLE ERROR
                return@runBlocking

            routes_dataset.add(route)
            wall_invalid = true
        }

        finish()

    }
}


// TODO maybe this should be implemented using ScaleGestureDetector somehow?
class AddHoldsOverlayView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val holdPicker = HoldPicker(context)
    private var frame: ViewFrame? = null

    init {
        // any initialization code here
    }

    constructor(context: Context) : this(context, null)


    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (holdPicker.onTouchEvent(event, frame!!)) {
            invalidate()
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frame == null) { // dirty hack this should be done in slightly different way
            frame = ViewFrame(0f, 0f, this.width.toFloat(), this.height.toFloat(), 1f)
        }
        holdPicker.onDraw(canvas, frame!!)

    }

    fun setHolds(wallHolds: Array<Hold>) {
        holdPicker.setHolds(wallHolds)
    }

    fun getHolds() : MutableMap<Hold, HoldType> {
        return holdPicker.getHolds()
    }
}
