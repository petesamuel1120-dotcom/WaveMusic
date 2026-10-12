package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.viewModel.SharedViewModel
import kotlin.reflect.KClass

@Composable
actual fun LiquidGlassAppBottomNavigationBar(
    startDestination: Any,
    navController: NavController,
    backdrop: PlatformBackdrop,
    viewModel: SharedViewModel,
    isScrolledToTop: Boolean,
    showAnalyticsTab: Boolean,
    showMixForYouTab: Boolean,
    onOpenNowPlaying: () -> Unit,
    onDismissNowPlaying: () -> Unit,
    onSwipeExpandProgress: (Float) -> Unit,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit,
    embedded: Boolean,
) {
}
