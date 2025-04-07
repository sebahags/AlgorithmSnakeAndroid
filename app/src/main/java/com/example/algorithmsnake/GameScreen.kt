package com.example.algorithmsnake

// Android & Compose imports
import android.content.Context
import android.graphics.Point
import androidx.compose.foundation.Image
import androidx.compose.foundation.background // Keep background import for root layout
import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.shape.CircleShape // No longer needed for explicit background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex // Import zIndex modifier
// Correct Compose ConstraintLayout Imports
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController

// IMPORTANT: Make sure you import your project's actual generated R class
import com.example.algorithmsnake.R // Adjust the package name if necessary

// IMPORTANT: Ensure your AppDestinations object/class is accessible from here
// Example: import com.example.algorithmsnake.navigation.AppDestinations

// IMPORTANT: Ensure your GameView class is accessible from here
// Example: import com.example.algorithmsnake.game.GameView


@Composable
fun GameScreen(
    navController: NavHostController,
    isPlayerMode: Boolean,
    gameSpeed: Int
) {
    val context = LocalContext.current
    var gameViewInstance by remember { mutableStateOf<GameView?>(null) }

    val dirUp = remember { Point(0, -1) }
    val dirDown = remember { Point(0, 1) }
    val dirLeft = remember { Point(-1, 0) }
    val dirRight = remember { Point(1, 0) }

    // Use ConstraintLayout as the root container
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding() // Keep padding for the status bar
    ) {

        val (gameViewRef, controlsRef, exitButtonRef) = createRefs()

        // --- GameView (AndroidView) ---
        AndroidView(
            factory = { ctx ->
                GameView(ctx, isPlayerMode, gameSpeed).also {
                    gameViewInstance = it
                }
            },
            modifier = Modifier
                .constrainAs(gameViewRef) {
                    top.linkTo(parent.top)
                    if (isPlayerMode) {
                        bottom.linkTo(controlsRef.top, margin = 16.dp)
                    } else {
                        bottom.linkTo(parent.bottom)
                    }
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .aspectRatio(1f),
            onRelease = { view ->
                view.endGameAndCleanup()
                gameViewInstance = null
            },
            update = { /* Optional */ }
        ) // End AndroidView


        // --- Exit Button (Now using Button) ---
        Button( // Changed from IconButton to Button
            onClick = {
                gameViewInstance?.endGameAndCleanup()
                gameViewInstance = null
                navController.navigate(AppDestinations.MAIN_MENU_ROUTE) {
                    popUpTo(AppDestinations.MAIN_MENU_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .zIndex(1f) // Keep zIndex to ensure it's drawn on top
                .constrainAs(exitButtonRef) {
                    top.linkTo(parent.top, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    // Keep the size constraints for the button's touch area
                    width = Dimension.value(48.dp)
                    height = Dimension.value(48.dp)
                },
            // Remove default internal padding to make icon fill better
            contentPadding = PaddingValues(0.dp),
            // Button will now automatically use theme's primary color and default shape
            // No need for explicit .background() modifier anymore
        ) {
            // Image content remains the same
            Image(
                painter = painterResource(id = R.drawable.cross_small),
                contentDescription = "Exit Game",
                modifier = Modifier.size(32.dp) // Adjust size as needed within the button
            )
        }


        // --- Control Buttons Area ---
        if (isPlayerMode) {
            Column(
                modifier = Modifier
                    .constrainAs(controlsRef) {
                        bottom.linkTo(parent.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                    }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- D-pad Buttons --- (Remain as Button)
                Row(horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirUp) },
                        modifier = Modifier.size(64.dp)
                        // These buttons already use the theme defaults
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_up),
                            contentDescription = "Up",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                }
                // ... (Rest of D-pad buttons omitted for brevity) ...
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirLeft) }, // Ensure method exists
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_left), // Uses your actual R class
                            contentDescription = "Left",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                    Spacer(Modifier.width(64.dp))
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirRight) }, // Ensure method exists
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_right), // Uses your actual R class
                            contentDescription = "Right",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirDown) }, // Ensure method exists
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_down), // Uses your actual R class
                            contentDescription = "Down",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                }
            } // End Controls Column
        } // End if (isPlayerMode)

    } // End ConstraintLayout
} // End GameScreen Composable