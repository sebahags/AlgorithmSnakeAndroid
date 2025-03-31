package com.example.algorithmsnake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.algorithmsnake.ui.theme.AlgorithmSnakeTheme
import com.example.algorithmsnake.AppNavigation  // Adjust package if needed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlgorithmSnakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create and remember the NavController
                    val navController = rememberNavController()
                    // Launch the navigation graph
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}