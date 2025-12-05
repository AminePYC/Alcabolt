// file: com.example.alcabolt.ui.theme/Theme.kt

package com.example.alcabolt.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// --- Custom Crimson Palette ---

// Primary: Deep Crimson / Ruby Red
val CrimsonPrimary = Color(0xFFC70039)
val CrimsonOnPrimary = Color.White
// Secondary: Gold/Amber for contrast
val CrimsonSecondary = Color(0xFFFFC300)
val CrimsonOnSecondary = Color.Black
// Background: Deep Charcoal/Black for elegance
val CrimsonBackground = Color(0xFF1A1A1A)
val CrimsonOnBackground = Color(0xFFF0F0F0) // Light text
// Surface: Darker gray for cards
val CrimsonSurface = Color(0xFF2C2C2C)
val CrimsonOnSurface = Color(0xFFF0F0F0)


private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = CrimsonOnPrimary,
    primaryContainer = CrimsonPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = CrimsonOnPrimary,
    secondary = CrimsonSecondary,
    onSecondary = CrimsonOnSecondary,
    tertiary = Color(0xFF581845),
    background = CrimsonBackground,
    onBackground = CrimsonOnBackground,
    surface = CrimsonSurface,
    onSurface = CrimsonOnSurface,
    surfaceContainerHigh = Color(0xFF3A3A3A),
    error = Color(0xFFD21F3C) // A slightly brighter error red
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    secondary = CrimsonSecondary,
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    /* other colors */
)

@Composable
fun AlcaboltTheme(
    darkTheme: Boolean = true, // We default to the dark, elegant theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Fallback or use Light theme if not dark
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}