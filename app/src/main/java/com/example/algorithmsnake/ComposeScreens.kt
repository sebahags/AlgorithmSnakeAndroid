package com.example.algorithmsnake
import android.app.Activity
import androidx.activity.compose.BackHandler // Import BackHandler specifically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.algorithmsnake.ui.theme.AlgorithmSnakeTheme

// This Composable function sets up the navigation graph
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.MAIN_MENU_ROUTE // Use constant from Java class
    ) {
        // Main Menu Screen Composable Definition
        composable(route = AppDestinations.MAIN_MENU_ROUTE) {
            MainMenuScreen(navController = navController)
        }

        // Game Screen Composable Definition
        composable(
            route = AppDestinations.GAME_SCREEN_ROUTE_PATTERN, // Use pattern from Java class
            arguments = listOf(
                navArgument(AppDestinations.IS_PLAYER_MODE_ARG) { type = NavType.BoolType },
                navArgument(AppDestinations.GAME_SPEED_ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // Retrieve arguments safely
            val isPlayerMode = backStackEntry.arguments?.getBoolean(AppDestinations.IS_PLAYER_MODE_ARG) ?: false
            val gameSpeed = backStackEntry.arguments?.getInt(AppDestinations.GAME_SPEED_ARG) ?: 55 // Default speed

            // This call will now unambiguously resolve to the GameScreen defined
            // in your separate GameScreen.kt file (the one from the Canvas)
            GameScreen(
                navController = navController, // Pass navController first
                isPlayerMode = isPlayerMode,
                gameSpeed = gameSpeed
            )
        }
    }
}


// Composable for the Main Menu UI
@OptIn(ExperimentalMaterial3Api::class) // Needed for ExposedDropdownMenuBox
@Composable
fun MainMenuScreen(navController: NavHostController) {
    val context = LocalContext.current
    val difficulties = listOf("Slugg Fest", "Medium", "Deranged")
    var selectedDifficulty by remember { mutableStateOf(difficulties[1]) } // Default to Medium
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Helper function (can be inside or outside the composable)
    fun getGameSpeedFromSelection(selection: String): Int {
        return when (selection) {
            "Slugg Fest" -> 90
            "Deranged" -> 25
            else -> 55 // Medium or default
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Match Java version background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Difficulty:", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Difficulty Dropdown
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedDifficulty,
                onValueChange = {}, // Not directly changeable
                readOnly = true,
                label = { Text("Difficulty") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(0.7f),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors( // Customize colors
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    disabledTextColor = Color.Gray, focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray, focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray, focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.LightGray
                )
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                difficulties.forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty) },
                        onClick = {
                            selectedDifficulty = difficulty
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Button(onClick = {
            val gameSpeed = getGameSpeedFromSelection(selectedDifficulty)
            // Navigate using the route builder from the Java class
            navController.navigate(AppDestinations.buildGameRoute(false, gameSpeed))
        }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Simulation")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val gameSpeed = getGameSpeedFromSelection(selectedDifficulty)
            // Navigate using the route builder from the Java class
            navController.navigate(AppDestinations.buildGameRoute(true, gameSpeed))
        }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Play")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Get the activity context and call finish() on it (Java Activity)
            val activity = context as? Activity
            activity?.finish()
        }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Exit")
        }
    }
}