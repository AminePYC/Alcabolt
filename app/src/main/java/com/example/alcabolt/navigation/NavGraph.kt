package com.example.alcabolt.navigation

// ... imports ...
import com.example.alcabolt.ui.screens.HistoryScreen // <-- Ensure this is imported

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Input.route
    ) {
        composable(route = Screen.Input.route) {
            InputScreen(navController = navController)
        }
        composable(route = Screen.History.route) { // <-- The destination for the History Screen
            HistoryScreen(navController = navController)
        }
    }
}