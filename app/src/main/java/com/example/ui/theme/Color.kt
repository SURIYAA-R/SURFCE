package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Iconic #31363F Palette Architecture:
// #222831 (Deep Bunker Background)
// #31363F (Sophisticated Slate Graphite Surface / Card)
// #76ABAE (Luminous Glacier Teal Accent & Highlights)
// #EEEEEE (Clean Crisp Silver White Typography)
// =========================================================================

val PrimarySlate31363F = Color(0xFF31363F)       // The requested #31363F
val BackgroundBunker222831 = Color(0xFF222831)   // Deep charcoal background #222831
val GlacierAccent76ABAE = Color(0xFF76ABAE)      // Vibrant glacier teal #76ABAE
val TypographySilverEEEEEE = Color(0xFFEEEEEE)   // Crisp silver typography #EEEEEE

// Accent & compatibility aliases
val PrimaryBlue7692FF = Color(0xFF76ABAE)        // Primary active accent
val PrimaryRubyAE2448 = Color(0xFF76ABAE)        // Alias
val PrimaryTeal45A9A9 = Color(0xFF76ABAE)        // Alias
val RoyalBluePrimary = Color(0xFF76ABAE)         // Alias
val ElectricBlue = Color(0xFF76ABAE)             // Alias

val LightRubyAccent = Color(0xFF98C7C9)          // Lighter luminous glacier highlight
val LightTealAccent = Color(0xFF98C7C9)          // Alias
val LightBlueAccent = Color(0xFF98C7C9)          // Alias
val LightSlateAccent = Color(0xFF98C7C9)         // Alias

val DeepRubyAccent = Color(0xFF31363F)           // Deep #31363F slate
val DeepTealAccent = Color(0xFF31363F)           // Alias
val DeepBlueAccent = Color(0xFF31363F)           // Alias
val DeepSlateAccent = Color(0xFF31363F)          // Alias

val CyanAccent = Color(0xFF76ABAE)
val SoftRubySecondary = Color(0xFF76ABAE)
val SoftTealSecondary = Color(0xFF76ABAE)
val SoftBlueSecondary = Color(0xFF76ABAE)
val DeepIndigo = Color(0xFF222831)

// Dark Theme Surfaces & Backgrounds (#31363F & #222831)
val DeepCharcoal = Color(0xFF222831)
val DarkNavyBackground = Color(0xFF222831)
val DarkNavySurface = Color(0xFF31363F)
val DarkNavySurfaceVariant = Color(0xFF3A414C)
val DarkNavyCard = Color(0xFF2B313A)
val DarkNavyBorder = Color(0x3376ABAE)           // 20% #76ABAE subtle accent border
val SleekBlueBorder = Color(0x5976ABAE)          // 35% glow border

// Light Theme Surfaces & Backgrounds
val LightBackground = Color(0xFFEEEEEE)          // #EEEEEE from palette
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E6EA)
val LightCard = Color(0xFFFFFFFF)
val LightBorder = Color(0x2631363F)              // 15% #31363F border

// Functional & Text Colors
val TextPrimaryDark = Color(0xFFEEEEEE)          // #EEEEEE from palette
val TextSecondaryDark = Color(0xFFB0B8C4)
val TextMutedDark = Color(0xFF7E8A98)

val TextPrimaryLight = Color(0xFF222831)         // #222831 from palette
val TextSecondaryLight = Color(0xFF4B5563)
val TextMutedLight = Color(0xFF717D8D)

// Default fallbacks
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFFB0B8C4)
val TextMuted = Color(0xFF7E8A98)
val TextWhite = Color(0xFFFFFFFF)

val FavoriteRed = Color(0xFFF43F5E)
val OnlineGreen = Color(0xFF10B981)
val SyncPendingOrange = Color(0xFFF59E0B)

// Dynamic Gradients based on #31363F Palette
val WavyBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x3376ABAE),
        Color.Transparent,
        Color(0xFF222831)
    )
)

val GlowBlueGradient = Brush.radialGradient(
    colors = listOf(
        Color(0x4D76ABAE),
        Color(0x1A31363F),
        Color.Transparent
    )
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2B313A),
        Color(0xFF31363F)
    )
)

val WaveformGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF76ABAE),
        Color(0xFF98C7C9),
        Color(0xFF31363F)
    )
)
