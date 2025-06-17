package com.example.testaddtoapp

import android.content.Context
import android.os.Bundle
import android.os.Debug;
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.testaddtoapp.ui.theme.TestAddToAppTheme
import io.flutter.embedding.android.FlutterSurfaceView
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode

enum class LayoutType {
    FLUTTER,
    NO_FLUTTER,
    MIX,
    MIX_FLUTTER_TOP
}

class MainActivity : ComponentActivity() {

    private lateinit var flutterViewEngines: FlutterViewEngines

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Debug.waitForDebugger();

        flutterViewEngines = FlutterViewEngines(applicationContext)
        flutterViewEngines.attachToActivity(this)


        setContent {
            TestAddToAppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
                        MyItemList(engines = flutterViewEngines)
                    }
                }
            }
        }
    }
}

@Composable
fun MyListItem(context: Context, itemText: String, modifier: Modifier = Modifier, engines: FlutterViewEngines) {
    Log.d("MyListItem", "Creating FlutterView for $itemText")
    var flutterView = FlutterView(context)

    var flutterViewEngine = engines.createAndRunEngine(itemText, listOf());
    flutterViewEngine.attachFlutterView(flutterView)

    AndroidView(
        factory = { context ->
            flutterView.apply {}
        },
        modifier = modifier
            .padding(16.dp)
            .wrapContentSize()
            .background(Color.LightGray),
    )
}

// Composable function for the list itself
@Composable
fun MyItemList(modifier: Modifier = Modifier, context: Context = LocalContext.current, engines: FlutterViewEngines) {
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