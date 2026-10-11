@file:Suppress("ktlint:standard:filename")

package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

enum class VoiceSearchState {
    IDLE,
    REQUESTING_PERMISSION,
    LISTENING,
    PROCESSING,
    PERMISSION_DENIED,
    UNAVAILABLE,
    ERROR,
}

interface VoiceSearchLauncher {
    fun launch()
    fun stop()
    fun cancel()
}

@Composable
expect fun rememberVoiceSearchLauncher(
    onResult: (String) -> Unit,
    onPartialResult: (String) -> Unit,
    onStateChange: (VoiceSearchState) -> Unit,
): VoiceSearchLauncher
