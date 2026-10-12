package com.petedev.wavemusic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.petedev.wavemusic.ui.theme.LocalReduceVisualEffects

internal const val HEADER_CONTROLS_MORPH_MILLIS = 280

internal enum class SharedHeaderControl { Navigation, Actions }

private data class HeaderMorphFrame(val key: Any, val content: State<SharedHeaderContent?>)

private data class HeaderControlsMorphScope(
    val shared: SharedTransitionScope,
    val visibility: AnimatedVisibilityScope,
)

private val LocalHeaderControlsMorphScope = staticCompositionLocalOf<HeaderControlsMorphScope?> { null }
internal val LocalSharedHeaderContentVisible = staticCompositionLocalOf { true }

/** Keep the last usable controls until the arriving page can supply its header. */
@Composable
internal fun SharedHeaderControlsMorphHost(
    entryKey: Any,
    content: SharedHeaderContent?,
    modifier: Modifier = Modifier,
) {
    val currentContent = remember(entryKey) { mutableStateOf(content) }
    // Publish a newly available header before measuring its first frame.
    if (content != null) currentContent.value = content
    val currentFrame = remember(entryKey) { HeaderMorphFrame(entryKey, currentContent) }
    var lastFrame by remember { mutableStateOf<HeaderMorphFrame?>(null) }
    SideEffect {
        if (content != null) {
            lastFrame = currentFrame
        }
    }
    val frame = if (content != null) currentFrame else lastFrame ?: return
    if (LocalReduceVisualEffects.current) {
        Box(modifier) { frame.content.value?.invoke(this) }
        return
    }
    SharedTransitionLayout(modifier) {
        val sharedScope = this
        AnimatedContent(
            targetState = frame,
            modifier = Modifier.fillMaxSize(),
            contentKey = { it.key },
            transitionSpec = { (EnterTransition.None togetherWith ExitTransition.None).using(null) },
            label = "sharedHeaderControlsMorph",
        ) { shownFrame ->
            val incoming = frame.key == shownFrame.key
            CompositionLocalProvider(
                LocalHeaderControlsMorphScope provides HeaderControlsMorphScope(sharedScope, this),
                LocalSharedHeaderContentVisible provides incoming,
            ) {
                Box(
                    Modifier.fillMaxSize().then(
                        if (incoming) Modifier else Modifier.clearAndSetSemantics {},
                    ),
                ) { shownFrame.content.value?.invoke(this) }
            }
        }
    }
}

/** Match control roles across pages, remeasuring the lenses as their bounds change. */
@Composable
internal fun Modifier.sharedHeaderControlsMorph(control: SharedHeaderControl): Modifier {
    val scope = LocalHeaderControlsMorphScope.current ?: return this
    val incoming = LocalSharedHeaderContentVisible.current
    return with(scope.shared) {
        val sharedContent = rememberSharedContentState(control)
        this@sharedHeaderControlsMorph
            .sharedElement(
                sharedContentState = sharedContent,
                animatedVisibilityScope = scope.visibility,
                boundsTransform = { _, _ -> tween(HEADER_CONTROLS_MORPH_MILLIS, easing = FastOutSlowInEasing) },
            )
            .clip(RoundedCornerShape(percent = 50))
            .drawWithContent {
                // Unmatched outgoing controls must not linger behind the new header.
                if (incoming || sharedContent.isMatchFound) drawContent()
            }
    }
}

/** Only the incoming header owns title/search content while control bounds are shared. */
@Composable
internal fun Modifier.sharedHeaderTitleVisibility(): Modifier {
    val visible = LocalSharedHeaderContentVisible.current
    return graphicsLayer { alpha = if (visible) 1f else 0f }
}
