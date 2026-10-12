package com.petedev.wavemusic.ui.screen.other

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.PageTopAppBar
import com.petedev.wavemusic.ui.component.pageHeaderContentHeight
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.ui.component.CenterLoadingBox
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.FrostedHeader
import com.petedev.wavemusic.ui.component.HeaderScrollOffsetTracker
import com.petedev.wavemusic.ui.component.LocalSharedHeaderHosted
import com.petedev.wavemusic.ui.component.LocalSharedHeaderState
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.component.HomeItemContentPlaylist
import com.petedev.wavemusic.ui.component.RippleIconButton
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.viewModel.MoreAlbumsUIState
import com.petedev.wavemusic.viewModel.MoreAlbumsViewModel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreAlbumsScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    id: String? = null,
    type: String? = null,
    viewModel: MoreAlbumsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(MaterialTheme.colorScheme.background)
    val gridState = rememberLazyGridState()
    val density = LocalDensity.current
    val initialHeaderHeight = innerPadding.calculateTopPadding() + pageHeaderContentHeight()
    var topAppBarHeight by remember(initialHeaderHeight) { mutableStateOf(initialHeaderHeight) }
    val topAppBarHeightPx = with(density) { topAppBarHeight.toPx() }
    val sharedHeaderState = LocalSharedHeaderState.current
    val previousHeaderOffset = remember(sharedHeaderState) { sharedHeaderState?.headerOffsetPx ?: 0f }
    val headerOffsetTracker = remember(gridState) { HeaderScrollOffsetTracker() }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val pageEntry = LocalLifecycleOwner.current as? NavBackStackEntry
    val isCurrentPage = currentBackStackEntry?.id == pageEntry?.id

    DisposableEffect(sharedHeaderState) {
        onDispose { sharedHeaderState?.updateHeaderOffset(previousHeaderOffset) }
    }
    LaunchedEffect(gridState, sharedHeaderState, topAppBarHeightPx, isCurrentPage) {
        val headerState = sharedHeaderState ?: return@LaunchedEffect
        if (!isCurrentPage) return@LaunchedEffect
        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val newOffset = headerOffsetTracker.update(
                index = index,
                offset = offset,
                currentHeaderOffsetPx = headerState.headerOffsetPx,
                maxHeightPx = topAppBarHeightPx,
            )
            headerState.updateHeaderOffset(newOffset)
        }
    }

    LaunchedEffect(id, type) {
        Logger.w("MoreAlbumsScreen", "id: $id, type: $type")
        if (id != null) {
            if (type != null) {
                when (type) {
                    MoreAlbumsDestination.ALBUM_TYPE -> {
                        viewModel.getAlbumMore(id)
                    }

                    MoreAlbumsDestination.SINGLE_TYPE -> {
                        viewModel.getSingleMore(id)
                    }
                }
            }
        }
    }

    Crossfade(targetState = uiState) { state ->
        when (state) {
            is MoreAlbumsUIState.Success -> {
                val data = state.albumItems
                LazyVerticalGrid(
                    // As many columns as fit, not a fixed two: HomeItemContentPlaylist is a fixed
                    // 180dp wide, so two columns on a wide window left each tile alone in a huge cell.
                    // 170dp rather than 180 keeps two columns on a 360dp phone (2 × 170 + the 10dp
                    // gap fits), where the cells come out exactly as wide as Fixed(2) made them.
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    state = gridState,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .frostedHeaderSource(frostedBackdrop, auroraBackground = MaterialTheme.colorScheme.background)
                            .hazeSource(state = hazeState),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        Spacer(
                            Modifier
                                .size(
                                    innerPadding.calculateTopPadding() + pageHeaderContentHeight(),
                                ),
                        )
                    }
                    items(data) { album ->
                        val data = album
                        HomeItemContentPlaylist(
                            data = data,
                            onClick = {
                                navController.navigate(
                                    AlbumDestination(
                                        browseId = album.browseId,
                                    ),
                                )
                            },
                        )
                    }
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        EndOfPage()
                    }
                }
            }

            is MoreAlbumsUIState.Error -> {
                viewModel.makeToast(state.message)
            }

            MoreAlbumsUIState.Loading -> {
                CenterLoadingBox(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(15.dp),
                )
            }
        }
    }
    // Keep Back available while loading, with the same fixed header surface as Home.
    MoreAlbumsHeader(
        title = (uiState as? MoreAlbumsUIState.Success)?.title.orEmpty(),
        navController = navController,
        hazeState = hazeState,
        backdrop = frostedBackdrop,
        modifier = Modifier.onGloballyPositioned { coordinates ->
            topAppBarHeight = with(density) { coordinates.size.height.toDp() }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreAlbumsHeader(
    title: String,
    navController: NavController,
    hazeState: HazeState,
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
) {
    FrostedHeader(
        hazeState = hazeState,
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth().then(modifier),
        shareHeader = true,
    ) {
        PageTopAppBar(
            title = {
                Text(
                    text = title,
                    style = typo().titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        if (LocalLiquidGlassEnabled.current && !LocalSharedHeaderHosted.current) Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                        else Modifier,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        WaveIcons.ArrowBackIosNew,
                        Modifier.size(com.petedev.wavemusic.ui.component.landscapeChromeSize(32.dp)),
                        true,
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        navController.navigateUp()
                    }
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    Color.Transparent,
                    Color.Unspecified,
                    Color.Unspecified,
                    Color.Unspecified,
                    Color.Unspecified,
                ),
        )
    }
}
