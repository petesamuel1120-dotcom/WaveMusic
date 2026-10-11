package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangularShape
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassAppearance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sign

/** A consistent reflective rim for small circles and wide pills. */
private val GlassControlHighlight = Highlight.Default.copy(width = 1.dp)

/** Capture the actual action-row area, with controls outside the source. */
@Composable
fun GlassBackdropBox(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(PlatformBackdrop) -> Unit,
) {
    val backdrop = com.maxrave.simpmusic.expect.ui.rememberBackdrop(backgroundColor)
    Box(modifier) {
        if (LocalLiquidGlassEnabled.current) {
            Box(Modifier.matchParentSize().layerBackdrop(backdrop).background(backgroundColor))
        }
        content(backdrop)
    }
}

/** Compact visual size; Compose retains its minimum touch target for clickable controls. */
@Composable
fun glassActionSize(): Dp = when {
    isCompactLandscapeChrome() -> 36.dp
    LocalLiquidGlassEnabled.current -> 42.dp
    else -> 48.dp
}

/**
 * Applies the aaveMusic liquid-glass effect to any element.
 *
 * This is the single primitive behind the glass buttons that used to be hand-wired
 * inline on every screen. It encapsulates the Kyant `drawBackdrop` effect stack
 * and the press/hold "liquid" interaction (a slight scale-up, deeper
 * refraction and a radial glow that follows the pointer, springing back on release).
 * The press gesture is observe-only, so wrapped click handlers keep working.
 *
 * The element MUST be a sibling of the backdrop source (the box carrying
 * [com.maxrave.simpmusic.expect.ui.layerBackdrop]); nesting it inside the source
 * creates a render-feedback loop that crashes the RuntimeShader.
 *
 * Runs on Android and desktop alike: Kyant's backdrop is a KMP artifact whose desktop
 * variant renders through skiko. The press interaction is driven by `pointerInput`, so
 * a mouse button triggers it exactly like a finger.
 *
 * @param interactive set false for a static glass surface.
 * @param minBlurRadius a legibility floor for large panes such as page headers.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    highlight: Highlight = GlassControlHighlight,
    lensScale: Float = 1f,
    lensAmountScale: Float = 1f,
    minBlurRadius: Dp = 0.dp,
): Modifier {
    // Opaque themed fills and a subtle outline separate flat controls from the header and
    // scrolling content. Applying the fallback here covers both containers and directly drawn
    // controls while retaining their shapes and touch targets.
    if (!LocalLiquidGlassEnabled.current) {
        // Immersive detail screens run inside ForceDarkContent for legibility over artwork. Their
        // disabled-glass controls should still use the app's actual theme, not that forced scheme.
        return this
            .clip(shape)
            .background(flatChromeControlBackground())
            .border(1.dp, flatChromeControlOutline(), shape)
    }
    // Artwork detail screens force their content palette dark, but the glass controls should
    // follow the selected app theme in both the active-glass and flat-fallback paths.
    val isDark = LocalAppIsDarkTheme.current
    val interaction = rememberGlassInteraction()
    return this.drawInteractiveGlass(
        isDark = isDark,
        backdrop = backdrop,
        luminanceAnimation = 0.5f,
        shape = shape,
        interaction = if (interactive) interaction else null,
        highlight = highlight,
        lensScale = lensScale,
        lensAmountScale = lensAmountScale,
        minBlurRadius = minBlurRadius,
    )
}

/**
 * Overload of [liquidGlass] for surfaces that sample their own background luminance
 * (e.g. the MiniPlayer and the bottom bar capsule): the caller drives
 * [luminanceAnimation], so the glass keeps adapting to the content behind it — unlike
 * the [liquidGlass] above, which uses a fixed mid-luminance.
 *
 * [blurScale], [minScrim] and [maxScrim] forward to [drawInteractiveGlass]; their
 * defaults reproduce the shared look, so only a caller that wants a denser pane
 * (the Desktop capsule) has to name them.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    luminanceAnimation: Float,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    blurScale: Float = 1f,
    minScrim: Float = 0.12f,
    maxScrim: Float = 0.5f,
    highlight: Highlight = GlassControlHighlight,
): Modifier {
    val isDark = LocalIsDarkTheme.current
    val interaction = rememberGlassInteraction()
    return this.drawInteractiveGlass(
        isDark = isDark,
        backdrop = backdrop,
        luminanceAnimation = luminanceAnimation,
        shape = shape,
        interaction = if (interactive) interaction else null,
        highlight = highlight,
        // MiniPlayer (the only caller of this layer + luminance overload) is a wide surface, so the
        // shared 1.12 press scale bulges too hard; use a gentler scale here.
        pressedScale = 1.04f,
        blurScale = blurScale,
        minScrim = minScrim,
        maxScrim = maxScrim,
    )
}

/**
 * A liquid-glass surface wrapping arbitrary [content] (e.g. a pill of icon
 * buttons). Thin convenience over [liquidGlass].
 */
@Composable
fun LiquidGlassContainer(
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    highlight: Highlight = GlassControlHighlight,
    contentAlignment: Alignment = Alignment.Center,
    showFlatContainer: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!LocalLiquidGlassEnabled.current) {
        Box(
            modifier = if (showFlatContainer) modifier.liquidGlass(backdrop, shape, interactive, highlight) else modifier,
            contentAlignment = contentAlignment,
            content = content,
        )
        return
    }
    val isDark = LocalAppIsDarkTheme.current
    val headerSurfaceProgress = LocalHeaderSurfaceProgress.current.coerceIn(0f, 1f)
    val interaction = if (interactive) rememberGlassInteraction() else null
    val appearance = LocalLiquidGlassAppearance.current
    Box(
        modifier = modifier.then(
            if (interaction != null) {
                Modifier.graphicsLayer {
                    val scale = 1f + 0.12f * appearance.motionMultiplier * interaction.pressProgress
                    scaleX = scale
                    scaleY = scale
                }.pointerInput(interaction) { interaction.detectPress(this) }
            } else Modifier,
        ),
        contentAlignment = contentAlignment,
    ) {
        // Tighten the visible rim around content, retaining the original layout and hit targets.
        Box(
            Modifier.matchParentSize()
                .padding(3.dp)
                .graphicsLayer { alpha = 1f - headerSurfaceProgress }
                .drawInteractiveGlass(
                    isDark = isDark,
                    backdrop = backdrop,
                    luminanceAnimation = 0.5f,
                    shape = shape,
                    interaction = interaction,
                    highlight = highlight,
                    pressedScale = 1f,
                    observePress = false,
                ),
        )
        content()
    }
}

/** Shared compact surface for grouped header actions. */
@Composable
fun LiquidGlassButtonGroup(
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    showFlatContainer: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassContainer(backdrop, modifier, shape = RoundedCornerShape(24.dp), showFlatContainer = showFlatContainer) {
        Row(
            Modifier.fillMaxHeight(),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            content = content,
        )
    }
}

/**
 * Convenience wrapper around [LiquidGlassContainer] for the common single-icon
 * case (e.g. the circular back button shared by the detail screens).
 */
@Composable
fun LiquidGlassIconButton(
    backdrop: PlatformBackdrop,
    imageVector: ImageVector,
    modifier: Modifier = Modifier.size(headerGlassControlSize()),
    shape: Shape = CircleShape,
    tint: Color = Color.White,
    interactive: Boolean = true,
    highlight: Highlight = GlassControlHighlight,
    showFlatContainer: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        modifier = modifier,
        shape = shape,
        interactive = interactive,
        highlight = highlight,
        showFlatContainer = showFlatContainer,
    ) {
        RippleIconButton(
            imageVector = imageVector,
            tint = tint,
            contentDescription = contentDescription,
            iconSize = if (isCompactLandscapeChrome()) 22.dp else 24.dp,
            onClick = onClick,
        )
    }
}

/**
 * Press/hold state holder for a single liquid-glass surface.
 *
 * Project-local, dependency-light reimplementation of Kyant's catalog
 * `InteractiveHighlight`: instead of the library-internal AGSL highlight shader
 * (whose public helpers are not exposed in backdrop 2.0.0) we drive a plain
 * [Brush.radialGradient] from [touchPosition] and a spring-animated
 * [pressProgress]. The drag detection is observe-only so wrapped buttons keep
 * receiving their own clicks.
 */
class GlassInteraction(
    private val animationScope: CoroutineScope,
) {
    private val pressSpec = spring(dampingRatio = 0.5f, stiffness = 300f, visibilityThreshold = 0.001f)
    private val pressAnimation = Animatable(0f, 0.001f)

    /** 0f at rest, animating to 1f while pressed. Read in draw/effect/layer blocks. */
    val pressProgress: Float get() = pressAnimation.value

    /** Local-space touch point used as the centre of the press glow. */
    var touchPosition by mutableStateOf(Offset.Zero)
        private set

    suspend fun detectPress(pointer: PointerInputScope) =
        with(pointer) {
            inspectDragGestures(
                onDragStart = { down ->
                    touchPosition = down.position
                    animationScope.launch { pressAnimation.animateTo(1f, pressSpec) }
                },
                onDragEnd = { animationScope.launch { pressAnimation.animateTo(0f, pressSpec) } },
                onDragCancel = { animationScope.launch { pressAnimation.animateTo(0f, pressSpec) } },
            ) { change, _ ->
                touchPosition = change.position
            }
        }
}

@Composable
fun rememberGlassInteraction(): GlassInteraction {
    val scope = rememberCoroutineScope()
    return remember(scope) { GlassInteraction(scope) }
}

/**
 * Draws the liquid-glass effect with the same look as the legacy
 * `drawBackdropCustomShape`, plus an optional press response driven by
 * [interaction]: the surface scales up a touch, the refraction/blur deepen and a
 * radial glow follows the pointer. Pass `interaction = null` for a static surface.
 *
 * [luminanceAnimation] keeps the brightness/contrast curve of the original wrapper.
 *
 * [blurScale] multiplies the luminance-driven blur radius and [minScrim]/[maxScrim]
 * are the ends of the theme tint ramp, shared by header controls and the bottom dock.
 * Blur shrinks with controls below the dock's height, keeping frost and refraction in proportion.
 * The Desktop capsule raises both because at the shared settings (7–11dp of blur over a 0.12 scrim floor)
 * the artwork behind it stays legible instead of dissolving the way Apple's does.
 */
@Composable
fun Modifier.drawInteractiveGlass(
    isDark: Boolean,
    backdrop: Backdrop,
    luminanceAnimation: Float,
    shape: Shape,
    interaction: GlassInteraction?,
    pressedScale: Float = 1.12f,
    highlight: Highlight = GlassControlHighlight,
    blurScale: Float = 1f,
    minScrim: Float = if (isDark) 0.12f else 0.62f,
    maxScrim: Float = if (isDark) 0.5f else 0.84f,
    lensScale: Float = 1f,
    lensAmountScale: Float = 1f,
    observePress: Boolean = true,
    minBlurRadius: Dp = 0.dp,
): Modifier {
    val appearance = LocalLiquidGlassAppearance.current
    return this
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            // Share the same rim and refraction stack across header and playlist controls.
            highlight = {
                if (appearance.reflectionMultiplier == 0f) null else highlight.copy(
                    alpha = (highlight.alpha * appearance.reflectionMultiplier).coerceIn(0f, 1f),
                    width = highlight.width * appearance.reflectionMultiplier,
                )
            },
            effects = {
                val l = (luminanceAnimation * 2f - 1f).let { sign(it) * it * it }
                val press = interaction?.pressProgress ?: 0f
                // Fade the color boost with the lens. Fixed vibrancy made bright header
                // samples look polished even at the frosted end of the slider.
                colorControls(saturation = lerp(0.75f, 1.5f, appearance.lensHeightMultiplier.coerceIn(0f, 1f)))
                colorControls(
                    // Neutral brightness/contrast: the old curve brightened + washed the glass out
                    // to white on bright backgrounds ("đục trắng"). Darkening is done in onDrawSurface.
                    brightness = 0.05f * appearance.reflectionMultiplier,
                    contrast = 1f,
                    saturation = 1f + 0.5f * appearance.lensHeightMultiplier,
                )
                // Keep the same frost-to-refraction proportions on small controls as the
                // 60dp dock. A fixed 8dp blur swallowed most detail in a 32-42dp header button.
                val controlBlurScale = (size.minDimension / GlassBottomDockHeight.toPx()).coerceIn(0f, 1f)
                val baseBlurRadius = if (l > 0f) {
                    lerp(8f.dp.toPx(), 16f.dp.toPx(), l)
                } else {
                    lerp(8f.dp.toPx(), 2f.dp.toPx(), -l)
                }
                val blurRadius = baseBlurRadius * controlBlurScale * blurScale * appearance.blurMultiplier +
                    2f.dp.toPx() * press * appearance.motionMultiplier
                // Large header panes need a legibility floor independent of the smaller
                // controls' intensity curve. Other surfaces retain their existing blur.
                blur(maxOf(blurRadius, minBlurRadius.toPx()))
                // Use the library lens implementation: it guards RuntimeShader support and shape
                // constraints internally. The previous app-owned AGSL lens could crash on devices
                // or GPU drivers that reject custom shader programs.
                if (shape is RoundedRectangularShape || shape is AbsoluteRoundedCornerShape || shape is CornerBasedShape) {
                    lens(
                        (size.minDimension / 4f * lensScale + 2f.dp.toPx() * press) * appearance.lensHeightMultiplier,
                        size.minDimension / 2f * lensAmountScale * appearance.lensAmountMultiplier,
                    )
                }
            },
            onDrawSurface = {
                // Stay "đục đen": darken more as the background brightens so the glass never washes
                // out to white (shared by the bottom bar capsule, search FAB and detail-screen pills).
                val darken = lerp(minScrim, maxScrim, ((luminanceAnimation - 0.3f) / 0.5f).coerceIn(0f, 1f))
                drawRect((if (isDark) Color.Black else Color.White).copy(alpha = appearance.scrimAlpha(darken, isDark)))
                val press = interaction?.pressProgress ?: 0f
                if (press > 0f) {
                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.18f * press * appearance.reflectionMultiplier),
                                        Color.Transparent,
                                    ),
                                center = interaction?.touchPosition ?: Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension * 1.2f,
                            ),
                        blendMode = BlendMode.Plus,
                    )
                }
            },
            layerBlock =
                if (interaction != null) {
                    {
                        val scale = 1f + (pressedScale - 1f) * appearance.motionMultiplier * interaction.pressProgress
                        scaleX = scale
                        scaleY = scale
                    }
                } else {
                    null
                },
        ).then(
            if (interaction != null && observePress) {
                Modifier.pointerInput(interaction) { interaction.detectPress(this) }
            } else {
                Modifier
            },
        )
}

/**
 * Observe-only drag/press recogniser ported from Kyant's catalog
 * `DragGestureInspector`. It never consumes events, so a glass surface can react
 * to a press while the buttons it wraps still handle their own taps.
 */
internal suspend fun PointerInputScope.inspectDragGestures(
    onDragStart: (down: PointerInputChange) -> Unit = {},
    onDragEnd: (change: PointerInputChange) -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

        val down = awaitFirstDown(requireUnconsumed = false)

        onDragStart(down)
        onDrag(down, Offset.Zero)
        val upEvent =
            drag(
                pointerId = down.id,
                onDrag = { onDrag(it, it.positionChange()) },
            )
        if (upEvent == null) {
            onDragCancel()
        } else {
            onDragEnd(upEvent)
        }
    }
}

private suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): PointerInputChange? {
    val isPointerUp = currentEvent.changes.firstOrNull { it.id == pointerId }?.pressed != true
    if (isPointerUp) {
        return null
    }
    var pointer = pointerId
    while (true) {
        val change = awaitDragOrUp(pointer) ?: return null
        if (change.isConsumed) {
            return null
        }
        if (change.changedToUpIgnoreConsumed()) {
            return change
        }
        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.awaitDragOrUp(pointerId: PointerId): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.firstOrNull { it.id == pointer } ?: return null
        if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.firstOrNull { it.pressed }
            if (otherDown == null) {
                return dragEvent
            } else {
                pointer = otherDown.id
            }
        } else {
            val hasDragged = dragEvent.previousPosition != dragEvent.position
            if (hasDragged) {
                return dragEvent
            }
        }
    }
}
