package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PodcastDestination

/** Lets artwork-backed pages extend their ambient color behind app chrome. */
val LocalPageArtworkBackground = staticCompositionLocalOf<MutableState<Color?>?> { null }

/** Keep the window and inset page on the same artwork-derived ground. */
fun NavDestination?.usesPageArtworkBackground(): Boolean =
    this?.hasRoute(PlaylistDestination::class) == true ||
        this?.hasRoute(PodcastDestination::class) == true
