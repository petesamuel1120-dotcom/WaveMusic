package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/** Read animation state in the draw layer, keeping the dialog's composition and layout still. */
@Composable
internal expect fun DialogWindowScrim(opacity: () -> Float, fullScreen: Boolean = false)

internal expect fun animatedDialogProperties(original: DialogProperties, fullScreen: Boolean = false): DialogProperties
