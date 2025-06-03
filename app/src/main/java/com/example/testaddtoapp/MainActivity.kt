package com.example.testaddtoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testaddtoapp.ui.theme.TestAddToAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestAddToAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call our new list composable
                    MyItemList()
                }
            }
        }
    }
}

// Composable function for a single item in the list
@Composable
fun MyListItem(itemText: String, modifier: Modifier = Modifier) {
    Text(
        text = itemText,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp) // Add some padding to each item
    )
}

// Composable function for the list itself
@Composable
fun MyItemList(modifier: Modifier = Modifier) {
    // Sample data for the list
    val items = listOf(
        "Row 1: Hello from Compose!",
        "Row 2: This is a list item.",
        "Row 3: LazyColumn is efficient.",
        "Row 4: Welcome to Jetpack Compose.",
        "Row 5: Another item in the list.",
        "Row 6: Scrolling is smooth!",
        "Row 7: Item seven",
        "Row 8: Item eight",
        "Row 9: Item nine",
        "Row 10: Item ten"
    )

    LazyColumn(modifier = modifier) {
        // The 'items' block takes a list and a lambda for how to display each item
        items(items) { individualItemText ->
            MyListItem(itemText = individualItemText)
        }

        // You can also add individual items directly
        item {
            MyListItem(itemText = "A special single item at the end")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyItemListPreview() {
    TestAddToAppTheme {
        MyItemList()
    }
}

// You can keep or remove the old GreetingPreview
@Preview(showBackground = true)
@Composable
fun OldGreetingPreview() {
    TestAddToAppTheme {
        // Centering the old Greeting for preview if you want to keep it around
        // Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        //     Greeting("Android")
        // }
        Text("Preview for a single item (Old Greeting)")
    }
}