package com.petedev.wavemusic.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.ui.theme.LocalAppColorScheme
import kotlin.math.PI
import kotlin.math.sin
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.loading

/** A small, continuous equalizer wave shared by page and inline loading states. */
@Composable
fun CenterLoadingBox(modifier: Modifier) {
    val colorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val indicatorColor = colorScheme.primary
    val loadingDescription = stringResource(Res.string.loading)
    val animation = rememberInfiniteTransition(label = "Loading equalizer")
    val phase by animation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Equalizer phase",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.size(width = 41.dp, height = 32.dp).semantics {
                contentDescription = loadingDescription
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            },
        ) {
            val barWidth = 5.dp.toPx()
            val spacing = 4.dp.toPx()
            val totalWidth = 5 * barWidth + 4 * spacing
            val startX = (size.width - totalWidth) / 2f
            val minimumHeight = 8.dp.toPx()
            val amplitude = 20.dp.toPx()
            // Read the phase while drawing: animation only redraws this tiny canvas.
            val cycle = phase * (2 * PI).toFloat()
            repeat(5) { index ->
                val pulse = (sin(cycle - index * 0.8f) + 1f) / 2f
                val envelope = 1f - kotlin.math.abs(index - 2) * 0.15f
                val height = minimumHeight + amplitude * envelope * pulse
                drawRoundRect(
                    color = indicatorColor.copy(alpha = 0.65f + 0.35f * pulse),
                    topLeft = Offset(startX + index * (barWidth + spacing), (size.height - height) / 2f),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
            }
        }
    }
}