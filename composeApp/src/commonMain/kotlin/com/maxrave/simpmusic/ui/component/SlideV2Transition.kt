package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

internal const val SLIDE_V2_ENTER_MILLIS = 200
internal const val SLIDE_V2_EXIT_MILLIS = 150

/** A small directional nudge, not a full-width page sweep. No spring, scaling or blur. */
internal fun slideV2InitialOffset(widthPx: Int, movingForward: Boolean): Int {
    val distance = widthPx.coerceAtLeast(0) / 8
    return if (movingForward) distance else -distance
}

internal fun slideV2EnterTransition(movingForward: Boolean, viewportWidthPx: Int? = null): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(SLIDE_V2_ENTER_MILLIS, easing = LinearOutSlowInEasing),
        initialOffsetX = { slideV2InitialOffset(viewportWidthPx ?: it, movingForward) },
    ) + slideV2FadeIn()

internal fun slideV2ExitTransition(movingForward: Boolean, viewportWidthPx: Int? = null): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(SLIDE_V2_EXIT_MILLIS, easing = FastOutLinearInEasing),
        targetOffsetX = { -slideV2InitialOffset(viewportWidthPx ?: it, movingForward) },
    ) + slideV2FadeOut()

internal fun slideV2FadeIn(): EnterTransition = fadeIn(tween(SLIDE_V2_ENTER_MILLIS, easing = LinearOutSlowInEasing))

internal fun slideV2FadeOut(): ExitTransition = fadeOut(tween(SLIDE_V2_EXIT_MILLIS, easing = FastOutLinearInEasing))
