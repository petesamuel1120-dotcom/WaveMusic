package com.petedev.wavemusic.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MutableFloatState
import com.petedev.wavemusic.domain.manager.DataStoreManager

internal suspend fun cancelSettingsSubpageBack(progress: MutableFloatState) {
    animate(
        initialValue = progress.floatValue,
        targetValue = 0f,
        animationSpec = tween(220),
    ) { value, _ -> progress.floatValue = value }
}

internal suspend fun finishSettingsSubpageBack(
    transitionStyle: String,
    progress: MutableFloatState,
    onPredictiveCommit: () -> Unit,
    onBack: () -> Unit,
) {
    // A back button has no gesture progress. Let AnimatedContent run the selected
    // Slide v2 or Fade return instead of substituting the full-width gesture slide.
    if (transitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE || progress.floatValue > 0f) {
        val animationSpec = when (transitionStyle) {
            DataStoreManager.PAGE_TRANSITION_SLIDE_V2 -> tween<Float>(SLIDE_V2_ENTER_MILLIS, easing = LinearOutSlowInEasing)
            DataStoreManager.PAGE_TRANSITION_SLIDE -> tween<Float>(180)
            else -> tween<Float>(260)
        }
        animate(
            initialValue = progress.floatValue,
            targetValue = 1f,
            animationSpec = animationSpec,
        ) { value, _ -> progress.floatValue = value }
        onPredictiveCommit()
    }
    onBack()
}

internal fun settingsBackTranslationX(
    transitionStyle: String,
    widthPx: Int,
    progress: Float,
    entering: Boolean,
): Float {
    val distance = when (transitionStyle) {
        DataStoreManager.PAGE_TRANSITION_SLIDE_V2 -> slideV2InitialOffset(widthPx, movingForward = true)
        DataStoreManager.PAGE_TRANSITION_SLIDE -> widthPx.coerceAtLeast(0)
        else -> 0
    }
    val fraction = progress.coerceIn(0f, 1f)
    return if (entering) -distance * (1f - fraction) else distance * fraction
}

internal fun settingsBackAlpha(transitionStyle: String, progress: Float, entering: Boolean): Float {
    if (transitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) return 1f
    val fraction = progress.coerceIn(0f, 1f)
    return if (entering) fraction else 1f - fraction
}
