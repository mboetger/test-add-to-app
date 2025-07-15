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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testaddtoapp.ui.theme.TestAddToAppTheme
import io.flutter.embedding.android.FlutterView
import androidx.core.view.isVisible

class MainActivity : ComponentActivity() {

    private lateinit var flutterViewEngines: FlutterViewEngines
    private var scrollingFlutterView: FlutterView? = null


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("MainActivity", "dispatchTouchEvent: $ev")
        if (ev != null) {
            val action = when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
                MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
                MotionEvent.ACTION_UP -> "ACTION_UP"
                MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
                MotionEvent.ACTION_POINTER_DOWN -> "ACTION_POINTER_DOWN"
                MotionEvent.ACTION_POINTER_UP -> "ACTION_POINTER_UP"
                else -> "OTHER (${ev.actionMasked})"
            }
            Log.d(
                "MainActivityTouch",
                "dispatchTouchEvent: Action: $action, X: ${ev.x}, Y: ${ev.y}, RawX: ${ev.rawX}, RawY: ${ev.rawY}"
            )

            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                val touchX = ev.rawX // Use rawX/rawY for global screen coordinates
                val touchY = ev.rawY
                // Find the root view of your activity; all FlutterViews will be descendants
                val rootView = window.decorView.rootView
                scrollingFlutterView = findFlutterViewUnderTouchIterative(rootView, touchX, touchY)
            }

            if (scrollingFlutterView != null) {
                // Now you have the FlutterView instance.
                // You might need to find its associated data/engine if you mapped them.
                // For instance, if you stored them in the view's tag:
                // val itemIdentifier = flutterViewUnderTouch.tag as? String
                // Log.d("MainActivityTouch", "FlutterView found under touch! Identifier: $itemIdentifier")

                Log.d(
                    "MainActivityTouch",
                    "FlutterView found under touch! Instance: $scrollingFlutterView"
                )

                // TODO: What do you want to do with this FlutterView?
                // - Pass the event to it specifically? (Compose usually handles this)
                // - Trigger some action based on which one was touched?
                if (scrollingFlutterView?.onTouchEvent(ev) == true) {
                    return true;
                }
                Log.d("MainActivityTouch", "flutterViewUnderTouch.onTouchEvent returned false")
            } else {
                Log.d("MainActivityTouch", "No FlutterView found directly under touch.")
            }
        }
        Log.d("MainActivityTouch", "dispatchTouchEvent: returning false")
        // To let the event propagate normally (including to Compose content):
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

            // Optional: Log all views being checked if needed, but can be verbose
            // Log.d("HitTestDebug", "Checking view: ${currentView.javaClass.simpleName}, ID: ${currentView.id}")

            if (currentView is FlutterView) {
                val globalVisibleRect = android.graphics.Rect()
                val isActuallyVisibleOnScreen = currentView.getGlobalVisibleRect(globalVisibleRect)
                // Also get location on screen for comparison, though globalVisibleRect is better for hit-testing
                val location = IntArray(2)
                currentView.getLocationOnScreen(location)

                Log.d("HitTestDebug", "Found FlutterView instance: $currentView")
                Log.d("HitTestDebug", "  - IsVisible (View.isVisible): ${currentView.isVisible}")
                Log.d("HitTestDebug", "  - IsActuallyVisibleOnScreen (getGlobalVisibleRect): $isActuallyVisibleOnScreen")
                Log.d("HitTestDebug", "  - GlobalVisibleRect: $globalVisibleRect")
                Log.d("HitTestDebug", "  - LocationOnScreen: [${location[0]}, ${location[1]}] (width: ${currentView.width}, height: ${currentView.height})")

                if (currentView.isVisible && isActuallyVisibleOnScreen) {
                    val containsTouch = globalVisibleRect.contains(screenTouchX.toInt(), screenTouchY.toInt())
                    Log.d("HitTestDebug", "  - GlobalVisibleRect.contains(touch): $containsTouch")
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
        var flutterView = FlutterView(context)

        var flutterViewEngine = engines.createAndRunEngine(itemText, listOf());
        flutterViewEngine.attachFlutterView(flutterView)

        AndroidView(
            factory = { context ->
                flutterView.apply {}
            },
            modifier = Modifier
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
}