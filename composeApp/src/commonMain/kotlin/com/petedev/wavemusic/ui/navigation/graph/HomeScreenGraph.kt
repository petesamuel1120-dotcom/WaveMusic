package com.petedev.wavemusic.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.petedev.wavemusic.ui.navigation.destination.home.ListenTogetherDestination
import com.petedev.wavemusic.ui.navigation.destination.home.ListenTogetherSettingsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.MoodDestination
import com.petedev.wavemusic.ui.navigation.destination.home.NotificationDestination
import com.petedev.wavemusic.ui.navigation.destination.home.RecentlySongsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.SettingsDestination
import com.petedev.wavemusic.ui.screen.home.ListenTogetherScreen
import com.petedev.wavemusic.ui.screen.home.ListenTogetherSettingsScreen
import com.petedev.wavemusic.ui.screen.home.MoodScreen
import com.petedev.wavemusic.ui.screen.home.NotificationScreen
import com.petedev.wavemusic.ui.screen.home.RecentlySongsScreen
import com.petedev.wavemusic.ui.screen.home.SettingScreen
import com.petedev.wavemusic.ui.screen.home.LiquidGlassSettingsScreen
import com.petedev.wavemusic.ui.navigation.destination.home.LiquidGlassSettingsDestination

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    onOpenYouTubeAccount: () -> Unit,
) {
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<ListenTogetherDestination> {
        ListenTogetherScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<ListenTogetherSettingsDestination> {
        ListenTogetherSettingsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<NotificationDestination> {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<RecentlySongsDestination> {
        RecentlySongsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<LiquidGlassSettingsDestination> {
        LiquidGlassSettingsScreen(navController = navController, innerPadding = innerPadding)
    }
    composable<SettingsDestination> {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
            onOpenYouTubeAccount = onOpenYouTubeAccount,
        )
    }
}
