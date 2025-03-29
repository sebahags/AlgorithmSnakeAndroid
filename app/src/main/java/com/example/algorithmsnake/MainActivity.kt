package com.example.algorithmsnake

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.algorithmsnake.GameView
import com.example.algorithmsnake.ui.theme.AlgorithmSnakeTheme
import android.os.Bundle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optionally call enableEdgeToEdge() and other initializations
        setContent {
            AlgorithmSnakeTheme {
                // Use AndroidView to host our custom GameView
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        // Instantiate GameView. If using XML attributes, use the constructor with AttributeSet or customize here.
                        GameView(context)
                    }
                )
            }
        }
    }
}