package com.example.alcabolt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Enhanced Crimson/Ruby Red Theme with Gold Accents ---

// Primary Colors - Deep Crimson/Ruby Red
private val CrimsonPrimary = Color(0xFFC70039)
private val CrimsonPrimaryVariant = Color(0xFF900C3F)
private val CrimsonOnPrimary = Color(0xFFFFFFFF)

// Secondary Colors - Gold/Amber
private val GoldSecondary = Color(0xFFFFC300)
private val GoldSecondaryVariant = Color(0xFFFFAB00)
private val GoldOnSecondary = Color(0xFF000000)

// Tertiary Colors - Deep Purple
private val DeepPurple = Color(0xFF581845)
private val DeepPurpleVariant = Color(0xFF3A0F2E)
private val PurpleOnTertiary = Color(0xFFFFFFFF)

// Background Colors - Dark Theme
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkSurfaceVariant = Color(0xFF2C2C2C)
private val DarkSurfaceContainer = Color(0xFF242424)
private val DarkSurfaceContainerHigh = Color(0xFF3A3A3A)

// Background Colors - Light Theme
private val LightBackground = Color(0xFFFAFAFA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF5F5F5)
private val LightSurfaceContainer = Color(0xFFF0F0F0)
private val LightSurfaceContainerHigh = Color(0xFFE8E8E8)

// Text Colors
private val DarkOnBackground = Color(0xFFF0F0F0)
private val DarkOnSurface = Color(0xFFE8E8E8)
private val LightOnBackground = Color(0xFF1A1A1A)
private val LightOnSurface = Color(0xFF2C2C2C)

// Error Colors
private val ErrorRed = Color(0xFFD21F3C)
private val ErrorRedVariant = Color(0xFFB71C1C)
private val OnError = Color(0xFFFFFFFF)

// Success/Info Colors
private val SuccessGreen = Color(0xFF4CAF50)
private val InfoBlue = Color(0xFF2196F3)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = CrimsonOnPrimary,
    primaryContainer = CrimsonPrimaryVariant,
    onPrimaryContainer = Color(0xFFFFDADE),

    secondary = GoldSecondary,
    onSecondary = GoldOnSecondary,
    secondaryContainer = GoldSecondaryVariant,
    onSecondaryContainer = Color(0xFF3E2723),

    tertiary = DeepPurple,
    onTertiary = PurpleOnTertiary,
    tertiaryContainer = DeepPurpleVariant,
    onTertiaryContainer = Color(0xFFF3E5F5),

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),

    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF4A4A4A),

    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrorRedVariant,
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF8E4958)
)

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = CrimsonOnPrimary,
    primaryContainer = Color(0xFFFFDADE),
    onPrimaryContainer = CrimsonPrimaryVariant,

    secondary = GoldSecondary,
    onSecondary = GoldOnSecondary,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF3E2723),

    tertiary = DeepPurple,
    onTertiary = PurpleOnTertiary,
    tertiaryContainer = Color(0xFFF3E5F5),
    onTertiaryContainer = DeepPurpleVariant,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),

    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFFE0E0E0),

    error = ErrorRed,
    onError = OnError,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFFFB1C1)
)

@Composable
fun AlcaBoltTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Custom color extensions for special use cases
object AlcaBoltColors {
    val SuccessGreen = SuccessGreen
    val InfoBlue = InfoBlue
    val RecordingPulse = Color(0xFFFF5252)
    val TranslatingIndicator = Color(0xFFFFAB00)
    val SpeakingIndicator = Color(0xFF00BCD4)
}