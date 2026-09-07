package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme Color Scheme with #31363F & #76ABAE Palette
private val SurfceDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue7692FF,
    onPrimary = BackgroundBunker222831,
    primaryContainer = DarkNavySurfaceVariant,
    onPrimaryContainer = TypographySilverEEEEEE,
    secondary = PrimaryBlue7692FF,
    onSecondary = BackgroundBunker222831,
    secondaryContainer = DarkNavyCard,
    onSecondaryContainer = TypographySilverEEEEEE,
    tertiary = CyanAccent,
    onTertiary = BackgroundBunker222831,
    background = DarkNavyBackground,
    onBackground = TextPrimaryDark,
    surface = DarkNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkNavyBorder,
    outlineVariant = Color(0xFF3A414C),
    error = FavoriteRed,
    onError = Color.White
)

// Light Theme Color Scheme with #31363F Primary
private val SurfceLightColorScheme = lightColorScheme(
    primary = PrimarySlate31363F,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = BackgroundBunker222831,
    secondary = GlacierAccent76ABAE,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2EBEB),
    onSecondaryContainer = PrimarySlate31363F,
    tertiary = GlacierAccent76ABAE,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = Color(0xFFCDD3D8),
    error = FavoriteRed,
    onError = Color.White
)

@Composable
fun SurfceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SurfceDarkColorScheme else SurfceLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for compatibility
@Composable
fun WavyxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    SurfceTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SurfceTheme(darkTheme = darkTheme, content = content)
}
