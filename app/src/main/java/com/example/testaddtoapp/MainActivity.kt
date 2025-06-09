package com.example.testaddtoapp

import android.content.Context
import android.os.Bundle
import android.os.Debug;
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testaddtoapp.ui.theme.TestAddToAppTheme
import io.flutter.embedding.android.FlutterView


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
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call our new list composable
                    MyItemList(engines = flutterViewEngines)
                }
            }
        }
    }
}

// Composable function for a single item in the list
@Composable
fun MyListItem(context: Context, itemText: String, modifier: Modifier = Modifier, engines: FlutterViewEngines) {
    Log.d("MyListItem", "Creating FlutterView for $itemText")
    var flutterView = FlutterView(context) //, itemText)

    var flutterViewEngine = engines.createAndRunEngine(itemText, listOf());
    flutterViewEngine.attachFlutterView(flutterView)

    AndroidView(
        factory = { context ->
            flutterView.apply {}
        },
        modifier = Modifier.padding(16.dp).height(300.dp),
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
