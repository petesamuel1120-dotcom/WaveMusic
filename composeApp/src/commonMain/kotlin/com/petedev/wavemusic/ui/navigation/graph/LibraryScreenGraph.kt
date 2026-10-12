package com.petedev.wavemusic.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.petedev.wavemusic.ui.screen.library.LibraryDynamicPlaylistScreen
import com.petedev.wavemusic.ui.component.slideV2TitleTransition

@ExperimentalMaterial3Api
fun NavGraphBuilder.libraryScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    sharedHeaderHeight: Dp,
    headerTint: Color,
    useSlideV2TitleTransition: Boolean = false,
) {
    composable<LibraryDynamicPlaylistDestination> { entry ->
        val data = entry.toRoute<LibraryDynamicPlaylistDestination>()
        val titleTransition = if (useSlideV2TitleTransition) slideV2TitleTransition(this) else null
        LibraryDynamicPlaylistScreen(
            innerPadding = innerPadding,
            navController = navController,
            type = data.type,
            sharedHeaderHeight = sharedHeaderHeight,
            headerTint = headerTint,
            slideV2TitleAlpha = titleTransition?.alpha,
            freezeStickyTitle = titleTransition?.inProgress == true,
        )
    }
}
