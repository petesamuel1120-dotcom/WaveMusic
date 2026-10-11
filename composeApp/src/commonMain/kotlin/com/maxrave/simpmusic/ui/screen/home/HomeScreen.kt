package com.maxrave.simpmusic.ui.screen.home

import androidx.lifecycle.Lifecycle

import com.maxrave.simpmusic.ui.component.SharedHeaderContentHeight
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderState
import com.maxrave.simpmusic.ui.component.HeaderScrollDirectionTracker
import com.maxrave.simpmusic.ui.component.HeaderScrollOffsetTracker
import com.maxrave.simpmusic.ui.component.AmoledAlertDialog
import com.maxrave.simpmusic.ui.component.AppContentHorizontalInset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.maxrave.simpmusic.ui.component.DialogTextButton as TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.CHART_SUPPORTED_COUNTRY
import com.maxrave.common.Config
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.home.HomeItem
import com.maxrave.domain.data.model.home.chart.Chart
import com.maxrave.domain.data.model.mood.Mood
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.domain.utils.LiveStreamType
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.expect.ui.HorizontalScrollBar
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.isScrollingUp
import com.maxrave.simpmusic.extension.animateScrollToTopSmoothly
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.Chip
import com.maxrave.simpmusic.ui.component.DropdownButton
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.HomeItem
import com.maxrave.simpmusic.ui.component.HomeItemContentPlaylist
import com.maxrave.simpmusic.ui.component.HomeShimmer
import com.maxrave.simpmusic.ui.component.MoodMomentAndGenreHomeItem
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.OfflineErrorState
import com.maxrave.simpmusic.ui.component.QuickPicksItem
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.headerGlassControlSize
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.component.isListenAgainSong
import com.maxrave.simpmusic.ui.icon.Groups
import com.maxrave.simpmusic.ui.icon.PersonAdd
import com.maxrave.simpmusic.ui.icon.Settings
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.MoodDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.SettingsDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.viewModel.HomeViewModel
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_COMMUTE
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_ENERGIZE
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_FEEL_GOOD
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_FOCUS
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_PARTY
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_RELAX
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_ROMANCE
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_SAD
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_SLEEP
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_WORKOUT
import com.maxrave.simpmusic.viewModel.ListState
import com.maxrave.simpmusic.viewModel.SharedViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.all
import simpmusic.composeapp.generated.resources.app_name
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.chart
import simpmusic.composeapp.generated.resources.commute
import simpmusic.composeapp.generated.resources.do_not_show_again
import simpmusic.composeapp.generated.resources.energize
import simpmusic.composeapp.generated.resources.feel_good
import simpmusic.composeapp.generated.resources.focus
import simpmusic.composeapp.generated.resources.go_to_log_in_page
import simpmusic.composeapp.generated.resources.home
import simpmusic.composeapp.generated.resources.library
import simpmusic.composeapp.generated.resources.good_afternoon
import simpmusic.composeapp.generated.resources.good_evening
import simpmusic.composeapp.generated.resources.good_morning
import simpmusic.composeapp.generated.resources.good_night
import simpmusic.composeapp.generated.resources.let_s_pick_a_playlist_for_you
import simpmusic.composeapp.generated.resources.let_s_start_with_a_radio
import simpmusic.composeapp.generated.resources.log_in
import simpmusic.composeapp.generated.resources.log_in_warning
import simpmusic.composeapp.generated.resources.party
import simpmusic.composeapp.generated.resources.quick_picks
import simpmusic.composeapp.generated.resources.relax
import simpmusic.composeapp.generated.resources.romance
import simpmusic.composeapp.generated.resources.sad
import simpmusic.composeapp.generated.resources.sleep
import simpmusic.composeapp.generated.resources.warning
import simpmusic.composeapp.generated.resources.welcome_back
import simpmusic.composeapp.generated.resources.what_is_best_choice_today
import simpmusic.composeapp.generated.resources.workout

// DataStore key for blog-promo one-shot dialog. Bump the suffix (v2, v3, …) to re-promote.

private val listOfHomeChip =
    listOf(
        Res.string.all,
        Res.string.relax,
        Res.string.sleep,
        Res.string.energize,
        Res.string.sad,
        Res.string.romance,
        Res.string.feel_good,
        Res.string.workout,
        Res.string.party,
        Res.string.commute,
        Res.string.focus,
    )

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
fun HomeScreen(
    onScrolling: (onTop: Boolean) -> Unit = {},
    viewModel: HomeViewModel =
        koinViewModel(),
    sharedViewModel: SharedViewModel =
        koinInject(),
    navController: NavController,
    sharedHeaderHeight: Dp,
    onAccountInfoChanged: (Pair<String?, String?>?) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val sharedHeaderState = LocalSharedHeaderState.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isCurrentHomePage = currentBackStackEntry?.destination?.hasRoute(HomeDestination::class) == true
    val sharedHeaderHeightPx = with(LocalDensity.current) { sharedHeaderHeight.toPx() }
    val offsetTracker = remember { HeaderScrollOffsetTracker() }
    // This value only feeds the app-bar callback. Keep its updates in the scroll collector so
    // scroll-direction changes do not recompose the entire Home feed.
    val isScrollingUpState = scrollState.isScrollingUp()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val quickPicksTitle = stringResource(Res.string.quick_picks)
    // Navigation keeps a moving entry STARTED until its transition completes. Retain the
    // current shelves during that slide instead of rebuilding them as network results arrive.
    val homeData by viewModel.homeItemList.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val visibleHomeData =
        remember(homeData) {
            homeData.mapNotNull { item ->
                val isListenAgainShelf = item.title.trim().equals("Listen again", ignoreCase = true)
                val isRemovedStationShelf =
                    item.title.contains("listen together", ignoreCase = true) ||
                        item.title.contains("station", ignoreCase = true)
                if (isRemovedStationShelf) return@mapNotNull null
                val visibleContents =
                    item.contents.filterNot { content ->
                        content?.let {
                            val isLikedMusic =
                                it.playlistId == "LM" || it.playlistId == "VLLM" ||
                                it.browseId == "LM" || it.browseId == "VLLM" ||
                                (it.title.trim().equals("Liked Music", ignoreCase = true) &&
                                    it.description?.trim()?.equals("Auto playlist", ignoreCase = true) == true)
                            val isLiveStream =
                                LiveStreamType.isLiveStream(
                                    it.isLiveVideo,
                                    it.title,
                                    it.description,
                                )
                            isLikedMusic || isLiveStream || (isListenAgainShelf && !it.isListenAgainSong())
                        } == true
                    }
                val isEmptyAfterFiltering =
                    item.contents.any { it != null } && visibleContents.none { it != null }
                if (isEmptyAfterFiltering) {
                    null
                } else {
                    item.copy(contents = visibleContents)
                }
            }
        }
    val newRelease by viewModel.newRelease.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val visibleNewRelease =
        remember(newRelease) {
            newRelease.mapNotNull { item ->
                val visibleContents =
                    item.contents.filterNot { content ->
                        content?.let {
                            LiveStreamType.isLiveStream(
                                it.isLiveVideo,
                                it.title,
                                it.description,
                            )
                        } == true
                    }
                if (item.contents.any { it != null } && visibleContents.none { it != null }) {
                    null
                } else {
                    item.copy(contents = visibleContents)
                }
            }
        }
    val chart by viewModel.chart.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val moodMomentAndGenre by viewModel.exploreMoodItem.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val chartLoading by viewModel.loadingChart.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    var accountShow by rememberSaveable {
        mutableStateOf(false)
    }
    val regionChart by viewModel.regionCodeChart.collectAsStateWithLifecycle()
    val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val chipRowState = rememberScrollState()
    val params by viewModel.params.collectAsStateWithLifecycle()
    val homeListState by viewModel.homeListState.collectAsStateWithLifecycle()
    val continuation by viewModel.continuation.collectAsStateWithLifecycle()

    val shouldShowLogInAlert by viewModel.showLogInAlert.collectAsStateWithLifecycle()

    LaunchedEffect(accountInfo) {
        onAccountInfoChanged(accountInfo)
    }

    LaunchedEffect(scrollState, sharedHeaderState, isCurrentHomePage) {
        // Navigation keeps the outgoing destination composed during transitions. It must not
        // keep publishing its old scroll offset into the shared header after another page opens.
        if (!isCurrentHomePage) return@LaunchedEffect
        snapshotFlow {
            Triple(
                scrollState.firstVisibleItemIndex,
                scrollState.firstVisibleItemScrollOffset,
                isScrollingUpState.value,
            )
        }.collect { (index, offset, scrollingUp) ->
            sharedHeaderState?.updateHomeSurfaceVisibility(index > 0 || offset > 0)
            val newOffset = offsetTracker.update(
                index = index,
                offset = offset,
                currentHeaderOffsetPx = sharedHeaderState?.headerOffsetPx ?: 0f,
                maxHeightPx = sharedHeaderHeightPx,
            )
            sharedHeaderState?.updateHeaderOffset(newOffset)
            if (index <= 1) {
                onScrolling.invoke(true)
            } else {
                onScrolling.invoke(scrollingUp)
            }
        }
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        viewModel.getHomeItemList(params)
        Logger.w("HomeScreen", "onRefresh")
    }
    LaunchedEffect(key1 = reloadDestination) {
        if (reloadDestination == HomeDestination::class) {
            if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0) {
                Logger.w("HomeScreen", "scrollState.firstVisibleItemIndex: ${scrollState.firstVisibleItemIndex}")
                scrollState.animateScrollToTopSmoothly()
                sharedViewModel.reloadDestinationDone()
            } else {
                onRefresh.invoke()
            }
        }
    }
    LaunchedEffect(key1 = loading) {
        if (!loading) {
            val refreshWasActive = isRefreshing
            isRefreshing = false
            if (reloadDestination == HomeDestination::class) {
                sharedViewModel.reloadDestinationDone()
            }
            if (refreshWasActive) {
                coroutineScope.launch {
                    pullToRefreshState.animateToHidden()
                }
            }
        }
    }
    LaunchedEffect(key1 = homeData) {
        accountShow = homeData.find { it.subtitle == accountInfo?.first } == null
    }
    val shouldStartPaginate =
        remember {
            derivedStateOf {
                homeListState != ListState.PAGINATION_EXHAUST &&
                    (
                        scrollState.layoutInfo.visibleItemsInfo
                            .lastOrNull()
                            ?.index ?: -9
                    ) >= (scrollState.layoutInfo.totalItemsCount - 1)
            }
        }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        Logger.d("HomeScreen", "shouldStartPaginate: ${shouldStartPaginate.value}")
        Logger.d("HomeScreen", "homeListState: $homeListState")
        Logger.d("HomeScreen", "Continuation: $continuation")
        if (shouldStartPaginate.value && homeListState == ListState.IDLE) {
            viewModel.getContinueHomeItem(
                continuation,
            )
        }
    }

//    if (shouldShowGetDataSyncIdBottomSheet) {
//        GetDataSyncIdBottomSheet(
//            cookie = youTubeCookie,
//            onDismissRequest = {
//                shouldShowGetDataSyncIdBottomSheet = false
//            },
//        )
//    }

    if (shouldShowLogInAlert) {
        var doNotShowAgain by rememberSaveable {
            mutableStateOf(false)
        }
        AmoledAlertDialog(
            title = {
                Text(stringResource(Res.string.warning))
            },
            text = {
                Column {
                    Text(text = stringResource(Res.string.log_in_warning))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    doNotShowAgain = !doNotShowAgain
                                }.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = doNotShowAgain,
                            onCheckedChange = {
                                doNotShowAgain = it
                            },
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(stringResource(Res.string.do_not_show_again))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.doneShowLogInAlert(doNotShowAgain)
                    navController.navigate(LoginDestination)
                }) {
                    Text(stringResource(Res.string.go_to_log_in_page))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.doneShowLogInAlert(doNotShowAgain)
                }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            onDismissRequest = {
                viewModel.doneShowLogInAlert()
            },
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(
                                top = sharedHeaderHeight,
                            ),
                    containerColor = PullToRefreshDefaults.indicatorContainerColor,
                    color = PullToRefreshDefaults.indicatorColor,
                    maxDistance = PullToRefreshDefaults.PositionalThreshold,
                )
            },
        ) {
            // Keep loading and content mutually exclusive. Crossfading composed the full feed
            // alongside every shimmer row during the cold-start handoff, doubling draw work.
            if (!loading) {
                if (homeData.isEmpty()) {
                    OfflineErrorState(
                        onRetry = onRefresh,
                        onOpenDownloaded = {
                            navController.navigate(
                                LibraryDynamicPlaylistDestination(
                                    type = LibraryDynamicPlaylistType.Downloaded.toStringParams(),
                                ),
                            ) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                } else {
                    LazyColumn(
                        state = scrollState,
                        // The app-level header is pinned while page content, including categories,
                        // scrolls underneath it.
                        contentPadding = PaddingValues(top = sharedHeaderHeight),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item(key = "home-categories", contentType = "home-categories") {
                            HomeCategoryRow(params, loading, chipRowState, viewModel::setParams)
                        }
                        itemsIndexed(
                            visibleHomeData,
                            key = { _, item ->
                                "${item.title}:${item.moreEndpoint?.browseId ?: item.channelId.orEmpty()}:${item.contents.firstOrNull()?.videoId ?: item.contents.firstOrNull()?.browseId.orEmpty()}"
                            },
                            contentType = { _, item ->
                                if (item.title == quickPicksTitle) "quick-picks" else "home-shelf"
                            },
                        ) { index, item ->
                            Box {
                                Column(
                                    modifier =
                                        Modifier
                                            .padding(horizontal = AppContentHorizontalInset),
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (index == 0 && accountInfo != null && accountShow) {
                                        AccountLayout(
                                            accountName = accountInfo?.first ?: "",
                                            url = accountInfo?.second ?: "",
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (item.title == quickPicksTitle) {
                                        val quickPicksHomeItem =
                                            remember(item) {
                                                item.copy(
                                                    contents =
                                                        item.contents.mapNotNull { content ->
                                                            content?.copy(
                                                                artists =
                                                                    content.artists?.let { artists ->
                                                                        if (artists.size > 1) artists.dropLast(1) else artists
                                                                    },
                                                            )
                                                        },
                                                )
                                            }
                                        QuickPicks(
                                            homeItem = quickPicksHomeItem,
                                            navController = navController,
                                            viewModel = viewModel,
                                        )
                                    } else {
                                        HomeItem(
                                            navController = navController,
                                            // The personalized "Listen again" shelf uses the
                                            // account avatar as its thumbnail. The page header
                                            // already shows that avatar, so omit the duplicate here.
                                            data =
                                                if (item.subtitle == accountInfo?.first) {
                                                    item.copy(thumbnail = null)
                                                } else {
                                                    item
                                                },
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(
                                homeListState == ListState.PAGINATING,
                                enter = expandVertically() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                CenterLoadingBox(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                )
                            }
                        }
                        if (homeListState == ListState.PAGINATION_EXHAUST) {
                            items(
                                visibleNewRelease,
                                key = {
                                    "${it.title}:${it.moreEndpoint?.browseId ?: it.channelId.orEmpty()}:${it.contents.firstOrNull()?.videoId ?: it.contents.firstOrNull()?.browseId.orEmpty()}"
                                },
                                contentType = { "home-shelf" },
                            ) { release ->
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(horizontal = AppContentHorizontalInset),
                                ) {
                                    HomeItem(
                                        navController = navController,
                                        data = release,
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = moodMomentAndGenre != null,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(horizontal = AppContentHorizontalInset),
                                    ) {
                                        moodMomentAndGenre?.let {
                                            MoodMomentAndGenre(
                                                mood = it,
                                                navController = navController,
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Column(
                                    Modifier
                                        .padding(vertical = 10.dp)
                                        .padding(horizontal = AppContentHorizontalInset),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    ChartTitle()
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Crossfade(targetState = regionChart) {
                                        if (it != null) {
                                            DropdownButton(
                                                items = CHART_SUPPORTED_COUNTRY.itemsData.toList(),
                                                dialogTitle = stringResource(Res.string.chart),
                                                defaultSelected =
                                                    CHART_SUPPORTED_COUNTRY.itemsData.getOrNull(
                                                        CHART_SUPPORTED_COUNTRY.items.indexOf(it),
                                                    )
                                                        ?: CHART_SUPPORTED_COUNTRY.itemsData[1],
                                            ) {
                                                viewModel.exploreChart(
                                                    CHART_SUPPORTED_COUNTRY.items[
                                                        CHART_SUPPORTED_COUNTRY.itemsData.indexOf(
                                                            it,
                                                        ),
                                                    ],
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Crossfade(
                                        targetState = chartLoading,
                                        label = "Chart",
                                    ) { loading ->
                                        if (!loading) {
                                            chart?.let {
                                                ChartData(
                                                    chart = it,
                                                    navController = navController,
                                                )
                                            }
                                        } else {
                                            CenterLoadingBox(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(400.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            EndOfPage()
                        }
                    }
                }
            } else {
                Column {
                    Spacer(
                        Modifier.height(sharedHeaderHeight),
                    )
                    HomeCategoryRow(params, loading, chipRowState, viewModel::setParams)
                    HomeShimmer()
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryRow(
    params: String?,
    loading: Boolean,
    chipRowState: ScrollState,
    onParamsSelected: (String?) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .padding(horizontal = AppContentHorizontalInset)
                .horizontalScroll(chipRowState)
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOfHomeChip.forEach { id ->
            val selected =
                when (params) {
                    HOME_PARAMS_RELAX -> id == Res.string.relax
                    HOME_PARAMS_SLEEP -> id == Res.string.sleep
                    HOME_PARAMS_ENERGIZE -> id == Res.string.energize
                    HOME_PARAMS_SAD -> id == Res.string.sad
                    HOME_PARAMS_ROMANCE -> id == Res.string.romance
                    HOME_PARAMS_FEEL_GOOD -> id == Res.string.feel_good
                    HOME_PARAMS_WORKOUT -> id == Res.string.workout
                    HOME_PARAMS_PARTY -> id == Res.string.party
                    HOME_PARAMS_COMMUTE -> id == Res.string.commute
                    HOME_PARAMS_FOCUS -> id == Res.string.focus
                    else -> id == Res.string.all
                }
            Chip(isAnimated = loading, isSelected = selected, text = stringResource(id)) {
                onParamsSelected(
                    when (id) {
                        Res.string.relax -> HOME_PARAMS_RELAX
                        Res.string.sleep -> HOME_PARAMS_SLEEP
                        Res.string.energize -> HOME_PARAMS_ENERGIZE
                        Res.string.sad -> HOME_PARAMS_SAD
                        Res.string.romance -> HOME_PARAMS_ROMANCE
                        Res.string.feel_good -> HOME_PARAMS_FEEL_GOOD
                        Res.string.workout -> HOME_PARAMS_WORKOUT
                        Res.string.party -> HOME_PARAMS_PARTY
                        Res.string.commute -> HOME_PARAMS_COMMUTE
                        Res.string.focus -> HOME_PARAMS_FOCUS
                        else -> null
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    navController: NavController,
    accountInfo: Pair<String?, String?>?,
    title: String,
    onOpenYouTubeAccount: () -> Unit,
    contentHeight: Dp = SharedHeaderContentHeight,
    showingPlaylistPage: Boolean = false,
    onBackFromPlaylistPage: () -> Unit = {},
) {
    val avatarUrl = accountInfo?.second?.takeIf(String::isNotBlank)
    val avatarControlSize = headerGlassControlSize()
    // Keep 48dp touch container while reducing inner avatar image size by 10 physical pixels (3.33dp on 3x screen).
    val avatarImageSize = if (LocalLiquidGlassEnabled.current) avatarControlSize - 9.33.dp else 24.dp
    val signedOutAvatarIconSize = with(LocalDensity.current) { 24.dp + 10.toDp() }
    val platformContext = LocalPlatformContext.current
    val avatarRequest =
        remember(avatarUrl, platformContext) {
            avatarUrl?.let {
                ImageRequest.Builder(platformContext)
                    .data(it)
                    .memoryCacheKey(it)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(it)
                    .crossfade(true)
                    .build()
            }
        }
    com.maxrave.simpmusic.ui.component.PageTopAppBar(
        contentHeight = contentHeight,
        windowInsets = WindowInsets(0),
        navigationIconInGlassContainer = false,
        controlHorizontalInset = 5.dp,
        title = {
            Text(
                text = title,
                style = typo().titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier.size(avatarControlSize).clip(CircleShape).clickable {
                    if (showingPlaylistPage) onBackFromPlaylistPage() else onOpenYouTubeAccount()
                },
                contentAlignment = Alignment.Center,
            ) {
                if (showingPlaylistPage) {
                    Icon(
                        imageVector = SimpIcons.ArrowBackIosNew,
                        contentDescription = stringResource(Res.string.library),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp),
                    )
                } else if (avatarUrl != null && avatarRequest != null) {
                    AsyncImage(
                        model = avatarRequest,
                        placeholder = rememberHolderPainter(),
                        error = rememberHolderPainter(),
                        contentDescription = accountInfo?.first,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(avatarImageSize)
                                .clip(CircleShape),
                    )
                } else {
                    Icon(
                        imageVector = SimpIcons.PersonAdd,
                        contentDescription = stringResource(Res.string.log_in),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(signedOutAvatarIconSize),
                    )
                }
            }
        },
        actions = {
            RippleIconButton(
                imageVector = SimpIcons.Settings,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onBackground,
            ) {
                navController.navigate(SettingsDestination)
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
    )
}

@Composable
fun AccountLayout(
    accountName: String,
    url: String,
) {
    Column {
        Text(
            text = stringResource(Res.string.welcome_back),
            style = typo().bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 3.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(
                            CircleShape,
                        ),
            )
            Text(
                text = accountName,
                style = typo().headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier
                        .padding(start = 8.dp),
            )
        }
    }
}

// Quick Picks stays in the vertical Home feed, arranged as two columns per row.
@ExperimentalFoundationApi
@Composable
fun QuickPicks(
    homeItem: HomeItem,
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    var bottomSheetShow by remember { mutableStateOf(false) }
    var track by remember { mutableStateOf<Track?>(null) }

    if (bottomSheetShow) {
        NowPlayingBottomSheet(
            onDismiss = { bottomSheetShow = false },
            song = track?.toSongEntity(),
                                                navController = navController,
        )
    }

    Column(
        Modifier.padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.let_s_start_with_a_radio),
            style = typo().bodySmall,
        )
        Text(
            text = stringResource(Res.string.quick_picks),
            style = typo().headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
        Column {
            homeItem.contents.filterNotNull().take(10).chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { content ->
                        QuickPicksItem(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val firstQueue: Track = content.toTrack()
                                viewModel.setQueueData(
                                    QueueData.Data(
                                        listTracks = arrayListOf(firstQueue),
                                        firstPlayedTrack = firstQueue,
                                        playlistId = "RDAMVM${content.videoId}",
                                        playlistName = "\"${content.title}\" Radio",
                                        playlistType = PlaylistType.RADIO,
                                        continuation = null,
                                    ),
                                )
                                viewModel.loadMediaItem(
                                    firstQueue,
                                    type = Config.SONG_CLICK,
                                )
                            },
                            onLongClick = {
                                track = content.toTrack()
                                bottomSheetShow = true
                            },
                            data = content,
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MoodMomentAndGenre(
    mood: Mood,
    navController: NavController,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.let_s_pick_a_playlist_for_you),
            style = typo().bodyMedium,
        )
        // One block per section YouTube returned, headed by ITS OWN title. Hard-coding
        // "Moods & moment" / "Genre" here (and reading mood.moodsMoments / mood.genres by
        // index) mislabelled every row as soon as a signed-in account got an extra
        // "For you" section, and hid the real Genres section altogether.
        mood.sections.forEach { section ->
            val gridState = rememberLazyGridState()
            val flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = gridState))
            Text(
                text = section.title,
                style = typo().headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
            )
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cardWidth = (maxWidth - 16.dp) / 2
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(210.dp),
                    state = gridState,
                    // The two visible columns fill the same inset width as the mini-player.
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    flingBehavior = flingBehavior,
                ) {
                    items(section.items, key = { it.params }) { item ->
                        MoodMomentAndGenreHomeItem(
                            title = item.title,
                            stripeColor = item.stripeColor,
                            cardWidth = cardWidth,
                        ) {
                            navController.navigate(
                                MoodDestination(
                                    item.params,
                                ),
                            )
                        }
                    }
                }
            }
            HorizontalScrollBar(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                scrollState = gridState,
                flingBehavior = flingBehavior,
            )
        }
    }
}

@Composable
fun ChartTitle() {
    Column {
        Text(
            text = stringResource(Res.string.what_is_best_choice_today),
            style = typo().bodyMedium,
        )
        Text(
            text = stringResource(Res.string.chart),
            style = typo().headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
    }
}

@Composable
fun ChartData(
    chart: Chart,
    navController: NavController,
) {
    Column {
        chart.listChartItem.forEach { item ->
            Text(
                text = item.title,
                style = typo().headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
            )
            val lazyListState = rememberLazyListState()
            val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyListState = lazyListState))
            LazyRow(
                flingBehavior = snapperFlingBehavior,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(item.playlists.size, key = { index ->
                    val data = item.playlists[index]
                    data.id + data.title + index
                }) {
                    HomeItemContentPlaylist(
                        onClick = {
                            navController.navigate(
                                PlaylistDestination(
                                    playlistId = item.playlists[it].id,
                                    isYourYouTubePlaylist = false,
                                ),
                            ) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        data = item.playlists[it],
                        // Shrink each chart cover just enough to create a clear gutter between
                        // neighbors while keeping a two-card row's total width unchanged.
                        thumbSize = 156.dp,
                        // Chart shelves are already inside the shared app inset. Remove the
                        // playlist tile's default inner gutter so artwork reaches the same
                        // viewport edge as the mini-player and clips cleanly while swiping.
                        horizontalInset = 0.dp,
                    )
                }
            }
        }
    }
}
