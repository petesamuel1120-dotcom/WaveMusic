package com.petedev.wavemusic.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

internal const val SLIDE_V2_ENTER_MILLIS = 280
internal const val SLIDE_V2_EXIT_MILLIS = 300
internal const val SLIDE_V2_FADE_IN_MILLIS = 180
internal const val SLIDE_V2_FADE_IN_DELAY_MILLIS = 20
internal const val SLIDE_V2_FADE_OUT_MILLIS = 170

internal data class SlideV2TitleTransition(val alpha: State<Float>, val inProgress: Boolean)

/** Titles lifted into the fixed header must follow the destination's own visibility transition. */
@Composable
internal fun slideV2TitleTransition(scope: AnimatedVisibilityScope): SlideV2TitleTransition {
    val alpha = scope.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                tween(SLIDE_V2_FADE_IN_MILLIS, delayMillis = SLIDE_V2_FADE_IN_DELAY_MILLIS, easing = LinearOutSlowInEasing)
            } else {
                tween(SLIDE_V2_FADE_OUT_MILLIS, easing = FastOutSlowInEasing)
            }
        },
        label = "slideV2PageTitleAlpha",
    ) { if (it == EnterExitState.Visible) 1f else 0f }
    return SlideV2TitleTransition(alpha, scope.transition.currentState != scope.transition.targetState || scope.transition.isRunning)
}

/** Half-viewport travel; opacity settles sooner so the last part of the slide stays readable. */
internal fun slideV2InitialOffset(widthPx: Int, movingForward: Boolean): Int {
    val distance = widthPx.coerceAtLeast(0) / 2
    return if (movingForward) distance else -distance
}

/** Tab motion follows the navbar; other routes follow the actual push/pop operation. */
internal fun slideV2MovingForward(initialTabOrder: Int?, targetTabOrder: Int?, isPop: Boolean): Boolean =
    if (initialTabOrder != null && targetTabOrder != null && initialTabOrder != targetTabOrder) {
        targetTabOrder > initialTabOrder
    } else {
        !isPop
    }

internal fun slideV2EnterTransition(
    movingForward: Boolean,
    viewportWidthPx: Int? = null,
): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(SLIDE_V2_ENTER_MILLIS, easing = LinearOutSlowInEasing),
        initialOffsetX = { slideV2InitialOffset(viewportWidthPx ?: it, movingForward) },
    ) + slideV2FadeIn()

internal fun slideV2ExitTransition(
    movingForward: Boolean,
    viewportWidthPx: Int? = null,
): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(SLIDE_V2_EXIT_MILLIS, easing = FastOutLinearInEasing),
        targetOffsetX = { -slideV2InitialOffset(viewportWidthPx ?: it, movingForward) },
    ) + slideV2FadeOut()

internal fun slideV2FadeIn(): EnterTransition =
    fadeIn(tween(SLIDE_V2_FADE_IN_MILLIS, delayMillis = SLIDE_V2_FADE_IN_DELAY_MILLIS, easing = LinearOutSlowInEasing))

internal fun slideV2FadeOut(): ExitTransition = fadeOut(tween(SLIDE_V2_FADE_OUT_MILLIS, easing = FastOutSlowInEasing))
