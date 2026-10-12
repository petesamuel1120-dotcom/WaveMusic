package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.LocalFrostedGlassHeaderStyle

// Keep the backdrop compact while retaining a gradual feather along its lower edge.
internal val HeaderBackdropExtensionHeight = 28.dp

/** Background-only pane; the host keeps it fixed while header controls scroll above it. */
@Composable
fun PersistentLiquidGlassHeaderBackdrop(
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.background,
) {
    val style = LocalFrostedGlassHeaderStyle.current
    if (!LocalLiquidGlassEnabled.current || !style.isEnabled) return
    Box(
        modifier
            .clipToBounds()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                // The sheet reaches the top and both sides of the viewport, behind status icons.
                // Taper across the whole pane so artwork becomes clear beneath the controls.
                // Offscreen isolates DstIn from the page and header controls.
                val fade = Brush.verticalGradient(
                    *(0..32).map { index ->
                        val progress = index / 32f
                        progress to Color.White.copy(alpha = headerMaterialOpacity(progress, style))
                    }.toTypedArray(),
                    startY = 0f,
                    endY = size.height,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(fade, blendMode = BlendMode.DstIn)
                }
            }
            .drawHeaderGlassBackdrop(
                backdrop = backdrop,
                tint = tint,
            ),
    )
}
