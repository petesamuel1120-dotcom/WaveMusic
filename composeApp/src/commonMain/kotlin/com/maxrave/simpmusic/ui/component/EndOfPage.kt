package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// The shell measures the navigation bar and player together. Lists behind that dock
// need only its actual height, with no extra reservation when contentPadding covers it.
val LocalPageBottomInset = staticCompositionLocalOf { 0.dp }

@Composable
fun pageEndSpacing(bottomPaddingAlreadyApplied: Dp = 0.dp): Dp =
    (LocalPageBottomInset.current - bottomPaddingAlreadyApplied).coerceAtLeast(0.dp) + 12.dp

@Composable
fun EndOfPage(bottomPaddingAlreadyApplied: Dp = 0.dp) {
    Spacer(Modifier.fillMaxWidth().height(pageEndSpacing(bottomPaddingAlreadyApplied)))
}
