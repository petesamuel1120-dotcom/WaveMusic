package com.petedev.wavemusic.expect.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {}

@Composable
actual fun PredictiveBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onCancelled: suspend () -> Unit,
    onBack: suspend () -> Unit,
) {}
