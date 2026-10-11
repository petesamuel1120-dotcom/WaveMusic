package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.maxrave.simpmusic.ui.theme.LocalReduceVisualEffects

/** Restored playback is already visible; only a new hidden-to-visible player animates in. */
@Composable
internal fun rememberMiniPlayerVisibility(visible: Boolean): MutableTransitionState<Boolean> {
    val state = remember { MutableTransitionState(visible) }
    state.targetState = visible
    return state
}

/** Reveal the complete card through drawing, with no height expansion or content clipping. */
@Composable
internal fun miniPlayerEnterTransition(): EnterTransition =
    if (LocalReduceVisualEffects.current) {
        fadeIn(tween(150))
    } else {
        fadeIn(tween(240, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 4 }
    }
