package com.petedev.wavemusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petedev.wavemusic.ui.theme.typo

/** Slide a centered title left while making room for search in the same toolbar row. */
@Composable
fun AnimatedLandscapeHeaderSearch(
    title: String,
    searchVisible: Boolean,
    contentColor: Color,
    searchField: @Composable () -> Unit,
    subtitle: String? = null,
    titleVisible: Boolean = true,
) {
    val progress by animateFloatAsState(
        targetValue = if (searchVisible) 1f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "landscapeTitleSearchHandoff",
    )
    BoxWithConstraints(Modifier.fillMaxWidth().height(headerGlassControlSize())) {
        val titleSpace = minOf(140.dp, maxWidth * 0.28f) + 12.dp
        val animatedTitleSpace = maxWidth - (maxWidth - titleSpace) * progress
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.width(animatedTitleSpace)
                    .height(headerGlassControlSize())
                    .padding(end = 12.dp * progress),
                contentAlignment = BiasAlignment(-progress, 0f),
            ) {
                androidx.compose.animation.AnimatedVisibility(titleVisible || searchVisible, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        // Measure both font lines together instead of squeezing the count
                        // into the height left after the title. The box centers the stack.
                        modifier = Modifier.wrapContentHeight(unbounded = true),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            title,
                            style = landscapeChromeTextStyle(typo().titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp)),
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle != null && !searchVisible) {
                            Text(
                                subtitle,
                                style = landscapeChromeTextStyle(typo().bodySmall.copy(fontSize = 10.sp, lineHeight = 12.sp)),
                                color = contentColor.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                modifier = Modifier.weight(1f),
                visible = searchVisible,
                enter = fadeIn(tween(240)) + slideInHorizontally(tween(240)) { it / 6 },
                exit = fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 },
            ) {
                searchField()
            }
        }
    }
}
