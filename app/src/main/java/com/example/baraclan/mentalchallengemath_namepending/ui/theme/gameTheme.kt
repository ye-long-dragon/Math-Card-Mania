package com.example.baraclan.mentalchallengemath_namepending.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.baraclan.mentalchallengemath_namepending.ui.theme.LightColorScheme
import androidx.compose.ui.graphics.Color
// Define your desired color palettes for light and dark modes

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Purple40, // Example: A shade of purple for primary UI elements
    secondary = PurpleGrey40, // Example: A complementary secondary color
    tertiary = Pink40, // Example: Another accent color
    background = Color.White, // Standard white background
    surface = Color.White, // Surface for cards, sheets etc.
    onPrimary = Color.White, // Text/icons on primary color
    onSecondary = Color.Black, // Text/icons on secondary color
    onTertiary = Color.White, // Text/icons on tertiary color
    onBackground = Color.Black, // Text/icons on background
    onSurface = Color.Black, // Text/icons on surface
    surfaceVariant = Color(0xFFF0F0F0) // A slightly off-white for distinction
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Purple80, // Example: A lighter purple for primary UI elements in dark mode
    secondary = PurpleGrey80, // Example: A complementary secondary color
    tertiary = Pink80, // Example: Another accent color
    background = Color(0xFF1C1B1F), // Dark background color
    surface = Color(0xFF1C1B1F), // Dark surface color
    onPrimary = Color.Black, // Text/icons on primary color
    onSecondary = Color.White, // Text/icons on secondary color
    onTertiary = Color.Black, // Text/icons on tertiary color
    onBackground = Color.White, // Text/icons on background
    onSurface = Color.White, // Text/icons on surface
    surfaceVariant = Color(0xFF303030) // A slightly lighter dark for distinction
)

@Composable
fun gameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Check system dark theme preference
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming you have a Typography.kt
        shapes = Shapes, // Assuming you have a Shapes.kt
        content = content
    )
}
