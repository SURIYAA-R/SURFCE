package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Custom 45A9A9 Oceanic Teal Core Palette
// ==========================================
val PrimaryTeal45A9A9 = Color(0xFF45A9A9)       // The requested #45A9A9
val PrimaryBlue7692FF = Color(0xFF45A9A9)       // Compatibility alias pointing to #45A9A9
val RoyalBluePrimary = Color(0xFF45A9A9)        // #45A9A9
val ElectricBlue = Color(0xFF45A9A9)            // #45A9A9
val LightTealAccent = Color(0xFF6EC4C4)         // Lighter tint of #45A9A9
val LightBlueAccent = Color(0xFF6EC4C4)         // Alias
val DeepTealAccent = Color(0xFF2B7878)          // Deeper companion of #45A9A9
val DeepBlueAccent = Color(0xFF2B7878)          // Alias
val CyanAccent = Color(0xFF45A9A9)
val SoftTealSecondary = Color(0xFF86ACAC)
val SoftBlueSecondary = Color(0xFF86ACAC)       // Alias
val DeepIndigo = Color(0xFF0D2525)

// Dark Theme Surfaces & Backgrounds
val DeepCharcoal = Color(0xFF071213)
val DarkNavyBackground = Color(0xFF071213)
val DarkNavySurface = Color(0xFF0E1F21)
val DarkNavySurfaceVariant = Color(0xFF152C2F)
val DarkNavyCard = Color(0xFF0A181A)
val DarkNavyBorder = Color(0x3345A9A9)          // 20% #45A9A9 subtle border
val SleekBlueBorder = Color(0x5945A9A9)         // 35% #45A9A9 glow border

// Light Theme Surfaces & Backgrounds
val LightBackground = Color(0xFFF1F7F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFDFEDED)
val LightCard = Color(0xFFFFFFFF)
val LightBorder = Color(0x2645A9A9)

// Functional & Text Colors
val TextPrimaryDark = Color(0xFFF1F5F9)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0B1B1C)
val TextSecondaryLight = Color(0xFF445B5C)
val TextMutedLight = Color(0xFF6E8687)

// Default fallbacks
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextWhite = Color(0xFFFFFFFF)

val FavoriteRed = Color(0xFFF43F5E)
val OnlineGreen = Color(0xFF10B981)
val SyncPendingOrange = Color(0xFFF59E0B)

// Dynamic Gradients based on #45A9A9
val WavyBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x3345A9A9),
        Color.Transparent,
        Color(0xFF071213)
    )
)

val GlowBlueGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x4D45A9A9),
        Color(0x1A2B7878),
        Color.Transparent
    )
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0A181A),
        Color(0xFF0E1F21)
    )
)

val WaveformGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF45A9A9),
        Color(0xFF6EC4C4),
        Color(0xFF2B7878)
    )
)
