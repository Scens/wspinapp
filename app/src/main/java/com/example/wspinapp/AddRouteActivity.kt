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
import android.widget.RadioButton
import androidx.core.content.ContextCompat
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
    private var wallID: UInt = 0u
    private var selectedRadioButton: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_route)

        findViewById<RadioButton>(R.id.top).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.horizon_green))
        findViewById<RadioButton>(R.id.regular).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.rock_sunny))
        findViewById<RadioButton>(R.id.start).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.horizon_pink))

        wallID = intent.getStringExtra(WALL_ID_MESSAGE)!!.toUInt()
        val wall: Wall = walls[wallID]!!

        setImageView(wall.ImageUrl)

        findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setHolds(wall.Holds)
        setRegularRadioButton(findViewById(R.id.regular))
    }

    private fun setImageView(imageUrl: String?) {
        val imageView = findViewById<ImageView>(R.id.add_route_wall_image)
        if (imageUrl != null) {
            imageView.load(imageUrl)
        }
    }

    fun setTopRadioButton(view: View) {
        val view: RadioButton = view as RadioButton
        if (selectedRadioButton == view.id) {
            selectedRadioButton = null
            view.isChecked = false
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setCurrentHoldType(HoldType.WALL_HOLD)
        }
        else {
            selectedRadioButton = view.id
            view.isChecked = true
            findViewById<RadioButton>(R.id.regular).isChecked = false
            findViewById<RadioButton>(R.id.start).isChecked = false
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setCurrentHoldType(HoldType.TOP_HOLD)
        }
    }

    fun setRegularRadioButton(view: View) {
        val view: RadioButton = view as RadioButton
        if (selectedRadioButton == view.id) {
            selectedRadioButton = null
            view.isChecked = false
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setCurrentHoldType(HoldType.WALL_HOLD)
        }
        else {
            selectedRadioButton = view.id
            view.isChecked = true
            findViewById<RadioButton>(R.id.top).isChecked = false
            findViewById<RadioButton>(R.id.start).isChecked = false
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setCurrentHoldType(HoldType.HOLD)
        }
    }

    fun setStartRadioButton(view: View) {
        val view: RadioButton = view as RadioButton
        if (selectedRadioButton == view.id) {
            selectedRadioButton = null
            view.isChecked = false
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setCurrentHoldType(HoldType.WALL_HOLD)
        }
        else {
            selectedRadioButton = view.id
            view.isChecked = true
            findViewById<RadioButton>(R.id.regular).isChecked = false  
            findViewById<RadioButton>(R.id.top).isChecked = false
            findViewById<AddHoldsOverlayView>(R.id.add_holds_canvas).setCurrentHoldType(HoldType.START_HOLD)

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
    private var currentHoldType: HoldType = HoldType.HOLD
    private var frame: ViewFrame? = null
    constructor(context: Context) : this(context, null)


    fun setCurrentHoldType(holdType: HoldType) {
        currentHoldType = holdType
    }

    // This warning says that we didn't override performClick method - this is a method that is helpful to people with impaired vision
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (holdPicker.onTouchEvent(event, frame!!, currentHoldType)) {
            invalidate()
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frame == null) { // dirty hack this should be done in slightly different way
            frame = ViewFrame(0f, 0f, this.width.toFloat(), this.height.toFloat(), 1f, this.measuredWidth, this.measuredHeight)
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
