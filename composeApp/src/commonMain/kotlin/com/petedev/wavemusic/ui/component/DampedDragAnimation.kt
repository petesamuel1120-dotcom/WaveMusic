package com.petedev.wavemusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Spring-damped tab indicator: follows the pointer directly while pressed, then settles using
 * the drag velocity. Shared by the regular glass bar and the slim dock's Home/Library capsule.
 */
class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    initialValue: Float,
    val valueRange: ClosedRange<Float>,
    private val visibilityThreshold: Float,
    private val initialScale: Float,
    private val pressedScale: Float,
    private val onDragStarted: DampedDragAnimation.(position: Offset) -> Boolean,
    private val onDragStopped: DampedDragAnimation.() -> Unit,
    private val onDrag: DampedDragAnimation.(size: IntSize, timeMillis: Long, dragAmount: Offset) -> Unit,
) {
    private val valueAnimationSpec = spring(1f, 1000f, visibilityThreshold)
    val selectionAnimationSpec: AnimationSpec<Float> =
        tween(
            durationMillis = BOTTOM_NAV_TRANSITION_DURATION_MILLIS,
            easing = FastOutSlowInEasing,
        )
    private val velocityAnimationSpec = spring(0.5f, 300f, visibilityThreshold * 10f)
    private val pressProgressAnimationSpec = spring(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec = spring(0.6f, 250f, 0.001f)
    private val scaleYAnimationSpec = spring(0.7f, 250f, 0.001f)

    private val valueAnimation = Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation = Animatable(0f, 5f)
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val scaleXAnimation = Animatable(initialScale, 0.001f)
    private val scaleYAnimation = Animatable(initialScale, 0.001f)

    private val mutatorMutex = MutatorMutex()
    private val velocityTracker = VelocityTracker()

    val value: Float get() = valueAnimation.value
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = velocityAnimation.value

    val modifier: Modifier =
        Modifier.pointerInput(Unit) {
            var enabledForGesture = false
            inspectDragGestures(
                onDragStart = { down ->
                    enabledForGesture = onDragStarted(down.position)
                    if (enabledForGesture) press()
                },
                onDragEnd = {
                    if (enabledForGesture) {
                        onDragStopped()
                        release()
                    }
                },
                onDragCancel = {
                    if (enabledForGesture) {
                        onDragStopped()
                        release()
                    }
                },
            ) { change, dragAmount ->
                if (enabledForGesture) onDrag(size, change.uptimeMillis, dragAmount)
            }
        }

    fun press() {
        velocityTracker.resetTracking()
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        animationScope.launch {
            withFrameNanos {}
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
        }
    }

    fun updateValue(value: Float, timeMillis: Long) {
        val target = value.coerceIn(valueRange.start, valueRange.endInclusive)
        animationScope.launch {
            // Snap to each pointer sample so the blob stays under the finger instead of chasing it.
            valueAnimation.snapTo(target)
            updateVelocity(timeMillis)
        }
    }

    suspend fun animateToValue(
        value: Float,
        animationSpec: AnimationSpec<Float> = valueAnimationSpec,
    ) {
        mutatorMutex.mutate {
            val target = value.coerceIn(valueRange.start, valueRange.endInclusive)
            coroutineScope {
                launch { valueAnimation.animateTo(target, animationSpec) }
                if (velocity != 0f) {
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                }
            }
        }
    }

    private suspend fun updateVelocity(timeMillis: Long) {
        velocityTracker.addPosition(timeMillis, Offset(value, 0f))
        val range = valueRange.endInclusive - valueRange.start
        if (range <= 0f) return
        val targetVelocity = velocityTracker.calculateVelocity().x / range
        velocityAnimation.snapTo(targetVelocity)
    }
}
