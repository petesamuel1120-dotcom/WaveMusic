package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BasicAlertDialogOverride
import androidx.compose.material3.BasicAlertDialogOverrideScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.DefaultBasicAlertDialogOverride
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ComponentOverrideApi
import androidx.compose.material3.LocalBasicAlertDialogOverride
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations
import kotlin.math.min
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** An opener is consumed once; old clicks and scroll gestures never anchor unrelated dialogs. */
internal class DialogOriginTracker {
    private var origin: DialogMotionOrigin? = null
    private var recordedAt: TimeMark? = null
    private val activeDialogs = mutableStateListOf<DialogMotionState>()

    fun attach(motion: DialogMotionState) { activeDialogs.add(motion) }
    fun detach(motion: DialogMotionState) { activeDialogs.remove(motion) }

    // The app blurs behind every dialog. A dialog window only blurs behind its children,
    // so its own surface stays sharp and dismissing a child leaves its parent's blur intact.
    fun backgroundProgress(behind: DialogMotionState?): Float = activeDialogs.maxOfOrNull { motion ->
        var ancestor = motion.parent
        while (ancestor != null && ancestor !== behind) ancestor = ancestor.parent
        if (!motion.nativeBackdropBlur && (behind == null || ancestor === behind)) motion.progress.value else 0f
    } ?: 0f

    fun record(bounds: Rect, source: DialogMotionSourceState? = null, cornerRadiusPx: Float? = null) {
        origin = DialogMotionOrigin(bounds, source, cornerRadiusPx)
        recordedAt = TimeSource.Monotonic.markNow()
    }

    fun clear() {
        origin = null
        recordedAt = null
    }

    fun takeRecent(): DialogMotionOrigin? {
        val recent = recordedAt?.elapsedNow()?.inWholeMilliseconds?.let { it < 1200 } == true
        return origin.takeIf { recent }.also { clear() }
    }
}

internal data class DialogMotionOrigin(
    val bounds: Rect,
    val source: DialogMotionSourceState? = null,
    val cornerRadiusPx: Float? = null,
) {
    val center: Offset get() = bounds.center
}

/** Transfer the opener to its dialog without drawing two copies of translucent controls. */
internal class DialogMotionSourceState {
    var motion: DialogMotionState? by mutableStateOf(null)
    var visual: GraphicsLayer? = null
    val opacity: Float get() = if (motion?.let { it.ready && !it.returnComplete } == true) 0f else 1f
}

/** Reuse the control's display list during the handoff; no bitmap capture or per-frame layout. */
@Composable
private fun rememberDialogSourceVisual(
    source: DialogMotionSourceState,
    feedbackModifier: Modifier = Modifier,
): Modifier {
    val visual = rememberGraphicsLayer()
    DisposableEffect(source, visual) {
        source.visual = visual
        onDispose { if (source.visual === visual) source.visual = null }
    }
    return remember(source, visual, feedbackModifier) {
        Modifier.graphicsLayer { alpha = source.opacity }.then(feedbackModifier).drawWithContent {
            // Record the control's content before opacity, but keep transient touch feedback
            // outside its display list. Hidden layers can retain their last pressed frame.
            visual.record { this@drawWithContent.drawContent() }
            drawLayer(visual)
        }
    }
}

internal val LocalDialogOrigins = staticCompositionLocalOf<DialogOriginTracker?> { null }
internal val LocalDialogMotionOwner = staticCompositionLocalOf<DialogMotionState?> { null }
private val LocalDialogCloseAction = staticCompositionLocalOf<((() -> Unit) -> Unit)?> { null }
private val LocalDialogIsClosing = staticCompositionLocalOf { false }

/** Blur the window behind an expanding dialog using the same spring, without remeasuring it. */
internal fun Modifier.dialogBackdropBlur(): Modifier = composed {
    val origins = LocalDialogOrigins.current ?: return@composed this
    val owner = LocalDialogMotionOwner.current
    val density = LocalDensity.current.density
    // Android's GPU effect requires API 31. Older devices retain the animated dim scrim.
    val supported = remember { BlurEffect(1f, 1f).isSupported() }
    if (!supported) return@composed this
    val layer = rememberGraphicsLayer()
    drawWithContent {
        // Observe progress in drawing, even while another native window has focus. Record all
        // nested glass/display layers into the same effect rather than blurring an empty overlay.
        val radius = dialogBackdropBlurRadius(origins.backgroundProgress(owner), density)
        if (radius <= 0.01f) {
            layer.renderEffect = null
            drawContent()
        } else {
            layer.renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
        }
    }
}

internal fun dialogBackdropBlurRadius(progress: Float, density: Float): Float =
    32f * density * dialogRevealOpacity(progress, 0f, 0.82f)

/** Record the control at activation, including keyboard and accessibility clicks. */
internal class DialogMotionAnchor(
    private val origins: DialogOriginTracker?,
    private val cornerRadiusPx: Float,
    private val source: DialogMotionSourceState,
    sourceModifier: Modifier,
    private val enabled: Boolean,
) {
    private var coordinates: LayoutCoordinates? = null

    val isClosing: Boolean get() = source.motion?.closing == true

    val modifier: Modifier = sourceModifier
        .onGloballyPositioned { coordinates = it }

    fun record() {
        if (!enabled) {
            origins?.clear()
            return
        }
        val current = coordinates?.takeIf { it.isAttached }
        if (current == null) {
            origins?.clear()
        } else {
            origins?.record(Rect(current.localToScreen(Offset.Zero), current.size.toSize()), source, cornerRadiusPx)
        }
    }
}

@Composable
internal fun rememberDialogMotionAnchor(cornerRadius: Dp = 16.dp): DialogMotionAnchor {
    val origins = LocalDialogOrigins.current
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    val enabled = LocalAdvancedDialogAnimations.current
    val source = remember { DialogMotionSourceState() }
    val sourceModifier = if (enabled) rememberDialogSourceVisual(source) else Modifier
    return remember(origins, radiusPx, sourceModifier, enabled) {
        DialogMotionAnchor(origins, radiusPx, source, sourceModifier, enabled)
    }
}

/** Observe releases before click handlers, without consuming any input or changing scrolling. */
internal fun Modifier.dialogMotionSource(
    clearPreviousOrigin: Boolean = false,
    captureBounds: Boolean = false,
): Modifier = composed {
    val origins = LocalDialogOrigins.current
    val animationsEnabled = LocalAdvancedDialogAnimations.current
    var coordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    if (origins == null) return@composed this
    // Sheets and popup menus receive input in their own window, outside App's observer.
    // Entering one invalidates its opener's tap, including keyboard/accessibility entry.
    DisposableEffect(origins, clearPreviousOrigin) {
        if (clearPreviousOrigin) origins.clear()
        onDispose {}
    }
    val sourceState = remember { DialogMotionSourceState() }
    val sourceModifier = if (captureBounds && animationsEnabled) rememberDialogSourceVisual(sourceState) else Modifier
    // These roots belong to sheet/popup windows, which the main app's layer cannot blur.
    val backdropModifier = if (clearPreviousOrigin) Modifier.dialogBackdropBlur() else Modifier
    then(backdropModifier).then(sourceModifier).onGloballyPositioned { coordinates = it }.pointerInput(origins, captureBounds) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            // A canceled gesture must not leave an earlier tap eligible for the next dialog.
            origins.clear()
            var moved = false
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                moved = moved || event.changes.size != 1 || event.changes.any {
                    (it.position - down.position).getDistance() > viewConfiguration.touchSlop
                }
            } while (event.changes.any { it.pressed })
            if (moved) {
                origins.clear()
            } else if (captureBounds && animationsEnabled) {
                coordinates?.takeIf { it.isAttached }?.let { source ->
                    // A control always opens from its own center, regardless of the tap position.
                    origins.record(Rect(source.localToScreen(Offset.Zero), source.size.toSize()), sourceState)
                }
            }
        }
    }
}

/** Preference rows anchor the dialog by bounds, keeping their live content in the page. */
internal fun Modifier.dialogMotionClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val origins = LocalDialogOrigins.current
    val animationsEnabled = LocalAdvancedDialogAnimations.current
    var coordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    // Do not draw an activity-owned GraphicsLayer in a separate Android dialog window.
    // Keeping this row live avoids a retained snapshot/pressed frame during dismissal.
    onGloballyPositioned { coordinates = it }.clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
    ) {
        // Resolve the screen bounds at activation, so scrolling and resizing never leave a stale source.
        val sourceBounds = coordinates?.takeIf { it.isAttached }?.let {
            Rect(it.localToScreen(Offset.Zero), it.size.toSize())
        }
        if (animationsEnabled && sourceBounds != null) origins?.record(sourceBounds) else origins?.clear()
        onClick()
    }
}

internal class DialogMotionState(val origin: DialogMotionOrigin?, val parent: DialogMotionState? = null) {
    // Native Android window blur replaces the Compose fallback for this dialog only.
    var nativeBackdropBlur by mutableStateOf(false)
    val progress = Animatable(0f)
    var ready by mutableStateOf(false)
    var closing by mutableStateOf(false)
    var returnComplete by mutableStateOf(false)
    var closeVelocity = 0f
    var closeAction: (() -> Unit)? = null
}

/** Native dialog layout stays still, so IME placement and touch targets don't jump during entry. */
@Composable
internal fun AppDialogMotion(
    onDismissRequest: () -> Unit,
    content: @Composable (DialogMotionState, () -> Unit) -> Unit,
) {
    val origins = LocalDialogOrigins.current
    val parent = LocalDialogMotionOwner.current
    val motion = remember(origins, parent) { DialogMotionState(origins?.takeRecent(), parent) }
    DisposableEffect(origins, motion) {
        val source = motion.origin?.source
        source?.motion = motion
        origins?.attach(motion)
        onDispose {
            origins?.detach(motion)
            if (source?.motion === motion) source.motion = null
        }
    }
    val latestDismiss by rememberUpdatedState(onDismissRequest)
    val close: (() -> Unit) -> Unit = remember(motion) {
        { action ->
            if (!motion.closing) {
                motion.closeVelocity = motion.progress.velocity
                motion.closeAction = action
                motion.closing = true
            }
        }
    }
    LaunchedEffect(motion.ready, motion.closing) {
        if (motion.ready) {
            // One spring drives the whole handoff. Capture entry velocity before canceling it,
            // so a quick Back/outside tap reverses continuously from the current frame.
            motion.progress.animateTo(
                if (motion.closing) 0f else 1f,
                spring(dampingRatio = 1f, stiffness = if (motion.closing) 240f else 200f, visibilityThreshold = 0.003f),
                initialVelocity = if (motion.closing) motion.closeVelocity else 0f,
            )
            if (motion.closing) {
                // Restore the live opener and clear the traveling layer while the window is
                // still alive. Android dismisses Dialog before disposing its composition, so
                // relying on disposal alone can leave the last opaque frame on the screen.
                motion.returnComplete = true
                withFrameNanos { }
                withFrameNanos { }
                val action = motion.closeAction
                motion.closeAction = null
                action?.invoke()
                // Let a StateFlow dismissal reach composition before deciding the caller kept
                // the dialog open. A coroutine yield can restart entry during the same frame.
                // Removing this dialog cancels the effect at these frame boundaries.
                withFrameNanos { }
                withFrameNanos { }
                // A callback may keep a dialog open (for example, validation). Restore it then.
                motion.returnComplete = false
                motion.closing = false
            }
        }
    }
    CompositionLocalProvider(
        LocalDialogMotionOwner provides motion,
        LocalDialogCloseAction provides close,
        LocalDialogIsClosing provides motion.closing,
    ) {
        content(motion) { close { latestDismiss() } }
    }
}

internal fun dialogTravelOffset(origin: Offset?, bounds: Rect?, progress: Float): Offset {
    if (origin == null || bounds == null) return Offset.Zero
    val fraction = progress.coerceIn(0f, 1f)
    val travel = origin - bounds.center
    val arrival = dialogRevealOpacity(fraction, 0f, 0.94f)
    // A shallow upward arc adds lift while still landing exactly on both measured bounds.
    val lift = min(bounds.height * 0.022f, travel.getDistance() * 0.07f)
    return travel * (1f - arrival) + Offset(0f, -lift * 4f * fraction * (1f - fraction))
}

internal data class DialogMorphFrame(
    val scaleX: Float,
    val scaleY: Float,
    val contentScale: Float,
    val contentOffsetY: Float,
    val travel: Offset,
    val contentOpacity: Float,
    val sourceOpacity: Float,
    val containerOpacity: Float,
)

/** Fit the whole content into the unfolding surface instead of cropping full-size rows. */
internal fun dialogMorphFrame(origin: DialogMotionOrigin?, bounds: Rect?, progress: Float): DialogMorphFrame {
    val fraction = progress.coerceIn(0f, 1f)
    val source = origin?.bounds?.takeIf { it.width > 0f && it.height > 0f }
    val target = bounds?.takeIf { it.width > 0f && it.height > 0f }
    val fallbackScale = 0.90f
    val initialX = if (source != null && target != null) source.width / target.width else fallbackScale
    val initialY = if (source != null && target != null) source.height / target.height else fallbackScale
    // Let the source widen first, then give the surface a longer vertical unfold. Staggered
    // easing keeps the panel attached to its origin without the old elastic overshoot.
    val widthProgress = dialogRevealOpacity(fraction, 0f, 0.72f)
    val heightProgress = dialogRevealOpacity(fraction, 0.035f, 1f)
    val scaleX = initialX + (1f - initialX) * widthProgress
    val scaleY = initialY + (1f - initialY) * heightProgress
    // Fit every edge of the content inside the unfolding shell. Canceling the parent's
    // unequal scales without this fit left full-size titles, options and buttons clipped.
    val contentSettle = dialogRevealOpacity(fraction, 0.15f, 0.72f)
    val contentScale = min(1f, min(scaleX, scaleY)) * (0.96f + 0.04f * contentSettle)
    val targetHeight = target?.height ?: 0f
    return DialogMorphFrame(
        scaleX = scaleX,
        scaleY = scaleY,
        contentScale = contentScale,
        contentOffsetY = targetHeight * 0.01f * (1f - contentSettle),
        travel = dialogTravelOffset(origin?.center, target, fraction),
        // Crossfade the source into fitted content while the shell finishes unfolding.
        contentOpacity = dialogRevealOpacity(fraction, 0.12f, 0.56f),
        sourceOpacity = 1f - dialogRevealOpacity(fraction, 0.06f, 0.32f),
        containerOpacity = dialogRevealOpacity(fraction, if (origin?.source != null) 0.06f else 0f, 0.27f),
    )
}

/** Crossfade as the container opens, with no separately timed animations drifting apart. */
internal fun dialogRevealOpacity(progress: Float, start: Float, end: Float): Float {
    val fraction = ((progress - start) / (end - start)).coerceIn(0f, 1f)
    return fraction * fraction * (3f - 2f * fraction)
}

// An origin without a captured control fades its empty shell in before the content unfolds.
private fun dialogSurfaceReveal(progress: Float): Float = dialogRevealOpacity(progress, 0f, 0.045f)

internal enum class DialogContentStage { Title, Body, Actions, Option }

/** Stagger dialog sections on the shared, reversible spring without remeasuring content. */
@Composable
internal fun Modifier.dialogContentMotion(stage: DialogContentStage, index: Int = 0): Modifier {
    val motion = LocalDialogMotionOwner.current
    if (motion == null || !LocalAdvancedDialogAnimations.current) return this
    val rise = with(LocalDensity.current) { 8.dp.toPx() }
    val delay = index.coerceIn(0, 6) * 0.018f
    val (start, end) = when (stage) {
        DialogContentStage.Title -> 0.18f to 0.48f
        DialogContentStage.Body -> 0.23f to 0.59f
        DialogContentStage.Actions -> 0.31f to 0.68f
        DialogContentStage.Option -> (0.18f + delay) to (0.53f + delay)
    }
    return graphicsLayer {
        val reveal = dialogRevealOpacity(motion.progress.value, start, end)
        alpha = reveal
        translationY = rise * (1f - reveal)
    }
}

@Composable
internal fun DialogMotionContent(stage: DialogContentStage, content: @Composable () -> Unit) {
    if (LocalDialogMotionOwner.current == null || !LocalAdvancedDialogAnimations.current) {
        content()
    } else {
        Box(Modifier.dialogContentMotion(stage), propagateMinConstraints = true) { content() }
    }
}

@Composable
internal fun DialogMotionSurface(
    motion: DialogMotionState,
    modifier: Modifier = Modifier,
    fullScreen: Boolean = false,
    content: @Composable () -> Unit,
) {
    var bounds: Rect? by remember { mutableStateOf(null) }
    val palette = rememberAppDialogPalette()
    Box(
        (if (fullScreen) Modifier.fillMaxSize() else Modifier).onGloballyPositioned {
            // Measure the stationary parent, never the animated child: no transform feedback.
            val measured = Rect(it.localToScreen(Offset.Zero), it.size.toSize())
            if (measured.width > 0f && measured.height > 0f) {
                bounds = measured
                // Keep entry at its source until the native dialog has its real destination.
                motion.ready = true
            }
        }.dialogMotionSource().pointerInput(Unit) {},
        propagateMinConstraints = true,
    ) {
        Box(
            Modifier.graphicsLayer {
                val progress = motion.progress.value.coerceIn(0f, 1f)
                val frame = dialogMorphFrame(motion.origin, bounds, progress)
                scaleX = frame.scaleX
                scaleY = frame.scaleY
                translationX = frame.travel.x
                translationY = frame.travel.y
                // Draw only the traveling copy until the return completes. Complementary
                // fades darken opaque content halfway through, while overlapping translucent
                // backgrounds brighten the control before it returns to its live layer.
                alpha = if (bounds == null || motion.returnComplete) {
                    0f
                } else if (motion.origin?.source != null) {
                    1f
                } else {
                    dialogSurfaceReveal(progress)
                }
                shadowElevation = 12.dp.toPx() * dialogRevealOpacity(progress, 0.06f, 0.52f)
                shape = DialogMorphShape(progress, frame.scaleX, frame.scaleY, fullScreen, motion.origin?.cornerRadiusPx)
                clip = true
            }.drawBehind {
                val frame = dialogMorphFrame(motion.origin, bounds, motion.progress.value)
                drawRect(palette.container.copy(alpha = frame.containerOpacity))
            }.pointerInput(motion.closing) {
                if (motion.closing) {
                    awaitPointerEventScope {
                        while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            },
            propagateMinConstraints = true,
        ) {
            // Dialog borders/background modifiers belong to the unfolding shell, not the
            // transparent opener. Fade them out before handing the settings row back.
            Box(
                Modifier.graphicsLayer {
                    alpha = dialogMorphFrame(motion.origin, bounds, motion.progress.value).containerOpacity
                }.then(modifier),
                propagateMinConstraints = true,
            ) {
                Box(
                    Modifier.graphicsLayer {
                        val progress = motion.progress.value.coerceIn(0f, 1f)
                        val frame = dialogMorphFrame(motion.origin, bounds, progress)
                        // Cancel the parent's unequal scales; the content keeps its proportions.
                        // Layout stays at its final size; text never reflows or stretches per frame.
                        scaleX = frame.contentScale / frame.scaleX
                        scaleY = frame.contentScale / frame.scaleY
                        translationY = frame.contentOffsetY
                        // Match the container's pivot so every row contracts together on return.
                        transformOrigin = TransformOrigin.Center
                        // Preserve the content fade while its enclosing decorations fade separately.
                        alpha = if (frame.containerOpacity > 0f) frame.contentOpacity / frame.containerOpacity else 0f
                    },
                    propagateMinConstraints = true,
                ) { content() }
            }
            Box(
                Modifier.matchParentSize().graphicsLayer {
                    alpha = dialogMorphFrame(motion.origin, bounds, motion.progress.value).sourceOpacity
                }.drawWithContent {
                    val visual = motion.origin?.source?.visual ?: return@drawWithContent
                    if (visual.size.width == 0 || visual.size.height == 0) return@drawWithContent
                    val frame = dialogMorphFrame(motion.origin, bounds, motion.progress.value)
                    val width = size.width * frame.scaleX
                    val height = size.height * frame.scaleY
                    // The opener's text/icon scales uniformly, even when the panel changes aspect ratio.
                    val sourceScale = min(1f, min(width / visual.size.width, height / visual.size.height))
                    val left = (width - visual.size.width * sourceScale) / 2f
                    val top = (height - visual.size.height * sourceScale) / 2f
                    withTransform({
                        translate(left / frame.scaleX, top / frame.scaleY)
                        scale(sourceScale / frame.scaleX, sourceScale / frame.scaleY, pivot = Offset.Zero)
                    }) { drawLayer(visual) }
                },
            )
        }
    }
}

/** Clip the surface with elliptical corners so they stay round while the source bounds scale. */
private class DialogMorphShape(
    private val progress: Float,
    private val scaleX: Float,
    private val scaleY: Float,
    private val fullScreen: Boolean,
    private val sourceCornerRadiusPx: Float?,
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density,
    ): Outline {
        val fraction = progress.coerceIn(0f, 1f)
        val endCornerRadius = if (fullScreen) 0.dp else 24.dp
        val cornerProgress = dialogRevealOpacity(fraction, 0.02f, 0.92f)
        val cornerRadius = with(density) {
            (sourceCornerRadiusPx ?: 16.dp.toPx()) * (1f - cornerProgress) +
                endCornerRadius.toPx() * cornerProgress
        }
        val cornerX = (cornerRadius / scaleX.coerceAtLeast(0.001f)).coerceAtMost(size.width / 2f)
        val cornerY = (cornerRadius / scaleY.coerceAtLeast(0.001f)).coerceAtMost(size.height / 2f)
        val corner = CornerRadius(cornerX, cornerY)
        return Outline.Rounded(
            RoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                topLeftCornerRadius = corner,
                topRightCornerRadius = corner,
                bottomRightCornerRadius = corner,
                bottomLeftCornerRadius = corner,
            ),
        )
    }
}

/** A transparent full-window stage lets the surface travel outside its final popup bounds. */
@Composable
private fun DialogMotionViewport(
    motion: DialogMotionState,
    properties: DialogProperties,
    onDismissRequest: () -> Unit,
    fullScreen: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val latestDismiss by rememberUpdatedState(onDismissRequest)
    Box(Modifier.fillMaxSize().dialogBackdropBlur()) {
        DialogWindowScrim(opacity = { motion.progress.value }, fullScreen = fullScreen)
        Box(
            // A pointer-only backdrop never becomes an unnamed accessibility/tab-focus target.
            Modifier.matchParentSize().pointerInput(properties.dismissOnClickOutside, motion.closing) {
                if (properties.dismissOnClickOutside && !motion.closing) {
                    detectTapGestures(onTap = { latestDismiss() })
                }
            },
        )
        val surfaceModifier =
            if (fullScreen) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .windowInsetsPadding(WindowInsets.ime)
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .widthIn(max = 560.dp)
            }
        Box(surfaceModifier) {
            DialogMotionSurface(motion, modifier, fullScreen, content)
        }
    }
}

/** Preserve Material's sizing, localized dialog semantics, focus trapping and native back handling. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ComponentOverrideApi::class)
internal object AnimatedAppAlertDialogOverride : BasicAlertDialogOverride {
    @Composable
    override fun BasicAlertDialogOverrideScope.BasicAlertDialog() {
        val dialogModifier = modifier
        val dialogContent = content
        AppDialogMotion(onDismissRequest) { motion, animatedDismiss ->
            CompositionLocalProvider(LocalBasicAlertDialogOverride provides DefaultBasicAlertDialogOverride) {
                BasicAlertDialog(
                    onDismissRequest = animatedDismiss,
                    modifier = Modifier.fillMaxSize(),
                    properties = animatedDialogProperties(properties),
                ) {
                    DialogMotionViewport(
                        motion,
                        properties,
                        animatedDismiss,
                        modifier = dialogModifier.sizeIn(minWidth = 280.dp, maxWidth = 560.dp),
                    ) {
                        // Nested alerts inherit the same animation rather than the native fallback.
                        CompositionLocalProvider(LocalBasicAlertDialogOverride provides AnimatedAppAlertDialogOverride) {
                            dialogContent()
                        }
                    }
                }
            }
        }
    }
}

/** The same motion for custom dialog surfaces (chart picker, cropper, member actions). */
@Composable
fun AnimatedAppDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    fullScreen: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (!LocalAdvancedDialogAnimations.current) {
        Dialog(onDismissRequest = onDismissRequest, properties = properties) {
            content()
        }
        return
    }
    AppDialogMotion(onDismissRequest) { motion, animatedDismiss ->
        Dialog(onDismissRequest = animatedDismiss, properties = animatedDialogProperties(properties, fullScreen)) {
            DialogMotionViewport(motion, properties, animatedDismiss, fullScreen, content = content)
        }
    }
}

/** Run dismissing actions after the short reverse animation; outside a dialog, act immediately. */
@Composable
fun DialogTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val close = LocalDialogCloseAction.current
    TextButton(
        onClick = { if (close != null) close(onClick) else onClick() },
        modifier = modifier,
        enabled = enabled && !LocalDialogIsClosing.current,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
internal fun rememberDialogCloseAction(): ((() -> Unit) -> Unit) =
    LocalDialogCloseAction.current ?: { action -> action() }
