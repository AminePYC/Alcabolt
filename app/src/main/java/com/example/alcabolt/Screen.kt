package com.example.alcabolt

/**
 * Sealed class defining all the navigation destinations (screens) in the application.
 * This provides type-safe routes for use in the NavGraph and NavController.
 */
sealed class Screen(val route: String) {

    /**
     * The main translator screen for text input, voice input, and core translation output.
     */
    object Input : Screen("input_screen")

    /**
     * The screen displaying the history of past translations stored in the database.
     */
    object History : Screen("history_screen")
}