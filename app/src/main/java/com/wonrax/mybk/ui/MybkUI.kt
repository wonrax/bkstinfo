package com.wonrax.mybk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.wonrax.mybk.model.SnackbarManager
import com.wonrax.mybk.ui.component.BottomNavigation
import com.wonrax.mybk.ui.component.FontWeight
import com.wonrax.mybk.ui.component.Navigation
import com.wonrax.mybk.ui.component.Text
import com.wonrax.mybk.ui.theme.Color
import com.wonrax.mybk.ui.theme.MybkTheme
import com.wonrax.mybk.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MybkAppState(
    val navController: NavHostController,
    val scaffoldState: ScaffoldState,
    private val snackbarManager: SnackbarManager,
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            snackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]

                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    scaffoldState.snackbarHostState.showSnackbar(
                        message.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Long
                    )
                    // Once the snackbar is gone or dismissed, notify the SnackbarManager
                    snackbarManager.setMessageShown(message.id)
                }
            }
        }
    }
}

/**
 * Remembers and creates an instance of [MybkAppState]
 */
@Composable
fun rememberMybkAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
) =
    remember(scaffoldState, navController, snackbarManager) {
        MybkAppState(navController, scaffoldState, snackbarManager)
    }

@Composable
fun MybkUI(mainActivityViewModel: MainActivityViewModel) {
    MybkTheme(false) {
        val appState = rememberMybkAppState()

        val navController = appState.navController

        Scaffold(
            scaffoldState = appState.scaffoldState,
            snackbarHost = { CustomSnackbarHost(it) },
            bottomBar = { CustomBottomNavigation(navController) }
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Grey10)
                    .fillMaxSize(),
            ) {
                Navigation(navController, mainActivityViewModel)
            }
        }
    }
}

@Composable
fun CustomSnackbarHost(snackbarHostState: SnackbarHostState) {
    // reuse default SnackbarHost to have default animation and timing handling
    SnackbarHost(snackbarHostState) { data ->
        // custom snackbar with the custom border
        Snackbar(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = 12.dp,
            content = {
                Text(text = data.message, color = Color.Light)
            },
            action = {
                data.actionLabel?.let { it1 ->
                    Text(
                        text = it1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { data.dismiss() }
                            .padding(12.dp, 8.dp),
                        color = Color.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun CustomBottomNavigation(navController: NavHostController) {
    BottomNavigation(navController) {
        navController.navigate(it) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }

            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true

            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
