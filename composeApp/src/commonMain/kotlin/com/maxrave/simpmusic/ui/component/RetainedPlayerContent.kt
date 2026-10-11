package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

/** A retained player may keep its state and decoder without updating an invisible screen. */
val LocalPlayerVisualsActive = staticCompositionLocalOf { true }

/** Do not keep a frame clock running for a hidden visual or an inactive crossfade label. */
@Composable
internal fun rememberVisiblePlayerFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: InfiniteRepeatableSpec<Float>,
    label: String,
    enabled: Boolean = true,
): State<Float> {
    if (!enabled || !LocalPlayerVisualsActive.current) {
        return remember(initialValue) { mutableStateOf(initialValue) }
    }
    return rememberInfiniteTransition(label = label).animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label,
    )
}

private class RetainedPlayerLifecycleOwner : LifecycleOwner {
    override val lifecycle = LifecycleRegistry(this)
}

@Composable
internal fun RetainedPlayerContent(active: Boolean, content: @Composable () -> Unit) {
    val parentLifecycle = LocalLifecycleOwner.current.lifecycle
    val parentState by parentLifecycle.currentStateFlow.collectAsState()
    val visualsActive = active && parentState.isAtLeast(Lifecycle.State.STARTED)
    val owner = remember(parentLifecycle) { RetainedPlayerLifecycleOwner() }
    DisposableEffect(parentLifecycle, owner, active) {
        fun synchronize() {
            val parentState = parentLifecycle.currentState
            owner.lifecycle.currentState =
                if (active || parentState < Lifecycle.State.STARTED) parentState
                else Lifecycle.State.CREATED
        }
        val observer = LifecycleEventObserver { _, _ -> synchronize() }
        parentLifecycle.addObserver(observer)
        synchronize()
        onDispose { parentLifecycle.removeObserver(observer) }
    }
    DisposableEffect(owner) {
        onDispose { owner.lifecycle.currentState = Lifecycle.State.DESTROYED }
    }
    CompositionLocalProvider(
        LocalLifecycleOwner provides owner,
        LocalPlayerVisualsActive provides visualsActive,
        content = content,
    )
}
