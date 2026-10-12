package com.petedev.wavemusic.expect.ui

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

/** Reports predictive-back progress, cancellation, and a completed back action separately. */
@Composable
expect fun PredictiveBackHandler(
    enabled: Boolean = true,
    onProgress: (Float) -> Unit,
    onCancelled: suspend () -> Unit,
    onBack: suspend () -> Unit,
)
