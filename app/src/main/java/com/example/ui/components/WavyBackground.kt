package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import com.example.ui.theme.PrimaryBlue7692FF
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyAtmosphereBackground(
    modifier: Modifier = Modifier,
    isSubtleAnimation: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSubtleAnimation) (2f * PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_phase"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val baseBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            PrimaryBlue7692FF.copy(alpha = 0.12f),
                            baseBg,
                            baseBg
                        )
                    } else {
                        listOf(
                            PrimaryBlue7692FF.copy(alpha = 0.08f),
                            baseBg,
                            baseBg
                        )
                    }
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-right soft atmospheric teal ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrimaryBlue7692FF.copy(alpha = if (isDark) 0.16f else 0.09f),
                        PrimaryBlue7692FF.copy(alpha = if (isDark) 0.05f else 0.02f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.92f, height * 0.10f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.92f, height * 0.10f)
            )

            // Bottom-left soft atmospheric teal ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrimaryBlue7692FF.copy(alpha = if (isDark) 0.16f else 0.09f),
                        PrimaryBlue7692FF.copy(alpha = if (isDark) 0.05f else 0.02f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.08f, height * 0.90f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.08f, height * 0.90f)
            )

            // Flowing soft sound wave silhouette in background
            val path = Path()
            val waveY = height * 0.42f
            path.moveTo(0f, waveY)

            var x = 0f
            val step = 16f
            while (x <= width) {
                val y = waveY + sin(x * 0.004f + phase) * 20f + sin(x * 0.002f + phase * 0.6f) * 14f
                path.lineTo(x, y)
                x += step
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBlue7692FF.copy(alpha = if (isDark) 0.08f else 0.04f),
                        PrimaryBlue7692FF.copy(alpha = if (isDark) 0.02f else 0.01f),
                        Color.Transparent
                    ),
                    startY = waveY - 30f,
                    endY = height
                ),
                style = Fill
            )
        }

        content()
    }
}
