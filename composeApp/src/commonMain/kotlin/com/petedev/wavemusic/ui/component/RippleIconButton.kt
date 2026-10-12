package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.ui.icon.Pause
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.WaveIcons

internal val LocalHeaderIconSize = staticCompositionLocalOf { 24.dp }
internal val LocalHeaderControlSize = staticCompositionLocalOf<Dp?> { null }

@Composable
fun RippleIconButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    fillMaxSize: Boolean = false,
    tint: Color = Color.White,
    iconOffsetX: Dp = 0.dp,
    iconSize: Dp = LocalHeaderIconSize.current,
    contentDescription: String? = null,
    showOnyxContainer: Boolean = true,
    onClick: () -> Unit,
) {
    val headerControlSize = LocalHeaderControlSize.current
    val buttonModifier = if (headerControlSize != null) Modifier.size(headerControlSize).then(modifier) else modifier
    IconButton(
        onClick = onClick,
        modifier = if (showOnyxContainer && !LocalGlassControlContainer.current) buttonModifier.onyxControlSurface(CircleShape) else buttonModifier,
        shape = CircleShape,
    ) {
        Icon(
            imageVector,
            contentDescription,
            tint = tint,
            modifier =
                if (fillMaxSize && headerControlSize == null) {
                    Modifier.fillMaxSize().padding(4.dp)
                } else {
                    Modifier.offset(x = iconOffsetX).size(iconSize)
                },
        )
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    RippleIconButton(
        if (!isPlaying) {
            WaveIcons.PlayArrow
        } else {
            WaveIcons.Pause
        },
        modifier = modifier,
        tint = tint,
        onClick = onClick,
    )
}
