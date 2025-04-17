package com.example.algorithmsnake
import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

//navigation graph
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.MAIN_MENU_ROUTE
    ) {
        // Main Menu composable
        composable(route = AppDestinations.MAIN_MENU_ROUTE) {
            MainMenuScreen(navController = navController)
        }
        // Game Screen composable
        composable(
            route = AppDestinations.GAME_SCREEN_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AppDestinations.IS_PLAYER_MODE_ARG) { type = NavType.BoolType },
                navArgument(AppDestinations.GAME_SPEED_ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val isPlayerMode = backStackEntry.arguments?.getBoolean(AppDestinations.IS_PLAYER_MODE_ARG) ?: false
            val gameSpeed = backStackEntry.arguments?.getInt(AppDestinations.GAME_SPEED_ARG) ?: 55 // Default speed
            GameScreen(
                navController = navController,
                isPlayerMode = isPlayerMode,
                gameSpeed = gameSpeed
            )
        }
    }
}


// main menu composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {
    val context = LocalContext.current
    val difficulties = listOf("Slugg Fest", "Medium", "Deranged")
    var selectedDifficulty by remember { mutableStateOf(difficulties[1]) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // get gamespeed in milliseconds
    fun getGameSpeedFromSelection(selection: String): Int {
        return when (selection) {
            "Slugg Fest" -> 105
            "Deranged" -> 29
            else -> 63
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Algorithm Snake Logo",
            modifier = Modifier
                .fillMaxWidth(0.6f)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text("Select Difficulty:", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

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

        //menubuttons
        Button(onClick = {
            val gameSpeed = getGameSpeedFromSelection(selectedDifficulty)
            navController.navigate(AppDestinations.buildGameRoute(false, gameSpeed))
        }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Simulation")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val gameSpeed = getGameSpeedFromSelection(selectedDifficulty)
            navController.navigate(AppDestinations.buildGameRoute(true, gameSpeed))
        }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Play")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val activity = context as? Activity
            activity?.finish()
        }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Exit")
        }
    }
}