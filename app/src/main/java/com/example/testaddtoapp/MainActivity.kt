package com.example.testaddtoapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testaddtoapp.ui.theme.TestAddToAppTheme
import io.flutter.embedding.android.FlutterView
import androidx.core.view.isVisible
import java.util.concurrent.ConcurrentHashMap

class MainActivity : ComponentActivity() {

    private lateinit var flutterViewEngines: FlutterViewEngines

    // To identify which FlutterView is which, we need to map FlutterView instances to their instanceIds
    // This is populated when FlutterViews are created/attached.
    private val flutterViewInstanceMap = ConcurrentHashMap<FlutterView, String>()

    var scrollingFlutterView: FlutterView? = null

    // Variables to store previous touch coordinates for delta calculation (for the primary pointer)
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var isTrackingTouchDelta = false // Flag to indicate if we have a valid lastTouchX/Y

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) return super.dispatchTouchEvent(ev)
        val action = ev.actionMasked
        // For multi-touch, you might want to get the specific pointer index:
        // val pointerIndex = ev.actionIndex

        // Current coordinates for the primary pointer (index 0)
        // If handling multiple pointers, loop through ev.pointerCount and use ev.getX(i), ev.getY(i)
        val currentX = ev.x // Relative to the view dispatching this event (Activity's DecorView)
        val currentY = ev.y
        var deltaY = 0f

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("MainActivityTouch", "ACTION_DOWN at X: $currentX, Y: $currentY")
                lastTouchX = currentX
                lastTouchY = currentY
                isTrackingTouchDelta = true // Start tracking for delta

                // Your existing logic to find scrollingFlutterView
                val touchRawX = ev.rawX
                val touchRawY = ev.rawY
                val rootView = window.decorView.rootView
                scrollingFlutterView = findFlutterViewUnderTouchIterative(rootView, touchRawX, touchRawY)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTrackingTouchDelta) {
                    deltaY = currentY - lastTouchY
                    Log.d("MainActivityTouch", "ACTION_MOVE - DeltaY: $deltaY (Current: $currentX, $currentY, Last: $lastTouchX, $lastTouchY)")

                    // Update last touch coordinates for the next MOVE event
                    lastTouchX = currentX
                    lastTouchY = currentY
                } else {
                    // This can happen if MOVE events are received without a preceding DOWN
                    // (e.g., if another view intercepted DOWN but released for MOVE).
                    // Initialize here if needed, or ignore.
                    lastTouchX = currentX
                    lastTouchY = currentY
                    isTrackingTouchDelta = true
                    Log.d("MainActivityTouch", "ACTION_MOVE - Initializing tracking at X: $currentX, Y: $currentY")
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d("MainActivityTouch", "ACTION_UP or ACTION_CANCEL. Resetting delta tracking.")
                isTrackingTouchDelta = false // Stop tracking delta
                // scrollingFlutterView will be cleared later
            }
        }


        if (scrollingFlutterView == null) return super.dispatchTouchEvent(ev)
        val instanceId = flutterViewInstanceMap[scrollingFlutterView]

        if (instanceId == null) return super.dispatchTouchEvent(ev)
        val scrollState = flutterInstanceScrollStates[instanceId];

        Log.d(
            "MainActivityTouch",
            "FlutterView found under touch! Instance: ${scrollingFlutterView.hashCode()} & $instanceId & $scrollState"
        )

        val forwardToFlutter = if (action == MotionEvent.ACTION_MOVE) {
            when(scrollState) {
                "atTop" -> deltaY < 0
                "atMiddle" -> true
                "atBottom" -> deltaY > 0
                else -> false
            }
        } else {
            true
        }

        if (!forwardToFlutter) {
            Log.d("MainActivityTouch", "Not forwarding to FlutterView")
            return super.dispatchTouchEvent(ev)
        }

        Log.d("MainActivityTouch", "Forwarding to FlutterView")

        // Get FlutterView's location on screen
        val flutterViewLocation = IntArray(2)
        scrollingFlutterView!!.getLocationOnScreen(flutterViewLocation)
        val flutterViewScreenX = flutterViewLocation[0]
        val flutterViewScreenY = flutterViewLocation[1]

        // Get the raw (absolute) screen coordinates of the touch event
        val rawTouchX = ev.rawX
        val rawTouchY = ev.rawY

        // Translate to coordinates relative to the FlutterView
        val relativeX = rawTouchX - flutterViewScreenX
        val relativeY = rawTouchY - flutterViewScreenY
        // Now, if you need to create a new MotionEvent to forward to Flutter
        // (e.g., if you're not using FlutterView.onTouchEvent directly or
        // need to modify the event), you can use these relative coordinates.

        // Create a new MotionEvent with coordinates relative to the FlutterView
        // Note: You might need to offset by ev.getX() - ev.getRawX() if you were
        // to use ev.getX/Y directly, but it's simpler to use rawX/Y and subtract.
        val newEvent = MotionEvent.obtain(
            ev.downTime,
            ev.eventTime,
            ev.action,
            relativeX, // Use the translated X
            relativeY, // Use the translated Y
            ev.pressure,
            ev.size,
            ev.metaState,
            ev.xPrecision,
            ev.yPrecision,
            ev.deviceId,
            ev.edgeFlags
        )


        // If you are using Flutter's AndroidViewController to send events,
        // you would use these relative coordinates when constructing an AndroidMotionEvent.
        // For example (conceptual, as direct sending like this is less common with FlutterView):
        // val androidMotionEvent = AndroidMotionEvent.obtain(newEvent, 0 /* deviceId */)
        // flutterEngine.renderer.dispatchPointerDataPacket(androidMotionEvent.rawPointerCoords, androidMotionEvent.pointerProperties);
        // androidMotionEvent.recycle();

        // The most straightforward way, if FlutterView.onTouchEvent handles it correctly:
        if (action == MotionEvent.ACTION_MOVE) {
            if (scrollingFlutterView?.onTouchEvent(newEvent) == true) { // Pass the NEW event
                newEvent.recycle() // Important: recycle MotionEvents you obtain
                return true
            }
        } else {
            // Other actions need to go to both if we want the Android scroll to work properly
            val result = super.dispatchTouchEvent(ev) && scrollingFlutterView?.onTouchEvent(newEvent) == true
            newEvent.recycle() // Recycle if not handled
            return result
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Debug.waitForDebugger();

        flutterViewEngines = FlutterViewEngines(applicationContext)
        flutterViewEngines.attachToActivity(this)


        setContent {
            TestAddToAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Yellow)) {
                        //MyStaticItem2()
                        MyItemList(engines = flutterViewEngines)
                    }
                }
            }
        }
    }

    private fun findFlutterViewUnderTouchIterative(
        rootView: View,
        screenTouchX: Float,
        screenTouchY: Float
    ): FlutterView? {
        val viewsToCheck: java.util.Queue<View> = java.util.ArrayDeque()
        viewsToCheck.offer(rootView)
        var foundFlutterView: FlutterView? = null

        Log.d("HitTestDebug", "----------------------------------------------------")
        Log.d("HitTestDebug", "Searching for view at screenX: $screenTouchX, screenY: $screenTouchY")

        while (viewsToCheck.isNotEmpty()) {
            val currentView = viewsToCheck.poll() ?: continue

            if (currentView is FlutterView) {
                val globalVisibleRect = android.graphics.Rect()
                val isActuallyVisibleOnScreen = currentView.getGlobalVisibleRect(globalVisibleRect)
                // Also get location on screen for comparison, though globalVisibleRect is better for hit-testing
                val location = IntArray(2)
                currentView.getLocationOnScreen(location)

                if (currentView.isVisible && isActuallyVisibleOnScreen) {
                    val containsTouch = globalVisibleRect.contains(screenTouchX.toInt(), screenTouchY.toInt())
                    if (containsTouch) {
                        Log.d("HitTestDebug", "  >>> MATCH! Returning this FlutterView.")
                        foundFlutterView = currentView
                        break
                    }
                } else {
                    Log.d("HitTestDebug", "  - View is not visible or not on screen.")
                }
            }

            if (currentView is ViewGroup) {
                for (i in 0 until currentView.childCount) {
                    currentView.getChildAt(i)?.let { child ->
                        viewsToCheck.offer(child)
                    }
                }
            }
        }
        if (foundFlutterView == null) {
            Log.d("HitTestDebug", "No FlutterView found matching coordinates.")
        }
        Log.d("HitTestDebug", "----------------------------------------------------")
        return foundFlutterView
    }

    @Composable
    fun MyListItem(
        context: Context,
        itemText: String,
        modifier: Modifier = Modifier,
        engines: FlutterViewEngines
    ) {
        Log.d("MyListItem", "Creating FlutterView for $itemText")
        val flutterView = FlutterView(context)

        val flutterViewEngine = engines.createAndRunEngine(itemText, listOf());
        flutterViewEngine.attachFlutterView(flutterView)

        flutterViewInstanceMap[flutterView] = itemText // Associate instance with ID

        // Ensure cleanup when the composable is disposed
        DisposableEffect(flutterView) {
            onDispose {
                Log.d("MyListItem", "Disposing FlutterView ${flutterView.hashCode()} for ID $itemText. Removing from map.")
                flutterViewInstanceMap.remove(flutterView)
            }
        }


        AndroidView(
            factory = { context ->
                flutterView.apply {}
            },
            modifier = modifier
                .padding(16.dp)
                .height(400.dp)
                .background(Color.LightGray),
        )
    }

    // Composable function for the list itself
    @Composable
    fun MyItemList(
        modifier: Modifier = Modifier,
        context: Context = LocalContext.current,
        engines: FlutterViewEngines
    ) {
        // Sample data for the list
        val numFlutterViews = 30;
        val items = (1..numFlutterViews).toList();

        LazyColumn(modifier = modifier) {
            // The 'items' block takes a list and a lambda for how to display each item
            items(items) { itemNumber ->
                MyListItem(context = context, itemText = itemNumber.toString(), engines = engines)
            }
        }
    }

    companion object {
        // Thread-safe map to store scroll states from Flutter
        // Key: instanceId (String), Value: scrollState (String, e.g., "atTop", "atMiddle", "atBottom")
        private val flutterInstanceScrollStates = ConcurrentHashMap<String, String>()

        fun staticHandleScrollState(instanceId: String, state: String) {
            Log.i("MainActivity", "Static Handler: Scroll state for instance '$instanceId' is '$state'")
            flutterInstanceScrollStates[instanceId] = state
        }

        fun getScrollStateForInstance(instanceId: String): String? {
            return flutterInstanceScrollStates[instanceId]
        }
    }
}
