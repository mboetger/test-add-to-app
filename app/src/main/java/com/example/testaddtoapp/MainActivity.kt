package com.example.testaddtoapp

import android.content.Context
import android.os.Bundle
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
import io.flutter.FlutterInjector
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor


class MainActivity : ComponentActivity() {

    private lateinit var flutterViewEngine: FlutterViewEngine
    override fun onDestroy() {
        super.onDestroy()
        flutterViewEngine.detachActivity()
    }

    override fun onUserLeaveHint() {
        flutterViewEngine.onUserLeaveHint()
        super.onUserLeaveHint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: create a multi-engine version after
        // https://github.com/flutter/flutter/issues/72009 is built.
        val engine = FlutterEngine(applicationContext)
        engine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint(
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                "main"))

        flutterViewEngine = FlutterViewEngine(engine)
        // The activity and FlutterView have different lifecycles.
        // Attach the activity right away but only start rendering when the
        // view is also scrolled into the screen.
        flutterViewEngine.attachToActivity(this)

        setContent {
            TestAddToAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call our new list composable
                    MyItemList(engine = engine)
                }
            }
        }
    }
}

// Composable function for a single item in the list
@Composable
fun MyListItem(context: Context, engine: FlutterEngine, itemText: String, modifier: Modifier = Modifier) {
    var flutterView = FlutterView(context)
    flutterView.attachToFlutterEngine(engine);
    AndroidView(
        factory = { context ->
            flutterView.apply {}
        },
        modifier = Modifier.padding(16.dp).height(300.dp),
    )
}

// Composable function for the list itself
@Composable
fun MyItemList(modifier: Modifier = Modifier, context: Context = LocalContext.current, engine: FlutterEngine) {
    // Sample data for the list
    val items = (1..20).toList();

    LazyColumn(modifier = modifier) {
        // The 'items' block takes a list and a lambda for how to display each item
        items(items) { itemNumber ->
            MyListItem(context = context, engine = engine, itemText = itemNumber.toString())
        }
    }
}
