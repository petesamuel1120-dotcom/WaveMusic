package com.petedev.wavemusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.AlbumEntity
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.PlaylistEntity
import com.petedev.wavemusic.domain.data.entities.PodcastsEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.browse.artist.ResultAlbum
import com.petedev.wavemusic.domain.data.model.browse.artist.ResultPlaylist
import com.petedev.wavemusic.domain.data.model.browse.artist.ResultSingle
import com.petedev.wavemusic.domain.data.model.home.Content
import com.petedev.wavemusic.domain.data.model.home.HomeItem
import com.petedev.wavemusic.domain.utils.LiveStreamType
import com.petedev.wavemusic.domain.data.model.home.HomeItem.MoreEndpoint
import com.petedev.wavemusic.domain.data.model.home.chart.ItemArtist
import com.petedev.wavemusic.domain.data.model.home.chart.ItemVideo
import com.petedev.wavemusic.domain.data.model.mood.genre.ItemsPlaylist
import com.petedev.wavemusic.domain.data.model.mood.moodmoments.Item
import com.petedev.wavemusic.domain.data.model.searchResult.albums.AlbumsResult
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Artist
import com.petedev.wavemusic.domain.data.type.ChartItem
import com.petedev.wavemusic.domain.data.type.HomeContentType
import com.petedev.wavemusic.domain.data.type.MonthlyRecapItem
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.utils.connectArtists
import com.petedev.wavemusic.domain.utils.MusicVideoType
import com.petedev.wavemusic.domain.utils.toListName
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.expect.ui.HorizontalScrollBar
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.BrowseDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PodcastDestination
import com.petedev.wavemusic.ui.theme.LocalForceDarkText
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.HomeViewModel
import com.petedev.wavemusic.viewModel.base.BaseViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.album
import com.petedev.wavemusic.resources.app_name
import com.petedev.wavemusic.resources.description
import com.petedev.wavemusic.resources.more
import com.petedev.wavemusic.resources.playlist
import com.petedev.wavemusic.resources.subscribers
import com.petedev.wavemusic.resources.wrapped_recap_subtitle
import com.petedev.wavemusic.resources.you

@Composable
fun HomeItem(
    homeViewModel: HomeViewModel = koinViewModel(),
    navController: NavController,
    data: HomeItem,
) {
    val visibleContents =
        remember(data) {
            data.contents.filterNot { content ->
                content?.let { LiveStreamType.isLiveStream(it.isLiveVideo, it.title, it.description) } == true
            }
        }
    val isRemovedStationShelf =
        data.title.contains("listen together", ignoreCase = true) ||
            data.title.contains("station", ignoreCase = true)
    if (isRemovedStationShelf || visibleContents.isEmpty()) return

    var bottomSheetShow by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyListState = lazyListState))

    var track by remember { mutableStateOf<Track?>(null) }

    if (bottomSheetShow) {
        NowPlayingBottomSheet(
            onDismiss = { bottomSheetShow = false },
            song = track?.toSongEntity(),
            navController = navController,
        )
    }

    val channelId = data.channelId
    val moreEndpoint = data.moreEndpoint
    val sectionThumbnail = data.thumbnail?.lastOrNull()
    Column {
        Row(
            modifier =
                if (channelId != null) {
                    Modifier
                        .focusable(true)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            navController.navigate(
                                ArtistDestination(
                                    channelId = channelId,
                                ),
                            )
                        }
                } else if (moreEndpoint != null) {
                    // The section title carries the same endpoint as its "More" button, as on the web.
                    Modifier
                        .focusable(true)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { navController.navigateToMoreEndpoint(moreEndpoint, data.title) }
                } else {
                    Modifier
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = sectionThumbnail != null,
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(sectionThumbnail?.url)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(sectionThumbnail?.url)
                            // New cards arrive in bursts while scrolling. Keep the transition short
                            // so decoding and animating several large covers do not drag the feed.
                            .crossfade(false)
                            .build(),
                    contentDescription = "",
                    placeholder = rememberHolderPainter(),
                    error = rememberHolderPainter(),
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(
                                CircleShape,
                            ),
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = if (sectionThumbnail != null) 10.dp else 0.dp),
            ) {
                AnimatedVisibility(visible = (data.subtitle != null && data.subtitle != "")) {
                    Text(
                        text = data.subtitle ?: "",
                        style = typo().bodySmall,
                    )
                }
                Text(
                    text = data.title,
                    style = typo().headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (moreEndpoint != null) {
                TextButton(
                    onClick = { navController.navigateToMoreEndpoint(moreEndpoint, data.title) },
                    colors =
                        ButtonDefaults
                            .textButtonColors()
                            .copy(contentColor = MaterialTheme.colorScheme.onSurface),
                ) {
                    Text(stringResource(Res.string.more), style = typo().bodyMedium)
                }
            }
        }
        LazyRow(
            state = lazyListState,
            flingBehavior = snapperFlingBehavior,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            // Keep the viewport exactly inside the shared content inset. Partial cards are
            // clipped here and revealed by swiping instead of bleeding past the mini-player.
            modifier = Modifier.fillMaxWidth().clipToBounds(),
        ) {
            items(visibleContents) { temp ->
                if (temp != null) {
                    HomeContentCard(
                        temp = temp,
                        navController = navController,
                        viewModel = homeViewModel,
                        // Home cards use the carousel's outer inset directly; the 20dp item gap
                        // replaces the old 10dp padding on each card edge.
                        horizontalInset = 0.dp,
                        // Avoid continuous marquee work across many cards while the feed scrolls.
                        // Ellipsis keeps each card's metadata readable.
                        animateOverflow = false,
                        onLongClick = {
                            track = it.toTrack()
                            bottomSheetShow = true
                        },
                    )
                }
            }
        }
        if (getPlatform() == Platform.Desktop) {
            HorizontalScrollBar(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                scrollState = lazyListState,
                flingBehavior = snapperFlingBehavior,
            )
        }
    }
}

/**
 * One shelf item, drawn as the card its ids call for, with Home's click handling: a song or video
 * starts its radio, anything else opens its page. Shared by Home's rows and the "More" page, so an
 * item behaves the same in both.
 */
@Composable
fun HomeContentCard(
    temp: Content,
    navController: NavController,
    viewModel: BaseViewModel,
    onLongClick: (Content) -> Unit,
    // Every card fills its grid cell, as on the "More" page; Home's rows keep the fixed sizes.
    fillMaxWidth: Boolean = false,
    animateOverflow: Boolean = true,
    horizontalInset: Dp? = null,
    compactTrackLayout: Boolean = false,
) {
    val cardHorizontalInset = horizontalInset ?: 10.dp
    val playlistCardHorizontalInset = horizontalInset ?: if (fillMaxWidth) 12.dp else cardHorizontalInset
    val browseId = temp.browseId
    val playlistId = temp.playlistId
    if ((playlistId != null && temp.videoId == null) || (playlistId != null && temp.videoId == "")) {
        if (playlistId.startsWith("UC")) {
            HomeItemArtist(onClick = {
                navController.navigate(
                    ArtistDestination(
                        channelId = playlistId,
                    ),
                )
            }, data = temp, fillMaxWidth = fillMaxWidth, animateOverflow = animateOverflow, horizontalInset = cardHorizontalInset)
        } else {
            HomeItemContentPlaylist(onClick = {
                navController.navigate(
                    PlaylistDestination(
                        playlistId = playlistId,
                    ),
                )
            }, data = temp, fillMaxWidth = fillMaxWidth, animateOverflow = animateOverflow, horizontalInset = playlistCardHorizontalInset)
        }
    } else if ((browseId != null && temp.videoId == null) || (browseId != null && temp.videoId == "")) {
        if (browseId.startsWith("UC")) {
            HomeItemArtist(onClick = {
                navController.navigate(
                    ArtistDestination(
                        channelId = browseId,
                    ),
                )
            }, data = temp, fillMaxWidth = fillMaxWidth, animateOverflow = animateOverflow, horizontalInset = cardHorizontalInset)
        } else if (browseId.startsWith("MPSP")) {
            HomeItemContentPlaylist(onClick = {
                navController.navigate(
                    PodcastDestination(
                        podcastId = browseId,
                    ),
                )
            }, data = temp, fillMaxWidth = fillMaxWidth, animateOverflow = animateOverflow, horizontalInset = playlistCardHorizontalInset)
        } else {
            HomeItemContentPlaylist(onClick = {
            navController.navigate(
                AlbumDestination(
                    browseId = browseId,
                ),
            )
        }, data = temp, fillMaxWidth = fillMaxWidth, animateOverflow = animateOverflow, horizontalInset = playlistCardHorizontalInset)
        }
    } else if (
        MusicVideoType.isVideoSong(temp.videoType) ||
            temp.thumbnails.firstOrNull()?.width != temp.thumbnails.firstOrNull()?.height
    ) {
        HomeItemVideo(
            onClick = { viewModel.playVideo(temp.toTrack()) },
            // Videos can be played as standalone items, but are not offered to song playlists.
            onLongClick = {},
            data = temp,
            fillMaxWidth = fillMaxWidth,
            animateOverflow = animateOverflow,
            horizontalInset = cardHorizontalInset,
        )
    } else {
        HomeItemSong(
            onClick = { temp.playRadio(viewModel) },
            onLongClick = { onLongClick(temp) },
            data = temp,
            fillMaxWidth = fillMaxWidth,
            animateOverflow = animateOverflow,
            horizontalInset = cardHorizontalInset,
            compactTrackLayout = compactTrackLayout,
        )
    }
}

/**
 * The thumbnail shape [HomeContentCard] draws for [this]: 16:9 for the video card, square for every
 * other card. It mirrors the card's dispatch, so a layout can size a row before drawing it.
 */
fun Content.homeCardAspectRatio(): Float {
    val opensPage = (playlistId != null || browseId != null) && videoId.isNullOrEmpty()
    val wide = MusicVideoType.isVideoSong(videoType) || thumbnails.firstOrNull()?.width != thumbnails.firstOrNull()?.height
    return if (!opensPage && wide) 16f / 9f else 1f
}

/** Keep Listen again and playlist suggestions song-only, even when a music video has square art. */
fun Content.isListenAgainSong(): Boolean =
    !videoId.isNullOrBlank() &&
        playlistId == null &&
        browseId == null &&
        !MusicVideoType.isVideoSong(videoType) &&
        (MusicVideoType.isAudio(videoType) || homeCardAspectRatio() == 1f)

private fun Content.playRadio(viewModel: BaseViewModel) {
    val firstQueue: Track = toTrack()
    viewModel.setQueueData(
        QueueData.Data(
            listTracks = arrayListOf(firstQueue),
            firstPlayedTrack = firstQueue,
            playlistId = "RDAMVM$videoId",
            playlistName = title,
            playlistType = PlaylistType.RADIO,
            continuation = null,
        ),
    )
    viewModel.loadMediaItem(
        firstQueue,
        Config.SONG_CLICK,
    )
}

/**
 * Opens what a section's "More" endpoint points to. YouTube names the page type on the endpoint, so
 * an album, playlist, artist or podcast opens its own screen; an unnamed one (`FEmusic_*`) is a
 * generic shelf page.
 */
fun NavController.navigateToMoreEndpoint(
    endpoint: MoreEndpoint,
    title: String?,
) {
    when (endpoint.pageType) {
        MoreEndpoint.PAGE_TYPE_ALBUM, MoreEndpoint.PAGE_TYPE_AUDIOBOOK -> {
            navigate(AlbumDestination(browseId = endpoint.browseId))
        }

        MoreEndpoint.PAGE_TYPE_PLAYLIST -> {
            navigate(PlaylistDestination(playlistId = endpoint.browseId))
        }

        MoreEndpoint.PAGE_TYPE_ARTIST, MoreEndpoint.PAGE_TYPE_USER_CHANNEL -> {
            navigate(ArtistDestination(channelId = endpoint.browseId))
        }

        MoreEndpoint.PAGE_TYPE_PODCAST -> {
            navigate(PodcastDestination(podcastId = endpoint.browseId))
        }

        else -> {
            navigate(BrowseDestination(browseId = endpoint.browseId, params = endpoint.params, title = title))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemContentPlaylist(
    onClick: () -> Unit,
    data: HomeContentType,
    thumbSize: Dp = 160.dp,
    forceDark: Boolean = LocalForceDarkText.current,
    // Fill a grid cell instead of the fixed thumbSize, as Metrolist's browse grid does.
    fillMaxWidth: Boolean = false,
    animateOverflow: Boolean = true,
    showSubtitle: Boolean = true,
    horizontalInset: Dp = if (fillMaxWidth) 12.dp else 10.dp,
    onLongClick: (() -> Unit)? = null,
) {
    val titleColor = if (forceDark) Color.White else MaterialTheme.colorScheme.onSurface
    val textWidth = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(thumbSize)
    Box(
        Modifier
            .wrapContentSize()
            .focusable(true)
            .clip(RoundedCornerShape(if (onLongClick != null) 18.dp else 8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = horizontalInset, top = 10.dp, end = horizontalInset, bottom = 6.dp)
                    // One floor for every home card, so a row mixing them stays one height. 70
                    // covers the tallest text block among them, HomeItemSong's: 69.5dp when the
                    // text engine counts Poppins' line gap (Skia on Desktop does), 65.8dp when not.
                    // A cell-filling card has no such row to match and its artwork is narrower than
                    // thumbSize, so the floor would only add empty space under every tile.
                    .then(if (fillMaxWidth) Modifier else Modifier.heightIn(min = thumbSize + 70.dp)),
        ) {
            val thumb =
                when (data) {
                    is Content -> data.thumbnails.lastOrNull()?.url
                    is com.petedev.wavemusic.domain.data.model.mood.genre.Content -> data.thumbnail?.lastOrNull()?.url
                    is com.petedev.wavemusic.domain.data.model.mood.moodmoments.Content -> data.thumbnails?.lastOrNull()?.url
                    // Empty playlists can carry YouTube's dark default cover URL even though
                    // the user never chose artwork. Use the generated, theme-aware tile instead.
                    is LocalPlaylistEntity ->
                        data.thumbnail?.takeUnless {
                            data.tracks.isNullOrEmpty() && it.startsWith("http", ignoreCase = true)
                        }
                    is ChartItem -> null
                    // YouTube returns a dark generic image for empty playlists. Treat it as
                    // absent so the theme-aware playlist placeholder is shown instead.
                    is PlaylistsResult ->
                        data.thumbnails.lastOrNull()?.url?.takeUnless {
                            data.itemCount.trim().startsWith("0")
                        }
                    is AlbumEntity -> data.thumbnails
                    is PlaylistEntity -> data.thumbnails
                    is ResultSingle -> data.thumbnails.lastOrNull()?.url
                    is ResultAlbum -> data.thumbnails.lastOrNull()?.url
                    is ResultPlaylist -> data.thumbnails.lastOrNull()?.url
                    is PodcastsEntity -> data.thumbnail
                    is AlbumsResult -> data.thumbnails.lastOrNull()?.url
                    // A recap owns no artwork; it borrows the month's own top song's, resolved
                    // where the recap itself is (LibraryViewModel.getMonthlyRecaps).
                    // Deliberately null: a recap has no artwork of its own, so it falls to
                    // the title placeholder below — the same tile a local playlist without
                    // a thumbnail gets. Same reason ChartItem above is null.
                    is MonthlyRecapItem -> null
                    else -> null
                }
            val artworkModifier =
                (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.size(thumbSize))
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
            if (data is LocalPlaylistEntity && data.tracks.isNullOrEmpty() && thumb.isNullOrBlank()) {
                LocalPlaylistArtworkPlaceholder(modifier = artworkModifier)
            } else if (data is PlaylistsResult && data.itemCount.trim().startsWith("0")) {
                // Empty user playlists should use the same neutral artwork as local playlists,
                // rather than the generic branded painter used for other missing covers.
                LocalPlaylistArtworkPlaceholder(modifier = artworkModifier)
            } else {
                AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder =
                    when (data) {
                        is LocalPlaylistEntity -> {
                            painterPlaylistThumbnail(
                                data.title,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        is PlaylistsResult -> {
                            painterPlaylistThumbnail(
                                data.title,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        is ChartItem -> {
                            painterPlaylistThumbnail(
                                data.name,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        // A month whose top song has no artwork still has a name, and the
                        // deterministic title tile reads as a playlist where the grey holder
                        // reads as a failed load.
                        is MonthlyRecapItem -> {
                            painterPlaylistThumbnail(
                                data.title,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        else -> {
                            rememberHolderPainter()
                        }
                    },
                error =
                    when (data) {
                        is LocalPlaylistEntity -> {
                            painterPlaylistThumbnail(
                                data.title,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        is PlaylistsResult -> {
                            painterPlaylistThumbnail(
                                data.title,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        is ChartItem -> {
                            painterPlaylistThumbnail(
                                data.name,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        // A month whose top song has no artwork still has a name, and the
                        // deterministic title tile reads as a playlist where the grey holder
                        // reads as a failed load.
                        is MonthlyRecapItem -> {
                            painterPlaylistThumbnail(
                                data.title,
                                style = typo().bodySmall,
                                thumbSize * 0.9f to thumbSize * 0.9f,
                            )
                        }

                        else -> {
                            rememberHolderPainter()
                        }
                    },
                fallback =
                    if (data is LocalPlaylistEntity && thumb.isNullOrBlank()) {
                        painterPlaylistThumbnail(
                            data.title,
                            style = typo().bodySmall,
                            thumbSize * 0.9f to thumbSize * 0.9f,
                        )
                    } else {
                        null
                    },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = artworkModifier,
                )
            }
            Text(
                    text =
                        when (data) {
                        is Content -> data.title
                        is com.petedev.wavemusic.domain.data.model.mood.genre.Content -> data.title.title
                        is com.petedev.wavemusic.domain.data.model.mood.moodmoments.Content -> data.title
                        is LocalPlaylistEntity -> data.title
                        is ChartItem -> data.name
                        is PlaylistsResult -> data.title
                        is AlbumEntity -> data.title
                        is PlaylistEntity -> data.title
                        is ResultSingle -> data.title
                        is ResultAlbum -> data.title
                        is ResultPlaylist -> data.title
                        is PodcastsEntity -> data.title
                        is AlbumsResult -> data.title
                        is MonthlyRecapItem -> data.title
                        else -> ""
                    },
                style = typo().titleSmall,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 8.dp),
            )
            if (showSubtitle) {
                Text(
                text =
                    when (data) {
                        is Content -> {
                            data.description
                                ?: if (data.playlistId == null) {
                                    (
                                        if (!data.artists.isNullOrEmpty()) {
                                            data.artists
                                                .toListName()
                                                .connectArtists()
                                        } else {
                                            stringResource(Res.string.album)
                                        }
                                    )
                                } else {
                                    stringResource(Res.string.playlist)
                                }
                        }

                        is com.petedev.wavemusic.domain.data.model.mood.genre.Content -> {
                            data.title.subtitle
                        }

                        is com.petedev.wavemusic.domain.data.model.mood.moodmoments.Content -> {
                            data.subtitle
                        }

                        is LocalPlaylistEntity -> {
                            stringResource(Res.string.you)
                        }

                        is ChartItem -> {
                            stringResource(Res.string.app_name)
                        }

                        is PlaylistsResult -> {
                            data.author
                        }

                        is AlbumEntity -> {
                            data.artistName?.connectArtists() ?: stringResource(Res.string.album)
                        }

                        is PlaylistEntity -> {
                            data.author ?: stringResource(Res.string.playlist)
                        }

                        is ResultSingle -> {
                            data.year
                        }

                        is ResultAlbum -> {
                            data.year
                        }

                        is ResultPlaylist -> {
                            data.author
                        }

                        is PodcastsEntity -> {
                            data.authorName
                        }

                        is MonthlyRecapItem -> {
                            stringResource(Res.string.wrapped_recap_subtitle)
                        }

                        is AlbumsResult -> {
                            data.year
                        }

                        else -> {
                            ""
                        }
                        },
                    style = typo().bodySmall,
                    minLines = 1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        textWidth
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .then(
                                if (animateOverflow) {
                                    Modifier.basicMarquee(
                                        initialDelayMillis = 2000,
                                        repeatDelayMillis = 2000,
                                        velocity = 25.dp,
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickPicksItem(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    data: Content,
    modifier: Modifier = Modifier,
) {
    val tag = "QuickPicksItem"
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .focusable(true)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(data.thumbnails.lastOrNull()?.url)
                        .crossfade(false)
                        .diskCacheKey(data.thumbnails.lastOrNull()?.url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                placeholder = rememberHolderPainter(),
                contentDescription = stringResource(Res.string.description),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterVertically)
                        .size(44.dp)
                        .clip(
                            RoundedCornerShape(10),
                        ),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(
                        start = 16.dp,
                    ).align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Keep the title to one line so the artist row remains aligned across the grid.
                Text(
                    text = data.title,
                    style = typo().titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .padding(
                                bottom = 3.dp,
                            ),
                )
                LazyRow(verticalAlignment = Alignment.CenterVertically) {
                    item {
                        androidx.compose.animation.AnimatedVisibility(visible = data.isExplicit == true) {
                            ExplicitBadge(
                                modifier =
                                    Modifier
                                        .size(20.dp)
                                        .padding(end = 4.dp)
                                        .weight(1f),
                            )
                        }
                    }
                    item {
                        Text(
                            text = data.artists.toListName().connectArtists(),
                            style = typo().bodySmall,
                            minLines = 1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .basicMarquee(
                                        initialDelayMillis = 2000,
                                        repeatDelayMillis = 2000,
                                        velocity = 25.dp,
                                    ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemSong(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    data: Content,
    // Fill a grid cell instead of the fixed 160dp, as Metrolist's browse grid does.
    fillMaxWidth: Boolean = false,
    animateOverflow: Boolean = true,
    horizontalInset: Dp = 10.dp,
    compactTrackLayout: Boolean = false,
) {
    val textWidth = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(160.dp)
    val verticalInset = if (compactTrackLayout) 4.dp else 10.dp
    val bottomInset = if (compactTrackLayout) 2.dp else 6.dp
    val titleSpacing = if (compactTrackLayout) 4.dp else 8.dp
    val artistTextInset = if (compactTrackLayout) 1.dp else 3.dp
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .focusable(true)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = horizontalInset, top = verticalInset, end = horizontalInset, bottom = bottomInset)
                    .heightIn(min = if (compactTrackLayout) 0.dp else 230.dp),
        ) {
            val thumb =
                data.thumbnails.lastOrNull()?.url?.let {
                    if (it.contains("w120")) {
                        Regex("([wh])120").replace(it, "$1544")
                    } else {
                        it
                    }
                }
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .then(if (fillMaxWidth) Modifier.fillMaxWidth().aspectRatio(1f) else Modifier.size(160.dp))
                        .clip(
                            RoundedCornerShape(10.dp),
                        ),
            )
            Text(
                text = data.title,
                style = typo().titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = titleSpacing),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = data.isExplicit == true) {
                    ExplicitBadge(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                                .weight(1f),
                    )
                }
                Text(
                    text =
                        listOfNotNull(
                            data.artists
                                .toListName()
                                .connectArtists()
                                .takeIf { it.isNotBlank() },
                            data.album?.name?.takeIf { it.isNotBlank() },
                        ).joinToString(" • "),
                    style = typo().bodySmall,
                    minLines = 1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        textWidth
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .then(
                                if (animateOverflow) {
                                    Modifier.basicMarquee(
                                        initialDelayMillis = 2000,
                                        repeatDelayMillis = 2000,
                                        velocity = 25.dp,
                                    )
                                } else {
                                    Modifier
                                },
                            ).padding(vertical = artistTextInset),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemVideo(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    data: Content,
    forceDark: Boolean = LocalForceDarkText.current,
    // Fill a grid cell instead of the fixed 284.5dp, as Metrolist's browse grid does.
    fillMaxWidth: Boolean = false,
    animateOverflow: Boolean = true,
    horizontalInset: Dp = 10.dp,
) {
    val titleColor = if (forceDark) Color.White else MaterialTheme.colorScheme.onSurface
    val textWidth = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(284.5.dp)
    Box(
        Modifier
            .fillMaxSize()
            .focusable(true)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = horizontalInset, top = 10.dp, end = horizontalInset, bottom = 6.dp)
                    .heightIn(min = 230.dp),
        ) {
            val thumb = data.thumbnails.lastOrNull()?.url
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder = rememberHolderPainter(isVideo = true),
                error = rememberHolderPainter(isVideo = true),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.height(160.dp))
                        .aspectRatio(16f / 9f)
                        .clip(
                            RoundedCornerShape(10.dp),
                        ),
            )
            Text(
                text = data.title,
                style = typo().titleSmall,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 8.dp),
            )
            Text(
                text =
                    listOfNotNull(
                        data.artists
                            .toListName()
                            .connectArtists()
                            .takeIf { it.isNotBlank() },
                        data.views?.takeIf { it.isNotBlank() },
                    ).joinToString(" • "),
                style = typo().bodySmall,
                minLines = 1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .then(
                            if (animateOverflow) {
                                Modifier.basicMarquee(
                                    initialDelayMillis = 2000,
                                    repeatDelayMillis = 2000,
                                    velocity = 25.dp,
                                )
                            } else {
                                Modifier
                            },
                        ).padding(vertical = 2.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemArtist(
    onClick: () -> Unit,
    data: Content,
    forceDark: Boolean = LocalForceDarkText.current,
    // Fill a grid cell instead of the fixed 160dp, as Metrolist's browse grid does.
    fillMaxWidth: Boolean = false,
    animateOverflow: Boolean = true,
    horizontalInset: Dp = 10.dp,
) {
    val titleColor = if (forceDark) Color.White else MaterialTheme.colorScheme.onSurface
    val textWidth = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(160.dp)
    Box(
        Modifier
            .fillMaxSize()
            .focusable(true)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = horizontalInset, top = 10.dp, end = horizontalInset, bottom = 6.dp)
                    .heightIn(min = 230.dp),
        ) {
            val thumb = data.thumbnails.lastOrNull()?.url
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .then(if (fillMaxWidth) Modifier.fillMaxWidth().aspectRatio(1f) else Modifier.size(160.dp))
                        .clip(
                            CircleShape,
                        ),
            )
            Text(
                text = data.title,
                style = typo().titleSmall,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 8.dp),
            )
            Text(
                text = data.description?.takeIf { it.isNotBlank() }.orEmpty(),
                style = typo().bodySmall,
                minLines = 1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .then(
                            if (animateOverflow) {
                                Modifier.basicMarquee(
                                    initialDelayMillis = 2000,
                                    repeatDelayMillis = 2000,
                                    velocity = 25.dp,
                                )
                            } else {
                                Modifier
                            },
                        ),
            )
        }
    }
}

@Composable
fun MoodMomentAndGenreHomeItem(
    title: String,
    stripeColor: Long,
    cardWidth: Dp? = null,
    fillMaxWidth: Boolean = false,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(12.dp)
    val cardBackdrop = rememberBackdrop(MaterialTheme.colorScheme.surface)
    Box(
        modifier =
            (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(cardWidth ?: 144.dp))
                .height(50.dp)
                .padding(vertical = 8.dp)
                .clip(cardShape)
                .clickable(onClick = onClick),
    ) {
        // The matte base is a local backdrop source; the lens surface is its sibling, avoiding
        // a recursive sample of the whole scrolling Home feed.
        Box(
            Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surface)
                .layerBackdrop(cardBackdrop),
        )
        Box(
            Modifier
                .matchParentSize()
                .liquidGlass(
                    backdrop = cardBackdrop,
                    shape = cardShape,
                    interactive = false,
                    lensScale = 0.45f,
                ),
        )
        Row {
            Box(
                // `solid.leftStripeColor` straight from the API (full ARGB). This used to be
                // generateRandomColor(), which — being outside remember — rolled a new colour on
                // every recomposition, so the stripes flickered while scrolling.
                Modifier
                    .width(10.dp)
                    .height(64.dp)
                    .background(Color(stripeColor)),
            )
            Text(
                text = title,
                style = typo().titleSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
fun ItemVideoChart(
    onClick: () -> Unit,
    data: ItemVideo,
    position: Int,
) {
    Box(
        Modifier
            .wrapContentSize()
            .focusable(true)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(10.dp),
        ) {
            val thumb = data.thumbnails.lastOrNull()?.url
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder = rememberHolderPainter(isVideo = true),
                error = rememberHolderPainter(isVideo = true),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(280.dp)
                        .height(160.dp)
                        .clip(
                            RoundedCornerShape(10),
                        ),
            )
            Row {
                Text(
                    text = position.toString(),
                    style = typo().titleLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .width(40.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .align(Alignment.CenterVertically),
                )
                Column(Modifier.padding(start = 10.dp)) {
                    Text(
                        text = data.title,
                        style = typo().titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier
                                .width(210.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .padding(top = 10.dp),
                    )
                    Text(
                        text =
                            listOfNotNull(
                                data.artists
                                    .toListName()
                                    .connectArtists()
                                    .takeIf { it.isNotBlank() },
                                data.views.takeIf { it.isNotBlank() },
                            ).joinToString(" • "),
                        style = typo().bodyMedium,
                        minLines = 1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier
                                .width(210.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .basicMarquee(
                                    initialDelayMillis = 2000,
                                    repeatDelayMillis = 2000,
                                    velocity = 25.dp,
                                ).padding(vertical = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ItemArtistChart(
    onClick: () -> Unit,
    data: ItemArtist,
    widthDp: Dp,
) {
    Box(
        Modifier
            .wrapContentSize()
            .focusable(true)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
    ) {
        Row(
            modifier =
                Modifier
                    .padding(10.dp)
                    .width(widthDp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = data.rank,
                style = typo().titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier =
                    Modifier
                        .wrapContentSize(Alignment.Center)
                        .align(Alignment.CenterVertically)
                        .padding(end = 20.dp),
            )
            val thumb = data.thumbnails.lastOrNull()?.url
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterVertically)
                        .size(60.dp)
                        .clip(
                            CircleShape,
                        ),
            )
            Column(
                Modifier
                    .padding(start = 15.dp)
                    .width(160.dp)
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = data.title,
                    style = typo().titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .wrapContentHeight(align = Alignment.CenterVertically),
                )
                Text(
                    text =
                        if (data.subscribers.contains(
                                stringResource(Res.string.subscribers).replace("%1\$s ", ""),
                            )
                        ) {
                            data.subscribers
                        } else {
                            stringResource(
                                Res.string.subscribers,
                                data.subscribers,
                            )
                        },
                    style = typo().bodySmall,
                    minLines = 1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                initialDelayMillis = 2000,
                                repeatDelayMillis = 2000,
                                velocity = 25.dp,
                            ),
                )
            }
        }
    }
}

@Composable
fun ItemTrackChart(
    onClick: () -> Unit,
    data: Track,
    position: Int?,
    widthDp: Dp,
) {
    Box(
        modifier =
            Modifier
                .wrapContentSize()
                .focusable(true)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onClick()
                },
    ) {
        Row(
            modifier =
                Modifier
                    .width(widthDp)
                    .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Crossfade(targetState = position != null, label = "Chart Track Position") {
                if (it) {
                    Row {
                        Text(
                            text = position.toString(),
                            style = typo().titleLarge,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier =
                                Modifier
                                    .width(40.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .align(Alignment.CenterVertically),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }
            val thumb = data.thumbnails?.lastOrNull()?.url
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumb)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumb)
                        .crossfade(false)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterVertically)
                        .size(50.dp)
                        .clip(
                            RoundedCornerShape(10),
                        ),
            )
            Column(
                Modifier
                    .padding(
                        start = 20.dp,
                    ).align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = data.title,
                    style = typo().titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .padding(
                                bottom = 3.dp,
                            ),
                )

                Text(
                    text = data.artists.toListName().connectArtists(),
                    style = typo().bodySmall,
                    minLines = 1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                initialDelayMillis = 2000,
                                repeatDelayMillis = 2000,
                                velocity = 25.dp,
                            ),
                )
            }
        }
    }
}

@Composable
fun MoodAndGenresContentItem(
    data: Any?,
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    Column(
        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
    ) {
        Text(
            text =
                when (data) {
                    is ItemsPlaylist -> (data).header
                    is Item -> (data).header
                    else -> ""
                },
            style = typo().titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .padding(
                        horizontal = 15.dp,
                    ).fillMaxWidth(),
        )
        LazyRow(
            modifier =
                Modifier.padding(
                    10.dp,
                ),
        ) {
            val itemList =
                when (data) {
                    is ItemsPlaylist -> (data).contents
                    is Item -> (data).contents
                    else -> listOf()
                }
            items(itemList) { item ->
                HomeItemContentPlaylist(onClick = {
                    // The "Songs" shelf mixes tracks into a list that is otherwise all playlists,
                    // so route by videoId: a track starts its radio, everything else opens a page.
                    val moodSong = item as? com.petedev.wavemusic.domain.data.model.mood.moodmoments.Content
                    val songVideoId = moodSong?.videoId
                    if (moodSong != null && songVideoId != null) {
                        val track =
                            Track(
                                album = null,
                                artists = listOf(Artist(id = null, name = moodSong.subtitle)),
                                duration = null,
                                durationSeconds = null,
                                isAvailable = true,
                                isExplicit = false,
                                likeStatus = null,
                                thumbnails = moodSong.thumbnails,
                                title = moodSong.title,
                                videoId = songVideoId,
                                videoType = null,
                                category = null,
                                feedbackTokens = null,
                                resultType = null,
                            )
                        homeViewModel.setQueueData(
                            QueueData.Data(
                                listTracks = arrayListOf(track),
                                firstPlayedTrack = track,
                                playlistId = "RDAMVM$songVideoId",
                                playlistName = "\"${moodSong.title}\" Radio",
                                playlistType = PlaylistType.RADIO,
                                continuation = null,
                            ),
                        )
                        homeViewModel.loadMediaItem(track, type = Config.SONG_CLICK)
                    } else {
                        navController.navigate(
                            PlaylistDestination(
                                playlistId =
                                    if (item is com.petedev.wavemusic.domain.data.model.mood.genre.Content) {
                                        item.playlistBrowseId
                                    } else {
                                        (item as com.petedev.wavemusic.domain.data.model.mood.moodmoments.Content).playlistBrowseId
                                    },
                            ),
                        )
                    }
                }, data = item)
            }
        }
    }
}
