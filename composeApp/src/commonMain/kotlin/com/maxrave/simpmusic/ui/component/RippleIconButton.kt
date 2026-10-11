package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons

@Composable
fun RippleIconButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    fillMaxSize: Boolean = false,
    tint: Color = Color.White,
    iconOffsetX: Dp = 0.dp,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null,
    showOnyxContainer: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = if (showOnyxContainer && !LocalGlassControlContainer.current) modifier.onyxControlSurface(CircleShape) else modifier,
        shape = CircleShape,
    ) {
        Icon(
            imageVector,
            contentDescription,
            tint = tint,
            modifier =
                if (fillMaxSize) {
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
            SimpIcons.PlayArrow
        } else {
            SimpIcons.Pause
        },
        modifier = modifier,
        tint = tint,
        onClick = onClick,
    )
}
