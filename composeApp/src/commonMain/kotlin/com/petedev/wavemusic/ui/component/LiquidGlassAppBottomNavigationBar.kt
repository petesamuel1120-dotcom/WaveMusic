package com.petedev.wavemusic.ui.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.icon.AutoGraph
import com.petedev.wavemusic.ui.icon.Home
import com.petedev.wavemusic.ui.icon.LibraryMusic
import com.petedev.wavemusic.ui.icon.Search
import com.petedev.wavemusic.ui.icon.Sensors
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.home.AnalyticsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.HomeDestination
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDestination
import com.petedev.wavemusic.ui.navigation.destination.library.MixForYouDestination
import com.petedev.wavemusic.ui.navigation.destination.search.SearchDestination
import com.petedev.wavemusic.viewModel.SharedViewModel
import org.jetbrains.compose.resources.StringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.analytics
import com.petedev.wavemusic.resources.home
import com.petedev.wavemusic.resources.library
import com.petedev.wavemusic.resources.mix
import com.petedev.wavemusic.resources.search
import kotlin.reflect.KClass

@Composable
expect fun LiquidGlassAppBottomNavigationBar(
    startDestination: Any = HomeDestination,
    navController: NavController,
    backdrop: PlatformBackdrop,
    viewModel: SharedViewModel,
    isScrolledToTop: Boolean = false,
    showAnalyticsTab: Boolean = false,
    showMixForYouTab: Boolean = false,
    onOpenNowPlaying: () -> Unit = {},
    onDismissNowPlaying: () -> Unit = {},
    onSwipeExpandProgress: (Float) -> Unit = {},
    reloadDestinationIfNeeded: (KClass<*>) -> Unit = { _ -> },
    // Render only the capsule when a parent dock supplies its player, spacing and insets.
    embedded: Boolean = false,
)

sealed class BottomNavScreen(
    val ordinal: Int,
    val destination: Any,
    val title: StringResource,
    val icon: @Composable () -> Unit,
) {
    data object Home : BottomNavScreen(
        ordinal = 0,
        destination = HomeDestination,
        title = Res.string.home,
        icon = {
            Icon(
                WaveIcons.Home,
                contentDescription = null,
            )
        },
    )

    data object Search : BottomNavScreen(
        ordinal = 1,
        destination = SearchDestination,
        title = Res.string.search,
        icon = {
            Icon(
                WaveIcons.Search,
                contentDescription = null,
            )
        },
    )

    data object Library : BottomNavScreen(
        ordinal = 2,
        destination = LibraryDestination,
        title = Res.string.library,
        icon = {
            Icon(
                imageVector = WaveIcons.LibraryMusic,
                contentDescription = null,
            )
        },
    )

    // Only shown when local tracking is enabled.
    data object Analytics : BottomNavScreen(
        ordinal = 3,
        destination = AnalyticsDestination,
        title = Res.string.analytics,
        icon = {
            Icon(
                imageVector = WaveIcons.AutoGraph,
                contentDescription = null,
            )
        },
    )

    // Only shown while signed in to YouTube — an anonymous session gets no mixes.
    // Labelled "Mix", not "Mix for you": the full title is the widest label in the bar and forces
    // every tab to be that wide. The screen itself still uses the full title.
    data object MixForYou : BottomNavScreen(
        ordinal = 4,
        destination = MixForYouDestination,
        title = Res.string.mix,
        icon = {
            Icon(
                imageVector = WaveIcons.Sensors,
                contentDescription = null,
            )
        },
    )
}
