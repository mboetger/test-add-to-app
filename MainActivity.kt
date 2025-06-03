package com.example.testaddtoapp// Your package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box // Used for centering
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment // Used for centering
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.helloworldapp.ui.theme.HelloWorldAppTheme // Your app's theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply your app's theme (colors, typography)
            HelloWorldAppTheme {
                // A Surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), // Make the Surface fill the whole screen
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call your main composable function
                    Greeting("Android")
                }
            }
        }
    }
}

// This is your main UI component (a "composable" function)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // Box is a layout composable that can be used for simple arrangements,
    // like centering its content.
    Box(
        modifier = modifier.fillMaxSize(), // Make the Box fill its parent (the Surface)
        contentAlignment = Alignment.Center // Center its children
    ) {
        Text(
            text = "Hello, $name!"
        )
    }
}

// This allows you to see a preview of your composable in Android Studio
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloWorldAppTheme {
        Greeting("Android")
    }
}