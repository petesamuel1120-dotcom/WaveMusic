package com.petedev.wavemusic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Chip(
    isAnimated: Boolean = false,
    isSelected: Boolean = false,
    text: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    animateLabel: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    InfiniteBorderAnimationView(
        isAnimated = isAnimated && isSelected,
        brush = Brush.sweepGradient(listOf(Color.Gray, Color.White)),
        backgroundColor = Color.Transparent,
        contentPadding = 0.dp,
        borderWidth = 1.dp,
        shape = CircleShape,
        oneCircleDurationMillis = 2500,
        animateSize = isAnimated,
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            ElevatedFilterChip(
                modifier = modifier,
                selected = isSelected,
                onClick = { onClick.invoke() },
                label = {
                    if (animateLabel) {
                        AnimatedContent(
                            targetState = text to isSelected,
                            transitionSpec = {
                                val selecting = targetState.second
                                (
                                    slideInHorizontally(tween(220)) { width ->
                                        if (selecting) width / 3 else -width / 3
                                    } + fadeIn(tween(150))
                                ).togetherWith(
                                    slideOutHorizontally(tween(220)) { width ->
                                        if (selecting) -width / 3 else width / 3
                                    } + fadeOut(tween(150)),
                                )
                            },
                            label = "filter_chip_label_slide",
                        ) { (label, _) ->
                            Text(label, maxLines = 1)
                        }
                    } else {
                        Text(text, maxLines = 1)
                    }
                },
                leadingIcon = leadingIcon,
                shapes = FilterChipDefaults.shapes(
                    shape = CircleShape,
                    selectedShape = CircleShape,
                    pressedShape = CircleShape,
                ),
                // No shadow. An unselected chip has no fill, and a drop shadow under a see-through
                // shape shows THROUGH it as a dark ring instead of sitting behind it. Naming
                // `elevation` alone is enough: pressed, focused and hovered all default to it.
                elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = 0.dp),
                colors =
                    FilterChipDefaults.elevatedFilterChipColors(
                        // Unselected chips have no fill at all. They mostly sit over artwork (Home)
                        // where a solid pill reads as a slab punched over the image; the outline
                        // below is what carries the shape now. The selected chip deliberately stays
                        // opaque — that contrast IS the selection, and it is the only one that has
                        // to be found at a glance.
                        containerColor = Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        // `primary` is a pale #B2C5FF on the dark theme, so the label has to be its
                        // matching `on` token — a hand-picked white would read on one theme and
                        // vanish on the other.
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                border =
                    FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = Color.Transparent,
                        borderColor = MaterialTheme.colorScheme.outline,
                    ),
                // Keep every shape state circular; the expressive API otherwise morphs the outline
                // on selection, which made this tab feel like it was growing and shrinking.
            )
        }
    }
}
