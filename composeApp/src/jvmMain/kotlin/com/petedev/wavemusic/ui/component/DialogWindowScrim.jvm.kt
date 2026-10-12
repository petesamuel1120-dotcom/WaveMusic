package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

@Composable
internal actual fun DialogWindowScrim(opacity: () -> Float, fullScreen: Boolean) {
    val tint = MaterialTheme.colorScheme.surfaceTint
    Box(
        Modifier.fillMaxSize().drawBehind {
            val reveal = opacity().coerceIn(0f, 1f)
            drawRect(Color.Black.copy(alpha = 0.18f * reveal))
            drawRect(tint.copy(alpha = 0.035f * reveal))
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun animatedDialogProperties(original: DialogProperties, fullScreen: Boolean) = DialogProperties(
    dismissOnBackPress = original.dismissOnBackPress,
    dismissOnClickOutside = false,
    usePlatformDefaultWidth = false,
    usePlatformInsets = original.usePlatformInsets,
    useSoftwareKeyboardInset = original.useSoftwareKeyboardInset,
    scrimColor = Color.Transparent,
    animateTransition = false,
)
