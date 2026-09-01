package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.ui.theme.LightTealAccent
import com.example.ui.theme.DeepTealAccent

/**
 * SURFCE Brand Logo with oceanic teal emblem, luminous 'S' wave letter, and flowing waveform styling.
 * Adapts seamlessly across both Dark and Light themes with #45A9A9 Oceanic Teal accents.
 */
@Composable
fun SurfceLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    showEmblem: Boolean = true,
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
            LightTealAccent,   // #6EC4C4 soft aqua mint highlight
            PrimaryBlue7692FF, // #45A9A9 Oceanic Teal
            DeepTealAccent     // #2B7878 deep companion teal
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
        if (showEmblem) {
            Box(
                modifier = Modifier
                    .size((fontSize.value * 1.35f).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                LightTealAccent.copy(alpha = 0.8f),
                                PrimaryBlue7692FF.copy(alpha = 0.4f),
                                DeepTealAccent.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(Color(0xFF071213)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_surfce_teal_logo_1788271894352),
                    contentDescription = "SURFCE Logo Emblem",
                    modifier = Modifier
                        .size((fontSize.value * 1.25f).dp)
                        .clip(RoundedCornerShape(7.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

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
