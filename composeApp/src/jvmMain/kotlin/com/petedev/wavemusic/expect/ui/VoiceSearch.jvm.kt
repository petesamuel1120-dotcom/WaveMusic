package com.petedev.wavemusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
@Suppress("UNUSED_PARAMETER")
actual fun rememberVoiceSearchLauncher(
    onResult: (String) -> Unit,
    onPartialResult: (String) -> Unit,
    onStateChange: (VoiceSearchState) -> Unit,
): VoiceSearchLauncher =
    remember {
        object : VoiceSearchLauncher {
            override fun launch() = Unit
            override fun stop() = Unit
            override fun cancel() = Unit
        }
    }
