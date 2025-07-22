package com.example.testaddtoapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import io.flutter.embedding.android.FlutterSurfaceView

class MainActivity : ComponentActivity() {

    private lateinit var flutterViewEngines: FlutterViewEngines

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Debug.waitForDebugger();

        flutterViewEngines = FlutterViewEngines(applicationContext)
        flutterViewEngines.attachToActivity(this)


        setContent {
            TestAddToAppTheme {
                MyStaticItem(engines = flutterViewEngines)
                MyItemList(engines = flutterViewEngines)
            }
        }
    }
}

@Composable
fun MyStaticItem(context: Context = LocalContext.current, engines: FlutterViewEngines) {
    // Fixes https://github.com/flutter/flutter/issues/169295
    var flutterSurfaceView = FlutterSurfaceView(context)
    flutterSurfaceView.setZOrderOnTop(true)
    var flutterView = FlutterView(context, flutterSurfaceView)

    // Deprecated API to fix https://github.com/flutter/flutter/issues/169295
    //var flutterView = FlutterView(context, RenderMode.surface, TransparencyMode.transparent)

    // Causes https://github.com/flutter/flutter/issues/169295
    //var flutterView = FlutterView(context)

    var flutterViewEngine = engines.createAndRunEngine("static", listOf())
    flutterViewEngine.attachFlutterView(flutterView)

    AndroidView(
        factory = { context ->
            flutterView.apply {}
        },
        modifier = Modifier
            .padding(16.dp)
            //.wrapContentHeight()
            .height(300.dp)
            .background(Color.Cyan)
    )
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
            .wrapContentHeight()
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

