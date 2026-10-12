package com.petedev.wavemusic.expect.ui

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler as AndroidPredictiveBackHandler
import androidx.compose.runtime.Composable
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled = enabled, onBack = onBack)
}

@OptIn(ExperimentalActivityApi::class)
@Composable
actual fun PredictiveBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onCancelled: suspend () -> Unit,
    onBack: suspend () -> Unit,
) {
    AndroidPredictiveBackHandler(enabled = enabled) { progress ->
        try {
            progress.collect {
                onProgress(it.progress)
            }
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { onCancelled() }
            throw cancelled
        }
        onBack()
    }
}
