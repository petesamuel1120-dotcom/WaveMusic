package com.maxrave.simpmusic.ui.screen.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.Config
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.home.Content
import com.maxrave.domain.data.model.searchResult.songs.Artist
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.LiveStreamType
import com.maxrave.simpmusic.expect.shareUrl
import com.maxrave.simpmusic.expect.ui.MediaPlayerView
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.barBlurStyle
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.getStringBlocking
import com.maxrave.simpmusic.extension.hexToColorOrNull
import com.maxrave.simpmusic.extension.rgbFactor
import com.maxrave.simpmusic.extension.toImmersiveBackground
import com.maxrave.simpmusic.extension.toSquareThumbnailUrl
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.component.DescriptionView
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.PageActionHeader
import com.maxrave.simpmusic.ui.component.LocalPageContentInsets
import com.maxrave.simpmusic.ui.component.LocalPageBottomInset
import com.maxrave.simpmusic.ui.component.StickyPlaylistTitleState
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.HomeItemArtist
import com.maxrave.simpmusic.ui.component.HomeItemContentPlaylist
import com.maxrave.simpmusic.ui.component.HomeItemVideo
import com.maxrave.simpmusic.ui.component.glassActionSize
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionState
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.Check
import com.maxrave.simpmusic.ui.icon.IosShare
import com.maxrave.simpmusic.ui.icon.Movie
import com.maxrave.simpmusic.ui.icon.MovieOff
import com.maxrave.simpmusic.ui.icon.PersonAdd
import com.maxrave.simpmusic.ui.icon.Sensors
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.BrowseDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.viewModel.ArtistScreenState
import com.maxrave.simpmusic.viewModel.ArtistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.albums
import simpmusic.composeapp.generated.resources.baseline_favorite_24
import simpmusic.composeapp.generated.resources.description
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.featured_inArtist
import simpmusic.composeapp.generated.resources.liked_songs
import simpmusic.composeapp.generated.resources.liked_songs_by
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.more
import simpmusic.composeapp.generated.resources.no_description
import simpmusic.composeapp.generated.resources.popular
import simpmusic.composeapp.generated.resources.videos_inArtist
import simpmusic.composeapp.generated.resources.related_artists
import simpmusic.composeapp.generated.resources.share
import simpmusic.composeapp.generated.resources.singles
import simpmusic.composeapp.generated.resources.unknown

@Composable
@ExperimentalMaterial3Api
fun ArtistScreen(
    channelId: String,
    viewModel: ArtistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
) {
    val artistScreenState by viewModel.artistScreenState.collectAsStateWithLifecycle()
    val isFollowed by viewModel.followed.collectAsStateWithLifecycle()
    val canvasUrl by viewModel.canvasUrl.collectAsStateWithLifecycle()
    // Header shows the canvas video by default; the top-right toggle swaps it for the artist's
    // picture. Keyed on the canvas so a different artist's canvas starts as video again.
    var showCanvasVideo by rememberSaveable(canvasUrl?.first) { mutableStateOf(true) }
    val headerCanvas = canvasUrl?.takeIf { showCanvasVideo }
    val shareTitle = stringResource(Res.string.share)
    val artistLogo by viewModel.artistLogo.collectAsStateWithLifecycle()

    val playingTrack by remember {
        sharedViewModel.nowPlayingState.map { it?.track?.videoId }
    }.collectAsState(null)

    // Choosing song to show Bottom sheet
    var choosingTrack by remember {
        mutableStateOf<Track?>(null)
    }
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(channelId) {
        if (channelId != artistScreenState.data.channelId) {
            viewModel.browseArtist(channelId)
        }
    }

    // Apple Music-inspired immersive treatment. Portrait keeps a square artwork frame; landscape
    // keeps the wide banner while reserving room for the persistent bottom dock.
    val screenInfo = getScreenSizeInfo()
    val isPortrait = screenInfo.wDP < screenInfo.hDP

    // Palette extraction from the artist artwork (portrait Apple-style only).
    val paletteState = com.kmpalette.rememberPaletteState()
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val landscapeBannerAspectRatio =
        bitmap?.takeIf { it.width > it.height }?.let { it.width.toFloat() / it.height } ?: 2.4f
    // Keep the action row above the mini-player and navigation dock. The banner can still use its
    // natural wide ratio on roomy windows; on short landscape windows it yields the dock's space.
    val landscapeBannerHeight =
        minOf(
            (screenInfo.wDP / landscapeBannerAspectRatio).dp,
            (screenInfo.hDP.dp - LocalPageBottomInset.current - 80.dp).coerceAtLeast(160.dp),
        )
    var paletteGeneratedFor by remember { mutableStateOf<String?>(null) }
    val currentImageUrl = (artistScreenState as? ArtistScreenState.Success)?.data?.imageUrl

    LaunchedEffect(bitmap) {
        val bm = bitmap
        if (bm != null && currentImageUrl != null && paletteGeneratedFor != currentImageUrl) {
            paletteState.generate(bm)
            paletteGeneratedFor = currentImageUrl
        }
    }

    val appIsDark = LocalAppIsDarkTheme.current
    val appColorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val artistContentColor = if (appIsDark) Color.White else appColorScheme.onSurface
    val artistSecondaryColor = if (appIsDark) Color(0xC4FFFFFF) else appColorScheme.onSurfaceVariant
    // Keep the artwork-derived hue while lifting the page ground for light mode.
    val mutedPaletteBg = paletteState.palette.toImmersiveBackground(isDark = appIsDark)
    // Tint for the description card, matching the non-portrait CollapsingToolbar color.
    val sectionTint = paletteState.palette.getColorFromPalette()

    // Accent color for the action buttons, sourced from the artist name-logo image's dominant
    // color (hidden catalog). Use the theme accent as the light-mode fallback.
    val artistAccent =
        artistLogo?.bgColorHex?.hexToColorOrNull()
            ?: if (appIsDark) Color.White else appColorScheme.primary
    val artistAccentForeground = if (artistAccent.luminance() > 0.5f) Color.Black else Color.White
    val useGlassActions = LocalLiquidGlassEnabled.current

    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(mutedPaletteBg)
    val lazyState = rememberLazyListState()
    val stickyTitle = remember(lazyState, channelId) { StickyPlaylistTitleState(lazyState, "artist_hero") }
    val firstItemVisible by remember {
        derivedStateOf { lazyState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    Crossfade(artistScreenState) { state ->
        when (state) {
            is ArtistScreenState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CenterLoadingBox(
                        Modifier
                            .align(Alignment.Center),
                    )
                }
            }

            is ArtistScreenState.Success -> {
                // ---- Apple Music style (mobile portrait only) ----
                Box(Modifier.fillMaxSize().background(mutedPaletteBg)) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(mutedPaletteBg)
                                .windowInsetsPadding(LocalPageContentInsets.current)
                                .then(stickyTitle.viewportModifier)
                                .frostedHeaderSource(frostedBackdrop)
                                .hazeSource(hazeState),
                        state = lazyState,
                    ) {
                        item(key = "artist_hero", contentType = "header") {
                            Box {
                                val actionBackdrop = rememberBackdrop(mutedPaletteBg)
                                Column(
                                    modifier = Modifier.layerBackdrop(actionBackdrop),
                                    // Negative spacing pulls the action row up into the header AND
                                    // shrinks the layout, so there's no leftover gap before "Popular"
                                    // (unlike Modifier.offset, which only moves pixels, not layout).
                                    verticalArrangement = Arrangement.spacedBy((-36).dp),
                                ) {
                                    // Edge-to-edge artwork (canvas plays on top of it when available).
                                    // Portrait fills a SQUARE frame, so the URL is clamped to a square
                                    // size there (logic from commit 5e596c5b). Landscape keeps YouTube's
                                    // own wide banner (e.g. w2880-h1200) instead: squaring the source
                                    // first would make the Crop below throw away most of its height.
                                    val headerImageUrl =
                                        state.data.imageUrl?.let {
                                            if (isPortrait) it.toSquareThumbnailUrl() else it
                                        }
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (isPortrait) {
                                                        Modifier.aspectRatio(1f)
                                                    } else {
                                                        // Preserve the banner's wide crop, bounded by the area
                                                        // above the persistent bottom dock.
                                                        Modifier.height(landscapeBannerHeight)
                                                    },
                                                ),
                                    ) {
                                        // Artwork and canvas feed the page capture; the moving header is outside it.
                                        Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                                            // Media layer (artwork + canvas).
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model =
                                                        ImageRequest
                                                            .Builder(LocalPlatformContext.current)
                                                            .data(headerImageUrl)
                                                            .diskCachePolicy(CachePolicy.ENABLED)
                                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                                            .diskCacheKey(headerImageUrl)
                                                            .memoryCacheKey(headerImageUrl)
                                                            .crossfade(false)
                                                            .build(),
                                                    placeholder = rememberHolderPainter(),
                                                    error = rememberHolderPainter(),
                                                    contentDescription = null,
                                                    // FillWidth fits the square source into the square portrait
                                                    // frame. The landscape frame takes the banner's own aspect,
                                                    // so Crop trims nothing once it has decoded.
                                                    contentScale =
                                                        if (isPortrait) ContentScale.FillWidth else ContentScale.Crop,
                                                    // Always decoded so the page background color can be extracted
                                                    // from the artwork palette, even when a canvas is playing.
                                                    onSuccess = {
                                                        bitmap = it.result.image.toImageBitmap()
                                                    },
                                                    // Hidden (but still decoded above) while a canvas is present —
                                                    // the canvas is shown instead. No canvas -> artwork is shown.
                                                    modifier =
                                                        Modifier
                                                            .fillMaxSize()
                                                            .alpha(if (headerCanvas != null) 0f else 1f),
                                                )
                                                // The artwork's bottom 200dp melts into the page through a
                                                // Modifier.blur copy of it, faded in by a DstIn gradient. At
                                                // the top of the ramp the copy is fully transparent, so there
                                                // is no seam — haze's HazeProgressive drew a visible line
                                                // there on Android (and crashes on skiko). Below Android 12
                                                // blur is a no-op and the copy is pixel-identical to the
                                                // artwork, leaving just the colour scrim. Skipped under a
                                                // canvas, where the artwork itself is hidden.
                                                if (headerCanvas == null) {
                                                    AsyncImage(
                                                        model = headerImageUrl,
                                                        contentDescription = null,
                                                        contentScale =
                                                            if (isPortrait) ContentScale.FillWidth else ContentScale.Crop,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxSize()
                                                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                                                .drawWithContent {
                                                                    drawContent()
                                                                    drawRect(
                                                                        brush =
                                                                            Brush.verticalGradient(
                                                                                colors = listOf(Color.Transparent, Color.Black),
                                                                                startY = size.height - 200.dp.toPx(),
                                                                                endY = size.height,
                                                                            ),
                                                                        blendMode = BlendMode.DstIn,
                                                                    )
                                                                }.blur(32.dp),
                                                    )
                                                }
                                                // Canvas (Spotify) plays AS the background when present;
                                                // otherwise the static artwork above is the fallback.
                                                headerCanvas?.let { canvas ->
                                                    // Canvas is a tall/portrait video. cropToBounds center
                                                    // scale-to-covers it into the header frame (ContentScale.Crop):
                                                    // true video aspect ratio, no stretch, overflow clipped.
                                                    MediaPlayerView(
                                                        url = canvas.first,
                                                        modifier = Modifier.fillMaxSize(),
                                                        cropToBounds = true,
                                                    )
                                                }
                                            } // end media layer
                                            // 5% black over the artwork/canvas, under the fade and scrim, so
                                            // a bright photo sits back a little behind the title.
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.05f)),
                                            )
                                            // Color scrim is a SEPARATE, taller box: the blur stays at 200dp so
                                            // its cost doesn't grow, while the color gets 70% of the artwork
                                            // to ramp over. A short ramp means a steep alpha, and a steep
                                            // alpha is what reads as a visible edge. 70% of the frame's own
                                            // height in both orientations — measured off the frame, since the
                                            // landscape one now follows the banner rather than the window.
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(0.7f)
                                                        .align(Alignment.BottomCenter)
                                                        .background(artworkScrimBrush(mutedPaletteBg)),
                                            )
                                            // Artist name (TEXT for now — logo image is roadmap) + subscriber · view
                                            Column(
                                                modifier =
                                                    Modifier
                                                        .align(Alignment.BottomCenter)
                                                        // Lift the name + subtitle together with the action row.
                                                        .offset(y = (-36).dp)
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 20.dp)
                                                        .padding(bottom = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                val logo = artistLogo
                                                if (logo != null && appIsDark) {
                                                    // Artist name rendered as a logo image (hidden catalog),
                                                    // in place of the plain-text title.
                                                    AsyncImage(
                                                        model = logo.logoUrl,
                                                        contentDescription = state.data.title,
                                                        contentScale = ContentScale.Fit,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth(0.7f)
                                                                .heightIn(max = 84.dp)
                                                                .then(stickyTitle.sourceModifier(typo().titleLarge, imageUrl = logo.logoUrl, color = artistContentColor)),
                                                    )
                                                } else {
                                                    Text(
                                                        text = state.data.title ?: stringResource(Res.string.unknown),
                                                        modifier = stickyTitle.sourceModifier(typo().titleLarge, color = artistContentColor),
                                                        style = typo().titleLarge,
                                                        color = artistContentColor,
                                                        maxLines = 2,
                                                        textAlign = TextAlign.Center,
                                                    )
                                                }
                                                val meta =
                                                    listOfNotNull(
                                                        state.data.subscribers?.takeIf { it.isNotBlank() },
                                                        state.data.playCount?.takeIf { it.isNotBlank() },
                                                    ).joinToString(" · ")
                                                if (meta.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = meta,
                                                        style = typo().bodyMedium,
                                                        color = artistSecondaryColor,
                                                        textAlign = TextAlign.Center,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Reserve the action row's height in the source; its glass controls
                                    // render as an overlay so the source cannot capture itself.
                                    Spacer(Modifier.fillMaxWidth().height(80.dp))
                                }

                                // Apple Music-style action row: [Radio][Shuffle pill][Follow] centered.
                                // In WaveMusic "play" an artist == shuffle, so the big middle button is Shuffle.
                                Row(
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(horizontal = 32.dp)
                                            .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Sample the header and its page-colored continuation beneath the
                                    // row. Both side buttons sit outside their backdrop source.
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(glassActionSize())
                                                .then(
                                                    if (useGlassActions) {
                                                        Modifier.liquidGlass(
                                                            actionBackdrop,
                                                            CircleShape,
                                                        )
                                                    } else {
                                                        Modifier.clip(CircleShape)
                                                            .border(1.5.dp, artistAccent, CircleShape)
                                                    },
                                                )
                                                .clickable {
                                                    val param = state.data.radioParam
                                                    if (param != null) {
                                                        viewModel.onRadioClick(param)
                                                    } else {
                                                        viewModel.makeToast(runBlocking { getString(Res.string.error) })
                                                    }
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.Sensors,
                                            contentDescription = "Radio",
                                            tint = if (useGlassActions) artistContentColor else artistAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    // Shuffle — primary "play" for an artist. Circular icon button filled
                                    // with the artist accent (white fallback); icon uses the dark page
                                    // background color so it stays legible on a bright accent.
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(artistAccent)
                                                .clickable {
                                                    val param = state.data.shuffleParam
                                                    if (param != null) {
                                                        viewModel.onShuffleClick(param)
                                                    } else {
                                                        viewModel.makeToast(runBlocking { getString(Res.string.error) })
                                                    }
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.Shuffle,
                                            contentDescription = "Shuffle",
                                            tint = artistAccentForeground,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                    // Following keeps the checkmark and a light accent tint in glass
                                    // mode; the flat style retains its outlined/filled states.
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(glassActionSize())
                                                .then(
                                                    if (useGlassActions) {
                                                        Modifier.liquidGlass(
                                                            actionBackdrop,
                                                            CircleShape,
                                                        ).background(
                                                            if (isFollowed) artistAccent.copy(alpha = 0.16f) else Color.Transparent,
                                                            CircleShape,
                                                        )
                                                    } else {
                                                        Modifier.clip(CircleShape)
                                                            .background(if (isFollowed) artistAccent else Color.Transparent)
                                                            .border(1.5.dp, artistAccent, CircleShape)
                                                    },
                                                )
                                                .clickable {
                                                    viewModel.updateFollowed(
                                                        if (isFollowed) 0 else 1,
                                                        state.data.channelId ?: return@clickable,
                                                    )
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = if (isFollowed) SimpIcons.Check else SimpIcons.PersonAdd,
                                            contentDescription = if (isFollowed) "Followed" else "Follow",
                                            tint =
                                                if (useGlassActions) artistContentColor
                                                else if (isFollowed) artistAccentForeground else artistAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
                        }
                        item(contentType = "sections") {
                            ArtistSections(
                                channelId = channelId,
                                state = state,
                                selectionState = selectionState,
                                playingTrack = playingTrack,
                                descriptionTint = sectionTint,
                                contentColor = artistContentColor,
                                secondaryColor = artistSecondaryColor,
                                isDark = appIsDark,
                                navController = navController,
                                viewModel = viewModel,
                                sharedViewModel = sharedViewModel,
                                onTrackMore = { track ->
                                    choosingTrack = track
                                    showBottomSheet = true
                                },
                            )
                        }
                    }

                    if (!selectionState.isActive) {
                        val headerControlColor =
                            if (!shouldHideTopBar && useGlassActions) Color.White else artistContentColor
                        PageActionHeader(
                            title = state.data.title.orEmpty(),
                            showTitle = stickyTitle.isDocked,
                            stickyTitle = stickyTitle,
                            hazeState = hazeState,
                            backdrop = frostedBackdrop,
                            tint = mutedPaletteBg,
                            contentColor = headerControlColor,
                            onBack = { navController.navigateUp() },
                            modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(LocalPageContentInsets.current),
                        ) {
                            if (canvasUrl != null) {
                                IconButton(
                                    onClick = { showCanvasVideo = !showCanvasVideo },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = if (showCanvasVideo) SimpIcons.MovieOff else SimpIcons.Movie,
                                        contentDescription = if (showCanvasVideo) "Show artist artwork" else "Show artist canvas",
                                        tint = headerControlColor,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { shareUrl(shareTitle, "https://music.youtube.com/channel/$channelId") },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(SimpIcons.IosShare, contentDescription = shareTitle, tint = headerControlColor)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectionState.isActive,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    SongSelectionTopAppBar(
                        state = selectionState,
                        onSelectAll = {
                            selectionState.toggleSelectAll(
                                (artistScreenState as? ArtistScreenState.Success)
                                    ?.data
                                    ?.popularSongs
                                    ?.map { it.videoId }
                                    ?: emptyList(),
                            )
                        },
                        onOpenActions = { showSelectionSheet = true },
                        modifier = Modifier.windowInsetsPadding(LocalPageContentInsets.current),
                        containerColor = if (appIsDark) Color.Black else appColorScheme.surface,
                    )
                }
                if (showSelectionSheet) {
                    val selectedIds = selectionState.selected.toList()
                    SelectedSongsBottomSheet(
                        count = selectedIds.size,
                        onDismiss = { showSelectionSheet = false },
                        onPlayNext = {
                            selectionViewModel.playNext(selectedIds)
                            selectionState.exit()
                        },
                        onAddToQueue = {
                            selectionViewModel.addToQueue(selectedIds)
                            selectionState.exit()
                        },
                        onAddToPlaylist = { showSelectionAddToPlaylist = true },
                        onDownload = {
                            selectionViewModel.download(selectedIds)
                            selectionState.exit()
                        },
                        onAddToFavorite = {
                            selectionViewModel.addToFavorite(selectedIds)
                            selectionState.exit()
                        },
                    )
                }
                if (showSelectionAddToPlaylist) {
                    val selectedIds = selectionState.selected.toList()
                    val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
                    AddToPlaylistModalBottomSheet(
                        isBottomSheetVisible = true,
                        listLocalPlaylist = localPlaylists,
                        listYouTubePlaylist = emptyList(),
                        onDismiss = { showSelectionAddToPlaylist = false },
                        onClick = { playlist ->
                            selectionViewModel.addToPlaylist(playlist.id, selectedIds)
                            selectionState.exit()
                        },
                        onYTPlaylistClick = {},
                    )
                }
                if (showBottomSheet && choosingTrack != null) {
                    NowPlayingBottomSheet(
                        onDismiss = {
                            showBottomSheet = false
                            choosingTrack = null
                        },
                        navController = navController,
                        song = choosingTrack?.toSongEntity(),
                    )
                }
            }

            is ArtistScreenState.Error -> {
                viewModel.makeToast(state.message ?: stringResource(Res.string.error))
                navController.navigateUp()
            }
        }
    }
}

/**
 * Shared artist body (Popular → Description), used by the portrait Apple-Music layout and the
 * existing CollapsingToolbar layout.
 */
@Composable
private fun ArtistSections(
    channelId: String,
    state: ArtistScreenState.Success,
    selectionState: SongSelectionState,
    playingTrack: String?,
    descriptionTint: Color,
    contentColor: Color,
    secondaryColor: Color,
    isDark: Boolean,
    navController: NavController,
    viewModel: ArtistViewModel,
    sharedViewModel: SharedViewModel,
    onTrackMore: (Track) -> Unit,
) {
    val likedSongCount by viewModel.likedSongCount.collectAsStateWithLifecycle()
    Column {
        // Liked songs by this artist (issue #2524), shaped like Spotify's section: a heading, then
        // the artist's picture wearing the liked heart beside the count. Shown only once at least
        // one song is liked; opens the full list with the route's channelId, the id it was counted by.
        androidx.compose.animation.AnimatedVisibility(likedSongCount > 0) {
            LikedSongsSection(
                imageUrl = state.data.imageUrl?.toSquareThumbnailUrl(),
                count = likedSongCount,
                artistName = state.data.title.orEmpty(),
                contentColor = contentColor,
                secondaryColor = secondaryColor,
                onClick = {
                    navController.navigate(
                        LibraryDynamicPlaylistDestination(
                            type = LibraryDynamicPlaylistType.ArtistLiked(channelId).toStringParams(),
                        ),
                    )
                },
            )
        }

        // Popular Songs
        AnimatedVisibility(state.data.popularSongs.isNotEmpty()) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.popular),
                        style = typo().labelMedium,
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            val id = state.data.listSongParam
                            if (id != null) {
                                navController.navigate(PlaylistDestination(id))
                            } else {
                                viewModel.makeToast(runBlocking { getString(Res.string.error) })
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = contentColor,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                state.data.popularSongs.forEach { song ->
                    SongFullWidthItems(
                        forceDark = isDark,
                        track = song,
                        isPlaying = song.videoId == playingTrack,
                        modifier = Modifier.fillMaxWidth(),
                        onMoreClickListener = {
                            onTrackMore(song)
                        },
                        onClickListener = {
                            val firstQueue: Track = song
                            viewModel.setQueueData(
                                QueueData.Data(
                                    listTracks = arrayListOf(firstQueue),
                                    firstPlayedTrack = firstQueue,
                                    playlistId = "RDAMVM${song.videoId}",
                                    playlistName = "\"${state.data.title ?: ""}\" ${getStringBlocking(Res.string.popular)}",
                                    playlistType = PlaylistType.RADIO,
                                    continuation = null,
                                ),
                            )
                            viewModel.loadMediaItem(
                                firstQueue,
                                type = Config.SONG_CLICK,
                            )
                        },
                        onAddToQueue = {
                            sharedViewModel.addListToQueue(
                                arrayListOf(song),
                            )
                        },
                        selectionMode = selectionState.isActive,
                        isSelected = selectionState.isSelected(song.videoId),
                        onLongClick = { selectionState.start(it) },
                        onSelectToggle = { selectionState.toggle(it) },
                    )
                }
            }
        }

        // Singles
        AnimatedVisibility(
            state.data.singles != null &&
                state.data.singles!!
                    .results
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.singles),
                        style = typo().labelMedium,
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            if (state.data.channelId != null) {
                                val id = "MPAD${state.data.channelId}"
                                navController.navigate(
                                    MoreAlbumsDestination(
                                        id = id,
                                        type = MoreAlbumsDestination.SINGLE_TYPE,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(getStringBlocking(Res.string.error))
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = contentColor,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.singles?.results ?: emptyList()) { single ->
                        HomeItemContentPlaylist(
                            forceDark = isDark,
                            onClick = {
                                navController.navigate(
                                    AlbumDestination(
                                        single.browseId,
                                    ),
                                )
                            },
                            data = single,
                            thumbSize = 180.dp,
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Albums
        AnimatedVisibility(
            state.data.albums != null &&
                state.data.albums!!
                    .results
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.albums),
                        style = typo().labelMedium,
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            if (state.data.channelId != null) {
                                val id = "MPAD${state.data.channelId}"
                                navController.navigate(
                                    MoreAlbumsDestination(
                                        id = id,
                                        type = MoreAlbumsDestination.ALBUM_TYPE,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(getStringBlocking(Res.string.error))
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = contentColor,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.albums?.results ?: emptyList()) { album ->
                        HomeItemContentPlaylist(
                            forceDark = isDark,
                            onClick = {
                                navController.navigate(
                                    AlbumDestination(
                                        browseId = album.browseId,
                                    ),
                                )
                            },
                            data = album,
                            thumbSize = 180.dp,
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Keep artist music videos as a dedicated feed. Each item plays by itself and does not
        // seed an audio radio queue; More opens the source feed rather than a playlist queue.
        val visibleArtistVideos =
            state.data.video?.video.orEmpty().filterNot { LiveStreamType.isLiveStream(it.isLiveVideo, it.title) }
        AnimatedVisibility(visibleArtistVideos.isNotEmpty()) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.videos_inArtist),
                        style = typo().labelMedium,
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.data.videoListParam != null) {
                        TextButton(
                            onClick = {
                                state.data.videoListParam?.let { browseId ->
                                    navController.navigate(
                                        BrowseDestination(
                                            browseId = browseId,
                                            params = state.data.videoListBrowseParams,
                                            title = getStringBlocking(Res.string.videos_inArtist),
                                        ),
                                    )
                                }
                            },
                            colors = ButtonDefaults.textButtonColors().copy(contentColor = contentColor),
                        ) {
                            Text(stringResource(Res.string.more), style = typo().bodySmall)
                        }
                    }
                }
                LazyRow(verticalAlignment = Alignment.CenterVertically) {
                    item { Spacer(Modifier.size(10.dp)) }
                    items(visibleArtistVideos, key = { it.videoId }) { video ->
                        HomeItemVideo(
                            forceDark = isDark,
                            onClick = { viewModel.playVideo(video) },
                            onLongClick = {},
                            data = video.toVideoContent(),
                        )
                    }
                    item { Spacer(Modifier.size(10.dp)) }
                }
            }
        }

        // Feature on
        AnimatedVisibility(state.data.featuredOn.isNotEmpty()) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.featured_inArtist),
                        style = typo().labelMedium,
                        color = contentColor,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp),
                    )
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.featuredOn) { feature ->
                        HomeItemContentPlaylist(
                            forceDark = isDark,
                            onClick = {
                                navController.navigate(
                                    PlaylistDestination(
                                        feature.id,
                                    ),
                                )
                            },
                            data = feature,
                            thumbSize = 180.dp,
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Related
        AnimatedVisibility(
            state.data.related != null &&
                state.data.related!!
                    .results
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.related_artists),
                        style = typo().labelMedium,
                        color = contentColor,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp),
                    )
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.related?.results ?: emptyList()) { related ->
                        HomeItemArtist(
                            forceDark = isDark,
                            onClick = {
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = related.browseId,
                                    ),
                                )
                            },
                            data =
                                Content(
                                    album = null,
                                    artists =
                                        listOf(
                                            Artist(
                                                id = related.browseId,
                                                name = related.title,
                                            ),
                                        ),
                                    description = related.subscribers,
                                    isExplicit = null,
                                    playlistId = null,
                                    browseId = related.browseId,
                                    thumbnails = related.thumbnails,
                                    title = related.title,
                                    videoId = null,
                                    views = null,
                                    durationSeconds = null,
                                    radio = null,
                                ),
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Text(
                text = stringResource(Res.string.description),
                style = typo().labelMedium,
                color = contentColor,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
            )
        }
        val urlHandler = LocalUriHandler.current
        ElevatedCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors =
                CardDefaults.elevatedCardColors().copy(
                    containerColor =
                        if (isDark) {
                            descriptionTint.rgbFactor(0.5f)
                        } else {
                            androidx.compose.ui.graphics.lerp(descriptionTint, Color.White, 0.82f)
                        },
                ),
        ) {
            DescriptionView(
                modifier = Modifier.padding(16.dp),
                text = state.data.description ?: stringResource(Res.string.no_description),
                limitLine = 5,
                onTimeClicked = {},
                onURLClicked = { url ->
                    urlHandler.openUri(url)
                },
            )
        }
        EndOfPage()
    }
}

private fun Track.toVideoContent(): Content =
    Content(
        album = album,
        artists = artists,
        description = null,
        isExplicit = isExplicit,
        playlistId = null,
        browseId = null,
        thumbnails = thumbnails.orEmpty(),
        title = title,
        videoId = videoId,
        views = views,
        durationSeconds = durationSeconds,
        radio = null,
        videoType = videoType,
    )

/**
 * "Liked songs" as a section of its own: the heading, then the artist's picture wearing the liked
 * heart beside "3 songs" / "By <artist>". The whole block opens
 * [LibraryDynamicPlaylistType.ArtistLiked]. Heading and text colours follow the active app theme.
 */
@Composable
private fun LikedSongsSection(
    imageUrl: String?,
    count: Int,
    artistName: String,
    contentColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
) {
    Column {
        // No "More" button beside it, so the padding stands in for the height the TextButton gives
        // the Popular and Singles headings.
        Text(
            text = stringResource(Res.string.liked_songs),
            style = typo().labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Box(modifier = Modifier.size(48.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
                Image(
                    painter = painterResource(Res.drawable.baseline_favorite_24),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .padding(3.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = pluralStringResource(Res.plurals.liked_songs_count, count, count),
                    style = typo().titleSmall,
                    color = contentColor,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(Res.string.liked_songs_by, artistName),
                    style = typo().bodySmall,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
