package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled

/** Only the backdrop reads this size; dock measurements do not recompose the page. */
@Stable
class FrostedHorizonDockState {
    var heightPx by mutableIntStateOf(0)
        private set

    fun updateSize(size: IntSize) { heightPx = size.height }
}

/** Screen-anchored bottom material, sized to the complete measured player/navigation dock. */
@Composable
fun FrostedGlassHorizonHost(
    backdrop: PlatformBackdrop,
    dockState: FrostedHorizonDockState,
    style: FrostedGlassHeaderStyle,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible || !style.isEnabled || !LocalLiquidGlassEnabled.current) return
    val dockHeight = with(LocalDensity.current) { dockState.heightPx.toDp() }
    if (dockState.heightPx <= 0) return
    FrostedGlassHorizonBackdrop(
        backdrop = backdrop,
        style = style,
        modifier = modifier.height(dockHeight + HeaderBackdropExtensionHeight),
    )
}

/** Reverse the header's gradient: clear at the upper edge, most diffused at the screen bottom. */
@Composable
fun FrostedGlassHorizonBackdrop(
    backdrop: PlatformBackdrop,
    style: FrostedGlassHeaderStyle,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.background,
) {
    if (!style.isEnabled || !LocalLiquidGlassEnabled.current) return
    Box(
        modifier.clipToBounds()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val fade = Brush.verticalGradient(
                    *(0..32).map { index ->
                        val progress = index / 32f
                        progress to Color.White.copy(alpha = headerMaterialOpacity(1f - progress, style))
                    }.toTypedArray(), startY = 0f, endY = size.height,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(fade, blendMode = BlendMode.DstIn)
                }
            }
            .drawFrostedGlassBackdrop(backdrop, tint, style = style, fromBottom = true),
    )
}
