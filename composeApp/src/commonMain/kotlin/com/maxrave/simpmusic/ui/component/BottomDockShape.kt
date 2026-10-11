package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Keep the landscape player's height in sync with its neighboring navigation bar.
internal val GlassBottomDockHeight = 60.dp
internal val FlatBottomDockHeight = 64.dp

// Match the player's and navigation bar's silhouette at their different heights.
// A fixed radius made the bar look boxier.
private const val BottomDockCornerPercent = 45
internal val BottomDockShape = RoundedCornerShape(BottomDockCornerPercent)

// Inset the selection's curve with its surface, keeping the two rims concentric.
internal fun bottomDockSelectionShape(outerHeight: Dp, verticalInset: Dp) =
    RoundedCornerShape(outerHeight * (BottomDockCornerPercent / 100f) - verticalInset)

internal val MiniPlayerArtworkSize = 36.dp
internal val MiniPlayerArtworkShape = RoundedCornerShape(12.dp)
