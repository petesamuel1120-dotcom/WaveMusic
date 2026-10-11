package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.typo
import coil3.compose.AsyncImage
import kotlin.math.roundToInt

/** Tracks banner title bounds for Liquid Glass handoff; flat chrome keeps the banner title visible. */
class StickyPlaylistTitleState(val list: LazyListState, val itemKey: Any) {
    private var viewportTop by mutableFloatStateOf(0f)
    private var sourceOffset by mutableFloatStateOf(0f)
    private var sourceStartOffset by mutableStateOf<Float?>(null)
    private var sourceBounds by mutableStateOf<Rect?>(null)
    private var sourceStyle by mutableStateOf<TextStyle?>(null)
    private var sourceImage by mutableStateOf<String?>(null)
    private var sourceImageFailed by mutableStateOf(false)
    private var sourceColor by mutableStateOf<Color?>(null)
    private var targetBounds by mutableStateOf<Rect?>(null)
    private var targetStyle by mutableStateOf<TextStyle?>(null)
    private var sourceTextAlign by mutableStateOf(TextAlign.Center)
    private var targetTextAlign by mutableStateOf(TextAlign.Center)

    val viewportModifier: Modifier
        get() = Modifier.onGloballyPositioned { viewportTop = it.positionInWindow().y }

    @Composable
    fun sourceModifier(
        style: TextStyle,
        imageUrl: String? = null,
        color: Color? = null,
        hideSource: Boolean = LocalLiquidGlassEnabled.current,
        textAlign: TextAlign = TextAlign.Center,
    ): Modifier = Modifier
        .onGloballyPositioned { coordinates ->
            val item = list.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
            if (item != null) {
                // positionInWindow remains valid as the banner crosses the viewport boundary.
                val position = coordinates.positionInWindow()
                sourceBounds = Rect(position.x, position.y,
                    position.x + coordinates.size.width, position.y + coordinates.size.height)
                sourceOffset = position.y - viewportTop - item.offset
                if (list.firstVisibleItemIndex == 0) {
                    // Recover the unscrolled position even during a reverse scroll. Short
                    // landscape heroes must not start halfway through the title handoff.
                    sourceStartOffset = position.y - viewportTop + list.firstVisibleItemScrollOffset
                }
                sourceStyle = style
                if (sourceImage != imageUrl) sourceImageFailed = false
                sourceImage = imageUrl
                sourceColor = color
                sourceTextAlign = textAlign
            }
        }
        .let { modifier ->
            if (hideSource) modifier.alpha(0f).clearAndSetSemantics {} else modifier
        }

    val isDocked: Boolean
        get() {
            val target = targetBounds ?: return false
            return sourceBounds != null && sourceTop <= target.top
        }

    /** Pixels the source title has traveled past the toolbar title position. */
    val distancePastDockPx: Float
        get() {
            val target = targetBounds ?: return 0f
            if (sourceBounds == null) return 0f
            return (target.top - sourceTop).coerceAtLeast(0f)
        }

    private val sourceTop: Float
        get() {
            val item = list.layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
            return if (item != null) viewportTop + item.offset + sourceOffset
            else if (list.firstVisibleItemIndex > 0) Float.NEGATIVE_INFINITY
            else sourceBounds?.top ?: Float.POSITIVE_INFINITY
        }

    @Composable
    fun Target(title: String, textAlign: TextAlign = TextAlign.Center, style: TextStyle = typo().titleMedium) {
        Text(title, style = style, textAlign = textAlign, maxLines = 1,
            modifier = Modifier.fillMaxWidth().onGloballyPositioned {
                // Use the full layout bounds while the destination is clipped during its slide.
                val position = it.positionInWindow()
                targetBounds = Rect(position.x, position.y,
                    position.x + it.size.width, position.y + it.size.height)
                targetTextAlign = textAlign
                targetStyle = style
            }.alpha(0f).clearAndSetSemantics {})
    }

    @Composable
    fun BoxScope.Title(title: String, color: Color, visible: Boolean = true) {
        val source = sourceBounds ?: return
        val target = targetBounds ?: return
        val density = LocalDensity.current
        var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        val position = stickyPlaylistTitlePosition(sourceTop, target.top, source.center.x,
            target.center.x, source.width, target.width,
            maxOf(with(density) { 96.dp.toPx() }, source.height * 2f),
            sourceStartTop = sourceStartOffset?.let { viewportTop + it })
        val style = lerp(sourceStyle ?: typo().titleMedium, targetStyle ?: typo().titleMedium, position.progress)
        val textMeasurer = rememberTextMeasurer()
        val textWidth = textMeasurer.measure(
            title, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = position.width.roundToInt().coerceAtLeast(0)),
        ).size.width.toFloat()
        // Landscape metadata starts at the left edge; the library toolbar centers its title.
        // Move the measured text continuously between those alignments instead of jumping.
        val sourceInset = if (sourceTextAlign == TextAlign.Center) (position.width - textWidth) / 2f else 0f
        val targetInset = if (targetTextAlign == TextAlign.Center) (position.width - textWidth) / 2f else 0f
        val textInset = sourceInset + (targetInset - sourceInset) * position.progress
        if (visible) {
            // Artist wordmarks are artwork, not alternate glyphs for the title. Fading both in
            // the same bounds makes them look like a broken, doubled name halfway through scroll.
            // Finish moving/fading the wordmark before introducing the plain text title.
            val logoAlpha = (1f - position.progress * 2f).coerceIn(0f, 1f)
            val titleAlpha = ((position.progress - 0.5f) * 2f).coerceIn(0f, 1f)
            Box(Modifier.matchParentSize().onGloballyPositioned { origin = it.positionInWindow() }) {
                val titleModifier = Modifier
                    .absoluteOffset { IntOffset((position.left - origin.x).roundToInt(), (position.top - origin.y).roundToInt()) }
                    .width(with(density) { position.width.toDp() })
                val showLogo = sourceImage != null && !sourceImageFailed
                if (showLogo && logoAlpha > 0f) {
                    AsyncImage(model = sourceImage, contentDescription = null,
                        contentScale = ContentScale.Fit, onError = { sourceImageFailed = true },
                        modifier = titleModifier
                            .height(with(density) { (source.height + (target.height - source.height) * position.progress).toDp() })
                            .alpha(logoAlpha))
                }
                Text(title, style = style, color = lerpColor(sourceColor ?: color, color, position.progress), maxLines = 1,
                    textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .absoluteOffset { IntOffset((position.left + textInset - origin.x).roundToInt(), (position.top - origin.y).roundToInt()) }
                        .width(with(density) { textWidth.toDp() })
                        .alpha(if (showLogo) titleAlpha else 1f))
            }
        }
    }
}
