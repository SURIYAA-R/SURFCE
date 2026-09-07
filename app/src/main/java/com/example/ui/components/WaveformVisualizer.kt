package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue7692FF
import com.example.ui.theme.LightRubyAccent
import com.example.ui.theme.DeepRubyAccent
import kotlin.math.PI
import kotlin.math.sin

/**
 * Interactive Waveform Duration Tracker.
 * Directly integrates dynamic audio wave animations with an interactive scrubber/duration tracker
 * utilizing the signature #7692FF blue styling and responsive dark/light theming.
 */
@Composable
fun WaveformDurationTracker(
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    amplitudes: List<Float>,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 88.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2f * PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_shift"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isPlaying) 1.15f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val activeProgress = if (isDragging) dragProgress else progress
    val effectivePositionMs = if (isDragging) (dragProgress * durationMs).toLong() else currentPositionMs

    val smoothProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = activeProgress,
        animationSpec = if (isDragging) tween(0) else tween(220, easing = androidx.compose.animation.core.LinearEasing),
        label = "smooth_waveform_progress"
    )
    val displayProgress = if (isDragging) activeProgress else smoothProgress

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = if (isDark) Color(0xFF0F1626) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) PrimaryBlue7692FF.copy(alpha = 0.25f) else PrimaryBlue7692FF.copy(alpha = 0.2f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("waveform_duration_tracker")
    ) {
        // Waveform Canvas with interactive touch & drag handling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .background(surfaceColor)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0 && size.width > 0) {
                            val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onSeekTo((newProgress * durationMs).toLong())
                        }
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (durationMs > 0 && size.width > 0) {
                                isDragging = true
                                dragProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            }
                        },
                        onDragEnd = {
                            if (durationMs > 0) {
                                onSeekTo((dragProgress * durationMs).toLong())
                                isDragging = false
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            if (durationMs > 0 && size.width > 0) {
                                change.consume()
                                dragProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val canvasHeight = size.height
                val centerY = canvasHeight / 2f

                // 1. Fluid Ambient Wave Backdrop with #7692FF
                drawFluidWaveLayer(
                    width = width,
                    height = canvasHeight,
                    centerY = centerY,
                    phase = phaseShift,
                    amplitude = if (isPlaying) 18f * breathingScale else 6f,
                    frequency = 0.014f,
                    color = PrimaryBlue7692FF.copy(alpha = if (isDark) 0.18f else 0.10f)
                )

                drawFluidWaveLayer(
                    width = width,
                    height = canvasHeight,
                    centerY = centerY,
                    phase = phaseShift * 1.3f + 1.2f,
                    amplitude = if (isPlaying) 12f * breathingScale else 4f,
                    frequency = 0.020f,
                    color = PrimaryBlue7692FF.copy(alpha = if (isDark) 0.25f else 0.15f)
                )

                // 2. Frequency Amplitude Bars with Played/Unplayed Color Split
                val barCount = if (amplitudes.isNotEmpty()) amplitudes.size else 36
                val spacing = 3.5f
                val totalSpacing = spacing * (barCount - 1)
                val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(3f)

                val activeIndex = (displayProgress * barCount).toInt().coerceIn(0, barCount - 1)

                for (i in 0 until barCount) {
                    val amp = amplitudes.getOrNull(i) ?: 0.3f
                    val dynamicAmp = if (isPlaying) {
                        val waveMod = (sin(phaseShift * 2f + i * 0.35f) * 0.22f)
                        (amp + waveMod).coerceIn(0.12f, 1f)
                    } else {
                        amp.coerceIn(0.15f, 0.45f)
                    }

                    val barHeight = (canvasHeight * 0.70f * dynamicAmp).coerceAtLeast(6f)
                    val x = i * (barWidth + spacing)
                    val y = centerY - (barHeight / 2f)

                    val isPlayed = i <= activeIndex
                    val brush = if (isPlayed) {
                        Brush.verticalGradient(
                            colors = listOf(
                                LightRubyAccent,
                                PrimaryBlue7692FF,
                                DeepRubyAccent
                            ),
                            startY = y,
                            endY = y + barHeight
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(PrimaryBlue7692FF.copy(alpha = 0.35f), Color(0x2531363F))
                            } else {
                                listOf(PrimaryBlue7692FF.copy(alpha = 0.25f), Color(0x1A76ABAE))
                            },
                            startY = y,
                            endY = y + barHeight
                        )
                    }

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                // 3. Glowing Playhead Scrubber Line
                val playheadX = (width * displayProgress).coerceIn(0f, width)
                val playheadHeight = canvasHeight * 0.85f
                val playheadY = centerY - (playheadHeight / 2f)

                // Playhead vertical needle
                drawLine(
                    color = PrimaryBlue7692FF,
                    start = Offset(playheadX, playheadY),
                    end = Offset(playheadX, playheadY + playheadHeight),
                    strokeWidth = 3.5f
                )

                // Glowing Playhead Head Indicator (orb at top)
                drawCircle(
                    color = PrimaryBlue7692FF.copy(alpha = 0.5f),
                    radius = 8f,
                    center = Offset(playheadX, playheadY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5f,
                    center = Offset(playheadX, playheadY)
                )
            }

            // Scrubber tooltip bubble when dragging
            if (isDragging) {
                val dragSec = (effectivePositionMs / 1000).coerceAtLeast(0)
                val formattedTime = String.format("%d:%02d", dragSec / 60, dragSec % 60)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xF00A101C) else Color(0xF0FFFFFF))
                        .border(1.dp, PrimaryBlue7692FF, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = PrimaryBlue7692FF,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Integrated Duration Tracker Time Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currSec = (effectivePositionMs / 1000).coerceAtLeast(0)
            val totalSec = (durationMs / 1000).coerceAtLeast(0)

            val currentFormatted = String.format("%d:%02d", currSec / 60, currSec % 60)
            val totalFormatted = String.format("%d:%02d", totalSec / 60, totalSec % 60)

            Text(
                text = currentFormatted,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = if (isDragging) PrimaryBlue7692FF else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = totalFormatted,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Organic Flowing Sound Waves visualizer for decorative and background cards.
 */
@Composable
fun OrganicWaveformVisualizer(
    isPlaying: Boolean,
    progress: Float,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2f * PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_shift"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isPlaying) 1.15f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("organic_waveform_visualizer")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f

            drawFluidWaveLayer(
                width = width,
                height = canvasHeight,
                centerY = centerY,
                phase = phaseShift,
                amplitude = if (isPlaying) 22f * breathingScale else 8f,
                frequency = 0.015f,
                color = PrimaryBlue7692FF.copy(alpha = if (isDark) 0.20f else 0.12f)
            )

            drawFluidWaveLayer(
                width = width,
                height = canvasHeight,
                centerY = centerY,
                phase = phaseShift * 1.3f + 1.5f,
                amplitude = if (isPlaying) 16f * breathingScale else 6f,
                frequency = 0.022f,
                color = PrimaryBlue7692FF.copy(alpha = if (isDark) 0.28f else 0.18f)
            )

            val barCount = if (amplitudes.isNotEmpty()) amplitudes.size else 32
            val spacing = 4f
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(3f)

            val activeIndex = (progress * barCount).toInt().coerceIn(0, barCount - 1)

            for (i in 0 until barCount) {
                val amp = amplitudes.getOrNull(i) ?: 0.3f
                val dynamicAmp = if (isPlaying) {
                    val waveMod = (sin(phaseShift * 2f + i * 0.4f) * 0.25f)
                    (amp + waveMod).coerceIn(0.12f, 1f)
                } else {
                    amp.coerceIn(0.15f, 0.4f)
                }

                val barHeight = (canvasHeight * 0.75f * dynamicAmp).coerceAtLeast(6f)
                val x = i * (barWidth + spacing)
                val y = centerY - (barHeight / 2f)

                val isPlayed = i <= activeIndex
                val brush = if (isPlayed) {
                    Brush.verticalGradient(
                        colors = listOf(
                            LightRubyAccent,
                            PrimaryBlue7692FF,
                            DeepRubyAccent
                        ),
                        startY = y,
                        endY = y + barHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(PrimaryBlue7692FF.copy(alpha = 0.35f), Color(0x2531363F))
                        } else {
                            listOf(PrimaryBlue7692FF.copy(alpha = 0.22f), Color(0x1576ABAE))
                        },
                        startY = y,
                        endY = y + barHeight
                    )
                }

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawFluidWaveLayer(
    width: Float,
    height: Float,
    centerY: Float,
    phase: Float,
    amplitude: Float,
    frequency: Float,
    color: Color
) {
    val path = Path()
    path.moveTo(0f, centerY)

    var x = 0f
    val step = 8f
    while (x <= width) {
        val y = centerY + sin(x * frequency + phase) * amplitude
        path.lineTo(x, y)
        x += step
    }

    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.close()

    drawPath(path = path, color = color, style = Fill)
}

/**
 * Mini animated waveform bars for list items and mini player.
 */
@Composable
fun MiniWaveformIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    color: Color = PrimaryBlue7692FF
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2f * PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mini_phase"
    )

    Canvas(
        modifier = modifier
            .height(16.dp)
            .testTag("mini_waveform_indicator")
    ) {
        val canvasHeight = size.height
        val barWidth = 3.dp.toPx()
        val spacing = 2.5.dp.toPx()

        for (i in 0 until barCount) {
            val factor = if (isPlaying) {
                (sin(phase * 2f + i * 1.3f) * 0.4f + 0.6f).coerceIn(0.2f, 1f)
            } else {
                0.3f
            }
            val barH = canvasHeight * factor
            val x = i * (barWidth + spacing)
            val y = canvasHeight - barH

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
