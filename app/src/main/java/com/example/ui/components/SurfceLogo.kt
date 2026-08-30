package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue7692FF

/**
 * SURFCE Brand Logo with extra bright luminous 'S' letter and flowing waveform styling.
 * Adapts seamlessly across both Dark and Light themes with #7692FF accents.
 */
@Composable
fun SurfceLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    showWaveBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "s_glow_anim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val sGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF), // pure brilliant white highlight
            Color(0xFF9DE0E0), // soft aqua mint highlight
            PrimaryBlue7692FF, // #45A9A9
            Color(0xFF2B7878)  // deep teal
        )
    )

    Row(
        modifier = modifier
            .testTag("surfce_brand_logo")
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing container for the ultra-bright 'S'
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(end = 1.dp)
        ) {
            // Ambient neon glow layer behind 'S'
            Text(
                text = "S",
                fontSize = fontSize * 1.08f,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = PrimaryBlue7692FF.copy(alpha = glowAlpha * 0.75f),
                modifier = Modifier
                    .blur(6.dp)
                    .padding(end = 1.dp)
            )

            // Foreground brilliant 'S'
            Text(
                text = "S",
                fontSize = fontSize * 1.08f,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                style = androidx.compose.ui.text.TextStyle(
                    brush = sGradient,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // Crisp sleek 'URFCE' typography adapting to theme
        Text(
            text = "URFCE",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (fontSize.value * 0.08f).sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (showWaveBadge) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PrimaryBlue7692FF.copy(alpha = 0.18f))
                    .border(1.dp, PrimaryBlue7692FF.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniWaveformIndicator(
                        isPlaying = true,
                        barCount = 3,
                        color = PrimaryBlue7692FF
                    )
                }
            }
        }
    }
}
