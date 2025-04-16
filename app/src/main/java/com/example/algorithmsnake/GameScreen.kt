package com.example.algorithmsnake


import android.content.Context
import android.graphics.Point
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import com.example.algorithmsnake.R

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

    // rootcontainer
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        val (gameViewRef, controlsRef, exitButtonRef) = createRefs()

        // GameView
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
        )


        // exit button
        Button(
            onClick = {
                gameViewInstance?.endGameAndCleanup()
                gameViewInstance = null
                navController.navigate(AppDestinations.MAIN_MENU_ROUTE) {
                    popUpTo(AppDestinations.MAIN_MENU_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .zIndex(1f) // draw on top
                .constrainAs(exitButtonRef) {
                    top.linkTo(parent.top, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    // touch area
                    width = Dimension.value(48.dp)
                    height = Dimension.value(48.dp)
                },
            contentPadding = PaddingValues(0.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.cross_small),
                contentDescription = "Exit Game",
                modifier = Modifier.size(32.dp) // Adjust size as needed within the button
            )
        }


        // arrow buttons
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
                Row(horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirUp) },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_up),
                            contentDescription = "Up",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirLeft) },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_left),
                            contentDescription = "Left",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                    Spacer(Modifier.width(64.dp))
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirRight) },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_right),
                            contentDescription = "Right",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { gameViewInstance?.setPlayerDirection(dirDown) },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painterResource(id = R.drawable.arrow_down),
                            contentDescription = "Down",
                            Modifier.size(ButtonDefaults.IconSize * 1.5f)
                        )
                    }
                }
            }
        }
    }
}