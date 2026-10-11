package com.maxrave.simpmusic.ui.screen.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxrave.simpmusic.extension.copy
import com.maxrave.simpmusic.ui.component.FrostedHeader
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.component.auroraGradientBackground
import com.maxrave.simpmusic.ui.component.GridLibraryPlaylist
import com.maxrave.simpmusic.ui.screen.home.HomeTopAppBar
import com.maxrave.simpmusic.viewModel.LibraryViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.mix_for_you
import simpmusic.composeapp.generated.resources.no_mixes_found

/**
 * The YouTube "Mix for you" playlists, promoted out of the Library chip row into a tab of its own.
 *
 * It shares [LibraryViewModel] with the Library screen rather than owning a view model, so the
 * mixes stay fetched once and the grid, its pull-to-refresh and its empty state behave exactly as
 * they did while this was a chip.
 *
 * The tab is hidden while signed out (see `App.kt`), which is what the chip did too — YouTube has
 * no mixes to give an anonymous session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixForYouScreen(
    innerPadding: PaddingValues,
    viewModel: LibraryViewModel = koinViewModel(),
    navController: NavController,
    onScrolling: (onTop: Boolean) -> Unit = {},
    onOpenYouTubeAccount: () -> Unit,
) {
    val density = LocalDensity.current
    val mixForYou by viewModel.youTubeMixForYou.collectAsStateWithLifecycle()
    val loggedIn by viewModel.youtubeLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val accountThumbnail by viewModel.accountThumbnail.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState()
    val gridState = rememberLazyGridState()
    var topAppBarHeight by remember { mutableStateOf(72.dp) }

    // Keep the chrome theme-based; artwork colors made the header visibly jump as covers loaded.
    val backgroundColor = MaterialTheme.colorScheme.background
    val frostedBackdrop = rememberBackdrop(backgroundColor)
    val isLightTheme = backgroundColor.luminance() > 0.5f

    LaunchedEffect(Unit) {
        if (mixForYou.data.isNullOrEmpty()) {
            viewModel.getYouTubeMixedForYou()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Keep the blur source on the scrolling page; the frosted toolbar is a separate sibling.
        Box(Modifier.fillMaxSize().frostedHeaderSource(frostedBackdrop).auroraGradientBackground(isLightTheme).hazeSource(hazeState)) {
            GridLibraryPlaylist(
                navController,
                innerPadding.copy(top = topAppBarHeight),
                mixForYou,
                emptyText = Res.string.no_mixes_found,
                state = gridState,
                onScrolling = onScrolling,
            ) {
                viewModel.getYouTubeMixedForYou()
            }
        }
        FrostedHeader(
            hazeState = hazeState,
            backdrop = frostedBackdrop,
            shareHeader = true,
            tint = backgroundColor,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        topAppBarHeight = with(density) { coordinates.size.height.toDp() }
                    },
        ) {
            Column {
                HomeTopAppBar(
                    navController = navController,
                    accountInfo = if (loggedIn) Pair("signed-in", accountThumbnail) else null,
                    title = stringResource(Res.string.mix_for_you),
                    onOpenYouTubeAccount = onOpenYouTubeAccount,
                )
            }
        }
    }
}
