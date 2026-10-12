package com.petedev.wavemusic.ui.component

import com.petedev.wavemusic.resources.copy_action
import com.petedev.wavemusic.resources.decrease_pitch
import com.petedev.wavemusic.resources.decrease_speed
import com.petedev.wavemusic.resources.favorite_checked
import com.petedev.wavemusic.resources.favorite_unchecked
import com.petedev.wavemusic.resources.increase_pitch
import com.petedev.wavemusic.resources.increase_speed
import com.petedev.wavemusic.resources.minutes_short
import com.petedev.wavemusic.resources.selected
import com.petedev.wavemusic.resources.sleep_timer_end_label
import com.petedev.wavemusic.resources.theme_color_custom
import com.petedev.wavemusic.resources.lyrics_offset_using_global
import com.petedev.wavemusic.resources.lyrics_offset_song_scope

import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import com.petedev.wavemusic.ui.component.CompactHeaderContentHeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.DialogTextButton as TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.petedev.wavemusic.data.io.readLocalImageBytes
import com.petedev.wavemusic.domain.data.entities.DownloadState
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.download.DownloadProgress
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Artist
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.utils.FilterState
import com.petedev.wavemusic.domain.utils.connectArtists
import com.petedev.wavemusic.domain.utils.toListName
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.expect.copyToClipboard
import com.petedev.wavemusic.expect.shareUrl
import com.petedev.wavemusic.expect.ui.persistPickedImage
import com.petedev.wavemusic.expect.ui.photoPickerResult
import com.petedev.wavemusic.extension.displayNameRes
import com.petedev.wavemusic.extension.greyScale
import com.petedev.wavemusic.ui.icon.AccessAlarm
import com.petedev.wavemusic.ui.icon.Add
import com.petedev.wavemusic.ui.icon.AddCircleOutline
import com.petedev.wavemusic.ui.icon.AddPhotoAlternate
import com.petedev.wavemusic.ui.icon.Album
import com.petedev.wavemusic.ui.icon.CheckCircle
import com.petedev.wavemusic.ui.icon.ContentCopy
import com.petedev.wavemusic.ui.icon.Delete
import com.petedev.wavemusic.ui.icon.Done
import com.petedev.wavemusic.ui.icon.DownloadForOffline
import com.petedev.wavemusic.ui.icon.DownloadForOfflineOutlined
import com.petedev.wavemusic.ui.icon.Downloading
import com.petedev.wavemusic.ui.icon.Edit
import com.petedev.wavemusic.ui.icon.FavoriteBorder
import com.petedev.wavemusic.ui.icon.KeyboardArrowDown
import com.petedev.wavemusic.ui.icon.KeyboardDoubleArrowDown
import com.petedev.wavemusic.ui.icon.KeyboardDoubleArrowUp
import com.petedev.wavemusic.ui.icon.Lyrics
import com.petedev.wavemusic.ui.icon.PeopleAlt
import com.petedev.wavemusic.ui.icon.PlayCircle
import com.petedev.wavemusic.ui.icon.PlaylistAdd
import com.petedev.wavemusic.ui.icon.QueueMusic
import com.petedev.wavemusic.ui.icon.Remove
import com.petedev.wavemusic.ui.icon.Sensors
import com.petedev.wavemusic.ui.icon.Share
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.Speed
import com.petedev.wavemusic.ui.icon.Sync
import com.petedev.wavemusic.ui.icon.SyncDisabled
import com.petedev.wavemusic.ui.icon.Tune
import com.petedev.wavemusic.ui.icon.Update
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.theme.seed
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.NowPlayingBottomSheetUIEvent
import com.petedev.wavemusic.viewModel.NowPlayingBottomSheetViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.add_to_a_playlist
import com.petedev.wavemusic.resources.add_to_queue
import com.petedev.wavemusic.resources.album
import com.petedev.wavemusic.resources.artists
import com.petedev.wavemusic.resources.baseline_favorite_24
import com.petedev.wavemusic.resources.better_lyrics
import com.petedev.wavemusic.resources.bitrate
import com.petedev.wavemusic.resources.bpm
import com.petedev.wavemusic.resources.can_not_be_empty
import com.petedev.wavemusic.resources.cancel
import com.petedev.wavemusic.resources.crop_cover
import com.petedev.wavemusic.resources.codec
import com.petedev.wavemusic.resources.copied_to_clipboard
import com.petedev.wavemusic.resources.delete
import com.petedev.wavemusic.resources.delete_playlist
import com.petedev.wavemusic.resources.delete_playlist_confirmation
import com.petedev.wavemusic.resources.description
import com.petedev.wavemusic.resources.download
import com.petedev.wavemusic.resources.download_speed
import com.petedev.wavemusic.resources.download_this_song_video_file_to_your_device
import com.petedev.wavemusic.resources.downloaded
import com.petedev.wavemusic.resources.downloading
import com.petedev.wavemusic.resources.downloading_audio
import com.petedev.wavemusic.resources.downloading_video
import com.petedev.wavemusic.resources.loading
import com.petedev.wavemusic.resources.edit_thumbnail
import com.petedev.wavemusic.resources.edit_title
import com.petedev.wavemusic.resources.endless_queue
import com.petedev.wavemusic.resources.error_occurred
import com.petedev.wavemusic.resources.extract_source
import com.petedev.wavemusic.resources.itag
import com.petedev.wavemusic.resources.key
import com.petedev.wavemusic.resources.like
import com.petedev.wavemusic.resources.like_and_dislike
import com.petedev.wavemusic.resources.liked
import com.petedev.wavemusic.resources.list_all_cookies_of_this_page
import com.petedev.wavemusic.resources.lrclib
import com.petedev.wavemusic.resources.main_lyrics_provider
import com.petedev.wavemusic.resources.prioritize_word_synced_lyrics
import com.petedev.wavemusic.resources.better_lyrics_portato
import com.petedev.wavemusic.resources.youlyplus
import com.petedev.wavemusic.resources.kugou
import com.petedev.wavemusic.resources.unison
import com.petedev.wavemusic.resources.change
import com.petedev.wavemusic.resources.create_playlist
import com.petedev.wavemusic.resources.already_in_playlist
import com.petedev.wavemusic.resources.merging_audio_and_video
import com.petedev.wavemusic.resources.mime_type
import com.petedev.wavemusic.resources.move_down
import com.petedev.wavemusic.resources.move_up
import com.petedev.wavemusic.resources.no_album
import com.petedev.wavemusic.resources.no_description
import com.petedev.wavemusic.resources.no_playlist_found
import com.petedev.wavemusic.resources.now_playing
import com.petedev.wavemusic.resources.now_playing_upper
import com.petedev.wavemusic.resources.ok
import com.petedev.wavemusic.resources.pitch
import com.petedev.wavemusic.resources.playback_speed
import com.petedev.wavemusic.resources.playback_speed_pitch
import com.petedev.wavemusic.resources.playback_speed_pitch_disabled
import com.petedev.wavemusic.resources.playlist_name
import com.petedev.wavemusic.resources.playlist_name_cannot_be_empty
import com.petedev.wavemusic.resources.plays
import com.petedev.wavemusic.resources.processing
import com.petedev.wavemusic.resources.queue
import com.petedev.wavemusic.resources.radio
import com.petedev.wavemusic.resources.refetch_lyrics
import com.petedev.wavemusic.resources.remove_from_playlist
import com.petedev.wavemusic.resources.save
import com.petedev.wavemusic.resources.scale
import com.petedev.wavemusic.resources.set
import com.petedev.wavemusic.resources.share
import com.petedev.wavemusic.resources.share_url
import com.petedev.wavemusic.resources.simpmusic_lyrics
import com.petedev.wavemusic.resources.sleep_minutes
import com.petedev.wavemusic.resources.sleep_timer
import com.petedev.wavemusic.resources.sleep_timer_end_of_song
import com.petedev.wavemusic.resources.sleep_timer_off
import com.petedev.wavemusic.resources.sleep_timer_set_error
import com.petedev.wavemusic.resources.sort_by
import com.petedev.wavemusic.resources.sync
import com.petedev.wavemusic.resources.sync_first
import com.petedev.wavemusic.resources.synced
import com.petedev.wavemusic.resources.title
import com.petedev.wavemusic.resources.to_download_folder
import com.petedev.wavemusic.resources.unknown
import com.petedev.wavemusic.resources.update_playlist
import com.petedev.wavemusic.resources.warning
import com.petedev.wavemusic.resources.your_discord_token
import com.petedev.wavemusic.resources.your_sp_dc_param_of_spotify_cookie
import com.petedev.wavemusic.resources.your_youtube_cookie
import com.petedev.wavemusic.resources.youtube_transcript
import com.petedev.wavemusic.resources.youtube_url

// ─────────────────────────────────────────────────────────────────────────────
// Sentinel value used by SleepTimerBottomSheet to signal "end of current song"
// Handle this in NowPlayingBottomSheetViewModel / SharedViewModel:
//   if (minutes == END_OF_SONG_SENTINEL) → stop after current track finishes
// ─────────────────────────────────────────────────────────────────────────────
const val END_OF_SONG_SENTINEL = Int.MAX_VALUE

@ExperimentalMaterial3Api
@Composable
fun InfoPlayerBottomSheet(
    onDismiss: () -> Unit,
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val windowInsets = WindowInsets.systemBars
    val scrollState = rememberScrollState()
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    val songEntity by sharedViewModel.nowPlayingState.map { it?.songEntity }.collectAsState(null)
    val format by sharedViewModel.format.collectAsState(null)
    val extractSource by sharedViewModel.extractSource.collectAsState()
    val downloadProgress by sharedViewModel.downloadFileProgress.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        containerColor = rememberSurfaceDarkColors().container,
        contentColor = Color.Transparent,
        dragHandle = {},
        scrimColor = Color.Black.copy(alpha = .5f),
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RectangleShape,
    ) {
        // This dialog MUST stay inside the sheet's content lambda. A Dialog is its own window
        // (Android ComponentDialog) / scene layer (skiko), so nothing in the layout tree orders
        // it — the layer attached LAST wins, and DisposableEffects attach in composition order.
        // Written as a sibling BEFORE ModalBottomSheet it lost to the sheet whenever both entered
        // composition in the same pass: reopening the sheet mid-download, or an Android config
        // change (downloadProgress lives in the ViewModel, so it survives this composable leaving).
        // Nested here, the sheet's layer necessarily exists first, so the dialog is always on top.
        if (downloadProgress != DownloadProgress.INIT) {
            val dialogPalette = rememberAppDialogPalette()
            BasicAlertDialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                modifier = Modifier.wrapContentSize(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, dialogPalette.border, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = dialogPalette.container,
                    contentColor = dialogPalette.content,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        Modifier.padding(
                            horizontal = 20.dp,
                            vertical = 20.dp,
                        ),
                    ) {
                        Text(
                            stringResource(Res.string.downloading),
                            style = typo().headlineMedium,
                        )
                        Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!downloadProgress.isDone && !downloadProgress.isError) {
                                CircularProgressIndicator()
                                Spacer(Modifier.size(15.dp))
                            }
                            Crossfade(downloadProgress) {
                                if (it.isMerging) {
                                    Text(
                                        text = stringResource(Res.string.merging_audio_and_video),
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        style = typo().bodyMedium,
                                    )
                                } else if (it.isError) {
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.error_occurred),
                                            modifier = Modifier.padding(vertical = 5.dp),
                                            style = typo().bodyMedium,
                                        )
                                        Text(
                                            text = downloadProgress.errorMessage,
                                            modifier = Modifier.padding(bottom = 5.dp),
                                            maxLines = 2,
                                            style = typo().bodyMedium,
                                        )
                                    }
                                } else if (it.isDone) {
                                    Text(
                                        text =
                                            stringResource(Res.string.downloaded) +
                                                stringResource(Res.string.to_download_folder)
                                                    .replace("\"", ""),
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        style = typo().bodyMedium,
                                    )
                                } else {
                                    Column {
                                        if (it.audioDownloadProgress != 0f) {
                                            Text(
                                                text =
                                                    stringResource(
                                                        Res.string.downloading_audio,
                                                        (downloadProgress.audioDownloadProgress * 100).toString() + "%",
                                                    ),
                                                modifier = Modifier.padding(vertical = 5.dp),
                                                style = typo().bodyMedium,
                                            )
                                        }
                                        if (it.videoDownloadProgress != 0f) {
                                            Text(
                                                text =
                                                    stringResource(
                                                        Res.string.downloading_video,
                                                        (downloadProgress.videoDownloadProgress * 100).toString() + "%",
                                                    ),
                                                modifier = Modifier.padding(vertical = 5.dp),
                                                style = typo().bodyMedium,
                                            )
                                        }
                                        if (downloadProgress.downloadSpeed != 0) {
                                            Text(
                                                text =
                                                    stringResource(
                                                        Res.string.download_speed,
                                                        downloadProgress.downloadSpeed.toString() + " kb/s",
                                                    ),
                                                modifier = Modifier.padding(vertical = 5.dp),
                                                style = typo().bodyMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Crossfade(downloadProgress) {
                            if (it.isError) {
                                Column {
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedButton(onClick = {
                                        sharedViewModel.downloadFileDone()
                                    }) { Text(stringResource(Res.string.ok)) }
                                }
                            }
                            if (it.isDone) {
                                Column {
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedButton(onClick = {
                                        sharedViewModel.downloadFileDone()
                                    }) { Text(stringResource(Res.string.ok)) }
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier =
                Modifier.dialogMotionSource(clearPreviousOrigin = true)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            shape = RectangleShape,
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(scrollState)
                        .padding(
                            top =
                                with(localDensity) {
                                    windowInsets.getTop(localDensity).toDp()
                                },
                        ),
            ) {
                TopAppBar(
                    expandedHeight = CompactHeaderContentHeight,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors =
                        TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = Color.Transparent,
                        ),
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.now_playing_upper),
                                style = typo().bodyMedium,
                                color = rememberSurfaceDarkColors().content,
                            )
                            Text(
                                text = screenDataState.nowPlayingTitle,
                                style = typo().labelMedium,
                                color = rememberSurfaceDarkColors().content,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately,
                                        ).focusable(),
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        }) {
                            Icon(
                                imageVector = WaveIcons.KeyboardArrowDown,
                                contentDescription = "",
                                tint = rememberSurfaceDarkColors().content,
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.size(48.dp),
                        )
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.title),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = screenDataState.nowPlayingTitle,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .padding(horizontal = 10.dp)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.artists),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = screenDataState.artistName,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.album),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = songEntity?.albumName ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.itag),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.itag?.toString() ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.mime_type),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.mimeType ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.codec),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.codecs ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.bitrate),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.bitrate?.toString() ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.bpm),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.bpm?.toString() ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.key),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.musicKey ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.scale),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = format?.keyScale ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(Res.string.extract_source),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = extractSource ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(Res.string.plays),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = screenDataState.songInfoData?.viewCount?.toString() ?: stringResource(Res.string.unknown),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.like),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.like_and_dislike,
                            screenDataState.songInfoData?.like ?: 0,
                            screenDataState.songInfoData?.dislike ?: 0,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.description),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text = screenDataState.songInfoData?.description ?: stringResource(Res.string.no_description),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .padding(horizontal = 10.dp),
                    style = typo().bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.youtube_url),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = typo().labelMedium,
                    color = rememberSurfaceDarkColors().content,
                )
                Text(
                    text =
                        buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "https://music.youtube.com/watch?v=${songEntity?.videoId}",
                                    TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline)),
                                ),
                            ) {
                                append("https://music.youtube.com/watch?v=${songEntity?.videoId}")
                            }
                        },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable(),
                    style = typo().bodyMedium,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    enabled = screenDataState.bitmap != null,
                    onClick = {
                        sharedViewModel.downloadFile(
                            bitmap = screenDataState.bitmap ?: return@OutlinedButton,
                        )
                    },
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 10.dp),
                ) {
                    Text(text = stringResource(Res.string.download_this_song_video_file_to_your_device))
                }
                Spacer(modifier = Modifier.height(10.dp))

                EndOfModalBottomSheet()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
fun QueueBottomSheet(
    onDismiss: () -> Unit,
    sharedViewModel: SharedViewModel = koinInject(),
    musicServiceHandler: MediaPlayerHandler = koinInject<MediaPlayerHandler>(),
    dataStoreManager: DataStoreManager = koinInject(),
) {
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val windowInsets = WindowInsets.systemBars
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    val lazyListState = rememberLazyListState()
    val dragDropState =
        rememberDragDropState(lazyListState) { from, to ->
            coroutineScope.launch {
                musicServiceHandler.swap(from, to)
            }
        }
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    var shouldShowQueueItemBottomSheet by rememberSaveable { mutableStateOf(false) }
    var clickMoreIndex by rememberSaveable { mutableIntStateOf(0) }
    var clickMoreVideoId by rememberSaveable { mutableStateOf<String?>(null) }
    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    val songEntity by sharedViewModel.nowPlayingState.map { it?.songEntity }.collectAsState(null)
    val queueData by musicServiceHandler.queueData.collectAsStateWithLifecycle()
    val queue by remember {
        derivedStateOf {
            queueData?.data?.listTracks ?: emptyList()
        }
    }
    val loadMoreState by remember {
        derivedStateOf {
            queueData?.queueState ?: QueueData.StateSource.STATE_CREATED
        }
    }
    val endlessQueueEnable by dataStoreManager.endlessQueue.map { it == DataStoreManager.TRUE }.collectAsState(false)

    val shouldLoadMore =
        remember {
            derivedStateOf {
                val layoutInfo = lazyListState.layoutInfo
                val lastVisibleItem =
                    layoutInfo.visibleItemsInfo.lastOrNull()
                        ?: return@derivedStateOf true

                lastVisibleItem.index >= layoutInfo.totalItemsCount - 3 && layoutInfo.totalItemsCount > 0
            }
        }

    // Convert the state into a cold flow and collect
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .collect {
                // if should load more, then invoke loadMore
                if (it && loadMoreState == QueueData.StateSource.STATE_INITIALIZED) musicServiceHandler.loadMore()
            }
    }

    LaunchedEffect(queue) {
        Logger.w("QueueBottomSheet", "queue: $queue")
    }

    DisposableEffect(Unit) {
        val currentSongIndex = musicServiceHandler.currentOrderIndex().takeIf { i -> i > -1 } ?: 0
        Logger.d("QueueBottomSheet", "currentSongIndex: $currentSongIndex")
        coroutineScope.launch {
            lazyListState.requestScrollToItem(currentSongIndex)
        }
        onDispose { }
    }

    val showQueueItemBottomSheet: (Int) -> Unit = { index ->
        clickMoreIndex = index
        clickMoreVideoId = queue.getOrNull(index)?.videoId
        shouldShowQueueItemBottomSheet = true
    }

    if (shouldShowQueueItemBottomSheet) {
        QueueItemBottomSheet(
            onDismiss = { shouldShowQueueItemBottomSheet = false },
            index = clickMoreIndex,
            videoId = clickMoreVideoId,
            musicServiceHandler = musicServiceHandler,
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        containerColor = rememberSurfaceDarkColors().container,
        contentColor = Color.Transparent,
        dragHandle = {},
        scrimColor = Color.Black.copy(alpha = .5f),
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RectangleShape,
    ) {
        Card(
            modifier =
                Modifier.dialogMotionSource(clearPreviousOrigin = true)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            shape = RectangleShape,
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        top =
                            with(localDensity) {
                                windowInsets.getTop(localDensity).toDp()
                            },
                    ),
            ) {
                TopAppBar(
                    expandedHeight = CompactHeaderContentHeight,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors =
                        TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = Color.Transparent,
                        ),
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.now_playing_upper),
                                style = typo().bodyMedium,
                                color = rememberSurfaceDarkColors().content,
                            )
                            Text(
                                text = screenDataState.playlistName,
                                style = typo().labelMedium,
                                color = rememberSurfaceDarkColors().content,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately,
                                        ).focusable(),
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        }) {
                            Icon(
                                imageVector = WaveIcons.KeyboardArrowDown,
                                contentDescription = "",
                                tint = rememberSurfaceDarkColors().content,
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.size(32.dp),
                        )
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.now_playing),
                    style = typo().titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                SongFullWidthItems(
                    songEntity = songEntity,
                    isPlaying = false,
                    onAddToQueue = null,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.queue),
                        style = typo().titleMedium,
                        modifier =
                            Modifier
                                .padding(horizontal = 20.dp)
                                .weight(1f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.endless_queue),
                            style = typo().bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Switch(
                            checked = endlessQueueEnable,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    dataStoreManager.setEndlessQueue(it)
                                }
                            },
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                val queueRowKeys =
                    remember(queue) {
                        val seen = HashMap<String, Int>(queue.size)
                        queue.map { track ->
                            val occurrence = seen.getOrElse(track.videoId) { 0 }
                            seen[track.videoId] = occurrence + 1
                            "${track.videoId}#$occurrence"
                        }
                    }
                LazyColumn(
                    horizontalAlignment = Alignment.Start,
                    state = lazyListState,
                    modifier =
                        Modifier
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDrag = { change, offset ->
                                        Logger.d("QueueBottomSheet", "onDrag $offset")
                                        change.consume()
                                        dragDropState.onDrag(offset = offset)

                                        if (overscrollJob?.isActive == true) {
                                            return@detectDragGesturesAfterLongPress
                                        }

                                        dragDropState
                                            .checkForOverScroll()
                                            .takeIf { it != 0f }
                                            ?.let {
                                                overscrollJob =
                                                    coroutineScope.launch {
                                                        dragDropState.state.animateScrollBy(
                                                            it * 1.3f,
                                                            tween(easing = FastOutLinearInEasing),
                                                        )
                                                    }
                                            }
                                            ?: run { overscrollJob?.cancel() }
                                    },
                                    onDragStart = { offset ->
                                        Logger.d("QueueBottomSheet", "onDragStart $offset")
                                        dragDropState.onDragStart(offset)
                                    },
                                    onDragEnd = {
                                        Logger.d("QueueBottomSheet", "onDragEnd")
                                        dragDropState.onDragInterrupted(true)
                                        overscrollJob?.cancel()
                                    },
                                    onDragCancel = {
                                        Logger.d("QueueBottomSheet", "onDragCancel")
                                        dragDropState.onDragInterrupted()
                                        overscrollJob?.cancel()
                                    },
                                )
                            },
                ) {
                    itemsIndexed(
                        queue,
                        // Keyed by the track, NOT by its position: a radio queue drops played
                        // tracks off the front, and a position-based key changes for every row
                        // when that happens, so the list loses its scroll anchor and jumps under
                        // the user. The occurrence number keeps the key unique when a radio
                        // repeats a song.
                        key = { i, t -> queueRowKeys.getOrElse(i) { t.videoId } },
                    ) { index, track ->
                        if (index != -1) {
                            val rowKey = queueRowKeys.getOrElse(index) { track.videoId }
                            DraggableItem(
                                dragDropState = dragDropState,
                                index = index,
                                modifier = Modifier,
                                itemKey = rowKey,
                            ) { _ ->
                                SongFullWidthItems(
                                    track = track,
                                    isPlaying = track.videoId == songEntity?.videoId,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(),
                                    onClickListener = { videoId ->
                                        if (videoId == track.videoId) {
                                            musicServiceHandler.playMediaItemInMediaSource(index)
                                        }
                                    },
                                    onMoreClickListener = {
                                        showQueueItemBottomSheet(index)
                                    },
                                    onAddToQueue = {
                                        sharedViewModel.addListToQueue(
                                            arrayListOf(track),
                                        )
                                    },
                                )
                            }
                        }
                    }
                    item {
                        if (loadMoreState == QueueData.StateSource.STATE_INITIALIZING) {
                            CenterLoadingBox(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                            )
                        }
                    }
                    item {
                        EndOfPage(bottomPaddingAlreadyApplied = LocalPageBottomInset.current)
                    }
                }
            }
        }
    }
}

private enum class QueueItemAction {
    UP,
    DOWN,
    DELETE,
}

@Composable
@ExperimentalMaterial3Api
fun QueueItemBottomSheet(
    onDismiss: () -> Unit,
    index: Int,
    /** The track that was at [index] when this sheet opened; its actions only run if it still is. */
    videoId: String?,
    musicServiceHandler: MediaPlayerHandler = koinInject<MediaPlayerHandler>(),
) {
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    val hideModalBottomSheet: () -> Unit =
        {
            coroutineScope.launch {
                modelBottomSheetState.hide()
                onDismiss()
            }
        }
    val listAction =
        listOf(
            QueueItemAction.UP,
            QueueItemAction.DOWN,
            QueueItemAction.DELETE,
        )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier =
                Modifier.dialogMotionSource(clearPreviousOrigin = true)
                    .fillMaxWidth()
                    .wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                Card(
                    modifier =
                        Modifier
                            .width(60.dp)
                            .height(4.dp),
                    colors =
                        CardDefaults.cardColors().copy(
                            containerColor = rememberSurfaceDarkColors().handle,
                        ),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(modifier = Modifier.height(5.dp))
                LazyColumn {
                    val canMoveUp =
                        index > 0 &&
                            index < (
                                musicServiceHandler.queueData.value
                                    ?.data
                                    ?.listTracks
                                    ?.size ?: 0
                            )
                    val canMoveDown =
                        index >= 0 &&
                            index < (
                                musicServiceHandler.queueData.value
                                    ?.data
                                    ?.listTracks
                                    ?.size ?: 0
                            ) - 1
                    items(listAction) { action ->
                        val disable =
                            when (action) {
                                QueueItemAction.UP -> !canMoveUp
                                QueueItemAction.DOWN -> !canMoveDown
                                QueueItemAction.DELETE -> false
                            }
                        if (disable) return@items
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        hideModalBottomSheet()
                                        // These act by POSITION, and a radio trims its played history
                                        // off the front while this sheet can be open, which moves every
                                        // row. If the track is no longer at [index], do nothing rather
                                        // than move or delete whatever slid into its place.
                                        val stillThere =
                                            musicServiceHandler.queueData.value
                                                ?.data
                                                ?.listTracks
                                                ?.getOrNull(index)
                                                ?.videoId == videoId
                                        if (!stillThere) return@clickable
                                        when (action) {
                                            QueueItemAction.UP -> {
                                                coroutineScope.launch {
                                                    musicServiceHandler.moveItemUp(index)
                                                }
                                            }

                                            QueueItemAction.DOWN -> {
                                                coroutineScope.launch {
                                                    musicServiceHandler.moveItemDown(index)
                                                }
                                            }

                                            QueueItemAction.DELETE -> {
                                                musicServiceHandler.removeMediaItem(index)
                                            }
                                        }
                                    },
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .padding(20.dp)
                                        .align(Alignment.CenterStart),
                            ) {
                                when (action) {
                                    QueueItemAction.UP -> {
                                        Image(
                                            imageVector = WaveIcons.KeyboardDoubleArrowUp,
                                            contentDescription = stringResource(Res.string.move_up),
                                            colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                        )
                                    }

                                    QueueItemAction.DOWN -> {
                                        Image(
                                            imageVector = WaveIcons.KeyboardDoubleArrowDown,
                                            contentDescription = stringResource(Res.string.move_down),
                                            colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                        )
                                    }

                                    QueueItemAction.DELETE -> {
                                        Image(
                                            imageVector = WaveIcons.Delete,
                                            contentDescription = stringResource(Res.string.delete),
                                            colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text =
                                        stringResource(
                                            when (action) {
                                                QueueItemAction.UP -> Res.string.move_up
                                                QueueItemAction.DOWN -> Res.string.move_down
                                                QueueItemAction.DELETE -> Res.string.delete
                                            },
                                        ),
                                    style = typo().labelSmall,
                                )
                            }
                        }
                    }
                    item {
                        EndOfModalBottomSheet()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NowPlayingBottomSheet(
    onDismiss: () -> Unit,
    navController: NavController,
    song: SongEntity?,
    viewModel: NowPlayingBottomSheetViewModel = koinViewModel(),
    setSleepTimerEnable: Boolean = false,
    changeMainLyricsProviderEnable: Boolean = false,
    onChangeLyricsProvider: (String) -> Unit = {},
    onRefetchLyrics: () -> Unit = {},
    onNavigateToOtherScreen: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onLibraryDelete: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    showAddToPlaylist: Boolean = true,
    dataStoreManager: DataStoreManager = koinInject<DataStoreManager>(),
    showFavoriteAction: Boolean = true,
) {
    val menuPalette = rememberAppDialogPalette()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    val hideModalBottomSheet: () -> Unit =
        {
            coroutineScope.launch {
                modelBottomSheetState.hide()
                onDismiss()
            }
        }

    var addToAPlaylist by remember { mutableStateOf(false) }
    var artist by remember { mutableStateOf(false) }
    var mainLyricsProvider by remember { mutableStateOf(false) }
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var changePlaybackSpeedPitch by remember { mutableStateOf(false) }
    val crossfadeEnabled by dataStoreManager.crossfadeEnabled.collectAsState(DataStoreManager.TRUE)
    val prioritizeWordSyncedLyrics by dataStoreManager.prioritizeWordSyncedLyrics.collectAsStateWithLifecycle(
        initialValue = DataStoreManager.TRUE,
    )

    LaunchedEffect(uiState) {
        if (uiState.songUIState.videoId.isNotEmpty() && !isBottomSheetVisible) {
            isBottomSheetVisible = true
        }
    }

    LaunchedEffect(key1 = song) {
        viewModel.setSongEntity(song)
    }

    if (changePlaybackSpeedPitch) {
        val playbackSpeed by dataStoreManager.playbackSpeed.collectAsState(1f)
        val pitch by dataStoreManager.pitch.collectAsState(0)
        PlaybackSpeedPitchBottomSheet(
            onDismiss = { changePlaybackSpeedPitch = false },
            playbackSpeed = playbackSpeed,
            pitch = pitch,
        ) { speed, p ->
            viewModel.onUIEvent(
                NowPlayingBottomSheetUIEvent.ChangePlaybackSpeedPitch(
                    speed = speed,
                    pitch = p,
                ),
            )
        }
    }

    if (addToAPlaylist) {
        AddToPlaylistModalBottomSheet(
            isBottomSheetVisible = true,
            listLocalPlaylist = uiState.listLocalPlaylist,
            listYouTubePlaylist = uiState.listYouTubePlaylist,
            onDismiss = { addToAPlaylist = false },
            onClick = {
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToPlaylist(it.id))
            },
            onYTPlaylistClick = {
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToYouTubePlaylist(it.browseId, it.title))
            },
            videoId = uiState.songUIState.videoId,
            onCreatePlaylist = viewModel.playlistCreationAction(),
        )
    }
    if (artist) {
        ArtistModalBottomSheet(
            isBottomSheetVisible = true,
            artists = uiState.songUIState.listArtists,
            navController = navController,
            onBeforeNavigate = {
                modelBottomSheetState.hide()
                onDismiss()
                onNavigateToOtherScreen()
            },
            onDismiss = { artist = false },
        )
    }

    if (mainLyricsProvider) {
        var selectedProvider by remember(uiState.mainLyricsProvider) { mutableStateOf(uiState.mainLyricsProvider) }
        var providerSelectionChanged by remember(uiState.mainLyricsProvider) { mutableStateOf(false) }
        val providerOptions =
            listOf(
                DataStoreManager.SIMPMUSIC to stringResource(Res.string.simpmusic_lyrics),
                DataStoreManager.LRCLIB to stringResource(Res.string.lrclib),
                DataStoreManager.YOUTUBE to stringResource(Res.string.youtube_transcript),
                DataStoreManager.BETTER_LYRICS to stringResource(Res.string.better_lyrics),
                DataStoreManager.BETTER_LYRICS_PORTATO to stringResource(Res.string.better_lyrics_portato),
                DataStoreManager.YOULYPLUS to stringResource(Res.string.youlyplus),
                DataStoreManager.KUGOU to stringResource(Res.string.kugou),
                DataStoreManager.UNISON to stringResource(Res.string.unison),
            )

        AmoledAlertDialog(
            onDismissRequest = { mainLyricsProvider = false },
            title = {
                Text(
                    text = stringResource(Res.string.main_lyrics_provider),
                    style = typo().titleMedium,
                )
            },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            coroutineScope.launch {
                                dataStoreManager.setPrioritizeWordSyncedLyrics(prioritizeWordSyncedLyrics != DataStoreManager.TRUE)
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = prioritizeWordSyncedLyrics == DataStoreManager.TRUE,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch { dataStoreManager.setPrioritizeWordSyncedLyrics(enabled) }
                            },
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(Res.string.prioritize_word_synced_lyrics), style = typo().labelSmall)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    providerOptions.forEach { (provider, label) ->
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth().clickable {
                                selectedProvider = provider
                                providerSelectionChanged = true
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedProvider == provider,
                                onClick = {
                                    selectedProvider = provider
                                    providerSelectionChanged = true
                                },
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(text = label, style = typo().labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (providerSelectionChanged) onChangeLyricsProvider(selectedProvider)
                        mainLyricsProvider = false
                    },
                ) {
                    Text(text = stringResource(Res.string.change), style = typo().labelSmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { mainLyricsProvider = false }) {
                    Text(text = stringResource(Res.string.cancel), style = typo().labelSmall)
                }
            },
        )
    }

    if (isBottomSheetVisible) {
        AppDialogTheme {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = modelBottomSheetState,
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                dragHandle = null,
                scrimColor = Color.Black.copy(alpha = .5f),
                // Keep the menu header and scroll viewport below system bars. Bottom padding
                // is supplied by EndOfModalBottomSheet so it is not added twice.
                contentWindowInsets = {
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                },
            ) {
                Card(
                    modifier =
                    Modifier.dialogMotionSource(clearPreviousOrigin = true)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            width = 1.dp,
                            color = menuPalette.border,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = menuPalette.container),
                ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier =
                            Modifier
                                .width(60.dp)
                                .height(4.dp),
                        colors =
                            CardDefaults.cardColors().copy(
                                containerColor = menuPalette.secondaryContent.copy(alpha = 0.4f),
                            ),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(65.dp)
                                .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val thumb = uiState.songUIState.thumbnails
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(LocalPlatformContext.current)
                                    .data(thumb)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .diskCacheKey(thumb)
                                    .crossfade(550)
                                    .build(),
                            placeholder = rememberHolderPainter(),
                            error = rememberHolderPainter(),
                            contentDescription = null,
                            contentScale = ContentScale.Inside,
                            modifier =
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .clip(RoundedCornerShape(10.dp))
                                    .size(60.dp),
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = uiState.songUIState.title,
                                style = typo().labelMedium,
                                // Keep the sheet header on the same theme-aware palette as its
                                // surface and the rest of the refreshed dialogs.
                                color = menuPalette.content,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .basicMarquee(animationMode = MarqueeAnimationMode.Immediately)
                                        .focusable(),
                            )
                            Text(
                                text =
                                    uiState.songUIState.listArtists
                                        .toListName()
                                        .connectArtists(),
                                style = typo().bodyMedium,
                                color = menuPalette.secondaryContent,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .basicMarquee(animationMode = MarqueeAnimationMode.Immediately)
                                        .focusable(),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        thickness = 1.dp,
                        color = menuPalette.border,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    ActionButton(
                        icon = WaveIcons.Share,
                        text = Res.string.share,
                    ) {
                        viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.Share)
                    }
                    val removeFromPlaylist = onRemoveFromPlaylist ?: onDelete
                    if (removeFromPlaylist != null) {
                        ActionButton(
                            icon = WaveIcons.Delete,
                            text = Res.string.remove_from_playlist,
                        ) {
                            hideModalBottomSheet()
                            removeFromPlaylist()
                        }
                    }
                    Crossfade(targetState = onLibraryDelete != null) {
                        if (it) {
                            ActionButton(
                                icon = WaveIcons.Delete,
                                text = Res.string.delete,
                            ) {
                                hideModalBottomSheet()
                                onLibraryDelete?.invoke()
                            }
                        }
                    }
                    if (showFavoriteAction && !changeMainLyricsProviderEnable) {
                        CheckBoxActionButton(
                            defaultChecked = uiState.songUIState.liked,
                            stateKey = uiState.songUIState.videoId,
                            isHeartIcon = true,
                            onChangeListener = { liked ->
                                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.SetLiked(liked))
                            },
                        )
                    }
                    ActionButton(
                        icon =
                            when (uiState.songUIState.downloadState) {
                                DownloadState.STATE_NOT_DOWNLOADED -> WaveIcons.DownloadForOfflineOutlined
                                DownloadState.STATE_DOWNLOADING -> WaveIcons.Downloading
                                DownloadState.STATE_DOWNLOADED -> WaveIcons.DownloadForOffline
                                DownloadState.STATE_PREPARING -> WaveIcons.Downloading
                                else -> WaveIcons.DownloadForOfflineOutlined
                            },
                        // The old baseline_downloaded.xml carried #FF00A0CB baked in; the shared
                        // symbol is neutral, so the "done" state has to say the colour out loud.
                        iconColor =
                            if (uiState.songUIState.downloadState == DownloadState.STATE_DOWNLOADED) {
                                Color(0xFF00A0CB)
                            } else {
                                Color.Unspecified
                            },
                        text =
                            when (uiState.songUIState.downloadState) {
                                DownloadState.STATE_NOT_DOWNLOADED -> Res.string.download
                                DownloadState.STATE_DOWNLOADING -> Res.string.downloading
                                DownloadState.STATE_DOWNLOADED -> Res.string.downloaded
                                DownloadState.STATE_PREPARING -> Res.string.downloading
                                else -> Res.string.download
                            },
                    ) {
                        viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.Download)
                    }
                    if (showAddToPlaylist) {
                        ActionButton(
                            icon = WaveIcons.PlaylistAdd,
                            text = Res.string.add_to_a_playlist,
                        ) {
                            viewModel.resetPlaylists()
                            addToAPlaylist = true
                        }
                    }
                    if (!uiState.songUIState.isVideo) {
                        ActionButton(
                            icon = WaveIcons.QueueMusic,
                            text = Res.string.add_to_queue,
                        ) {
                            viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToQueue)
                        }
                    }
                    ActionButton(
                        icon = WaveIcons.PeopleAlt,
                        text = Res.string.artists,
                    ) {
                        artist = true
                    }
                    ActionButton(
                        icon = WaveIcons.Album,
                        // Three states, not two. A track can carry an album ID with no title: the
                        // row it was parsed from links an album but never spells its name out.
                        // That case still navigates, so it must not read "No album" — but the name
                        // is genuinely unknown, so fall back to the generic label rather than
                        // showing a blank row. The parser deliberately leaves the name empty
                        // instead of inventing one, because a made-up title would travel out to
                        // MediaSession and into external scrobblers.
                        text =
                            when {
                                uiState.songUIState.album == null -> Res.string.no_album
                                uiState.songUIState.album
                                    ?.name
                                    .isNullOrBlank() -> Res.string.album
                                else -> null
                            },
                        textString =
                            uiState.songUIState.album
                                ?.name
                                ?.takeIf { it.isNotBlank() },
                        enable = uiState.songUIState.album != null,
                    ) {
                        uiState.songUIState.album?.id?.let { id ->
                            coroutineScope.launch {
                                modelBottomSheetState.hide()
                                onDismiss()
                                onNavigateToOtherScreen()
                                navController.navigate(AlbumDestination(browseId = id))
                            }
                        }
                    }
                    if (changeMainLyricsProviderEnable) {
                        ActionButton(
                            icon = WaveIcons.Sync,
                            text = Res.string.refetch_lyrics,
                        ) {
                            hideModalBottomSheet()
                            onRefetchLyrics()
                        }
                    }
                    AnimatedVisibility(
                        visible = changeMainLyricsProviderEnable,
                        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                        exit = fadeOut(tween(140)) + shrinkVertically(tween(140)),
                    ) {
                        Column {
                            ActionButton(
                                icon = WaveIcons.Lyrics,
                                text = Res.string.main_lyrics_provider,
                            ) {
                                mainLyricsProvider = true
                            }
                            key(uiState.songUIState.videoId) {
                                val songId = uiState.songUIState.videoId
                                val offsetState by remember(songId, dataStoreManager) {
                                    combine(dataStoreManager.getSongLyricsOffsetMs(songId), dataStoreManager.lyricsOffsetMs) { local, global -> local to global }
                                }.collectAsStateWithLifecycle<Pair<Int?, Int>?>(initialValue = null)
                                LyricsOffsetControl(
                                    offsetMs = offsetState?.let { it.first ?: it.second },
                                    onAdjustOffset = { deltaMs -> viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AdjustLyricsOffset(deltaMs)) },
                                    onDefault = { viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.ResetLyricsOffset) },
                                    scopeHint = offsetState?.let {
                                        stringResource(if (it.first == null) Res.string.lyrics_offset_using_global else Res.string.lyrics_offset_song_scope)
                                    },
                                )
                            }
                        }
                    }
                    Crossfade(targetState = setSleepTimerEnable) {
                        if (it) {
                            ActionButton(
                                icon = WaveIcons.Speed,
                                text =
                                    if (crossfadeEnabled != DataStoreManager.TRUE) {
                                        Res.string.playback_speed_pitch
                                    } else {
                                        Res.string.playback_speed_pitch_disabled
                                    },
                                enable = crossfadeEnabled != DataStoreManager.TRUE,
                            ) {
                                changePlaybackSpeedPitch = true
                            }
                        }
                    }
                    EndOfModalBottomSheet()
                }
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: StringResource?,
    textString: String? = null,
    textColor: Color? = null,
    iconColor: Color = Color.Unspecified,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    val c = rememberSurfaceDarkColors()
    val resolvedIconColor = if (iconColor == Color.Unspecified) c.content else iconColor
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.CenterVertically)
                .then(
                    if (enable) Modifier.clickable { onClick.invoke() } else Modifier.greyScale(),
                ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
                Image(
                    imageVector = icon,
                    contentDescription = if (text != null) stringResource(text) else textString ?: "",
                    modifier = Modifier.wrapContentSize(Alignment.Center).padding(12.dp),
                colorFilter =
                    if (enable) {
                        ColorFilter.tint(resolvedIconColor)
                    } else {
                        ColorFilter.tint(c.disabled)
                    },
            )
            Text(
                text = if (text != null) stringResource(text) else textString ?: "",
                style = typo().labelSmall,
                color = if (enable) textColor ?: c.content else c.disabled,
                modifier =
                    Modifier
                        .padding(start = 10.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
fun CheckBoxActionButton(
    defaultChecked: Boolean,
    stateKey: String,
    isHeartIcon: Boolean,
    onChangeListener: (checked: Boolean) -> Unit,
) {
    var stateChecked by remember(stateKey, defaultChecked) { mutableStateOf(defaultChecked) }
    Box(
        modifier =
            Modifier
                .wrapContentSize(align = Alignment.Center)
                .clickable {
                    stateChecked = !stateChecked
                    onChangeListener(stateChecked)
                },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
        ) {
            Box(Modifier.padding(10.dp)) {
                if (isHeartIcon) {
                    HeartCheckBox(
                        checked = stateChecked,
                        size = 30,
                        onStateChange = {
                            stateChecked = !stateChecked
                            onChangeListener(stateChecked)
                        },
                    )
                } else {
                    Crossfade(stateChecked) {
                        if (it) {
                            Icon(WaveIcons.CheckCircle, "")
                        } else {
                            Icon(WaveIcons.AddCircleOutline, "")
                        }
                    }
                }
            }
            Text(
                text =
                    if (stateChecked) {
                        stringResource(Res.string.liked)
                    } else {
                        stringResource(Res.string.like)
                    },
                style = typo().labelSmall,
                // Matches [ActionButton], which this sits directly above in every sheet that uses
                // both — without it the label alone falls back to the colour typo() carries.
                color = rememberSurfaceDarkColors().content,
                modifier =
                    Modifier
                        .padding(start = 10.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
fun HeartCheckBox(
    size: Int = 24,
    checked: Boolean,
    tint: Color = rememberSurfaceDarkColors().content,
    onStateChange: (() -> Unit)? = null,
) {
    val burstState = rememberHeartBurstState()
    Box(
        modifier =
            Modifier
                .size(size.dp)
                // Before .clip: the burst draws outside the button bounds and the circle clip
                // would trim it to the heart's own circle.
                .heartBurst(burstState)
                .clip(CircleShape)
                .clickable {
                    // Judged at TAP time: tapping an unchecked heart is a like. Firing from the
                    // tap — not from watching `checked` — is what keeps a track change onto an
                    // already-liked song from celebrating a like nobody gave.
                    if (!checked) burstState.fire()
                    onStateChange?.invoke()
                },
    ) {
        Crossfade(targetState = checked, modifier = Modifier.fillMaxSize()) {
            if (it) {
                Image(
                    painter = painterResource(Res.drawable.baseline_favorite_24),
                    contentDescription = stringResource(Res.string.favorite_checked),
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
            } else {
                Image(
                    imageVector = WaveIcons.FavoriteBorder,
                    contentDescription = stringResource(Res.string.favorite_unchecked),
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    colorFilter = ColorFilter.tint(tint),
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun PlaybackSpeedPitchBottomSheet(
    onDismiss: () -> Unit,
    playbackSpeed: Float,
    pitch: Int,
    onSet: (playbackSpeed: Float, pitch: Int) -> Unit,
) {
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Card(
                    modifier = Modifier.width(40.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(modifier = Modifier.height(16.dp))
                // Playback Speed row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        imageVector = WaveIcons.Speed,
                        contentDescription = stringResource(Res.string.playback_speed),
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().subtitle),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val newSpeed = (kotlin.math.floor((playbackSpeed - 0.1f) * 10f) / 10f).coerceIn(0.2f, 2f)
                            onSet(
                                newSpeed,
                                pitch,
                            )
                        },
                    ) {
                        Icon(
                            WaveIcons.Remove,
                            contentDescription = stringResource(Res.string.decrease_speed),
                            tint = rememberSurfaceDarkColors().subtitle,
                        )
                    }
                    Text(
                        text = "x${String.format("%.1f", playbackSpeed)}",
                        style = typo().titleMedium,
                        color = rememberSurfaceDarkColors().subtitle,
                        modifier = Modifier.widthIn(min = 60.dp),
                        textAlign = TextAlign.Center,
                    )
                    IconButton(
                        onClick = {
                            val newSpeed = (kotlin.math.floor((playbackSpeed + 0.1f) * 10f) / 10f).coerceIn(0.2f, 2f)
                            onSet(
                                newSpeed,
                                pitch,
                            )
                        },
                    ) {
                        Icon(
                            WaveIcons.Add,
                            contentDescription = stringResource(Res.string.increase_speed),
                            tint = rememberSurfaceDarkColors().subtitle,
                        )
                    }
                }
                // Shown on every platform. It used to be hidden on Desktop because LibVLC had no
                // independent pitch control, but that backend is long gone — mpv shifts pitch with
                // its rubberband filter. The control is still locked out while crossfade is on,
                // handled by the caller: crossfade owns mpv's filter chain and the two would fight
                // over it.
                run {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            WaveIcons.Tune,
                            contentDescription = stringResource(Res.string.pitch),
                            modifier = Modifier.size(24.dp),
                            tint = rememberSurfaceDarkColors().subtitle,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                val newPitch = (pitch - 1).coerceIn(-12, 12)
                                onSet(playbackSpeed, newPitch)
                            },
                        ) {
                            Icon(
                                WaveIcons.Remove,
                                contentDescription = stringResource(Res.string.decrease_pitch),
                                tint = rememberSurfaceDarkColors().subtitle,
                            )
                        }
                        Text(
                            text = "$pitch",
                            style = typo().titleMedium,
                            color = rememberSurfaceDarkColors().subtitle,
                            modifier = Modifier.widthIn(min = 60.dp),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = {
                                val newPitch = (pitch + 1).coerceIn(-12, 12)
                                onSet(playbackSpeed, newPitch)
                            },
                        ) {
                            Icon(
                                WaveIcons.Add,
                                contentDescription = stringResource(Res.string.increase_pitch),
                                tint = rememberSurfaceDarkColors().subtitle,
                            )
                        }
                    }
                }
                EndOfModalBottomSheet()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REDESIGNED SleepTimerBottomSheet
// Quick presets (5, 10, 15, 30, 45 min, 1 hour) + End of Song + Custom input
// Passes END_OF_SONG_SENTINEL (Int.MAX_VALUE) for "End of Song" option.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    onDismiss: () -> Unit,
    onSetTimer: (minutes: Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // -1 = nothing selected, Int.MAX_VALUE = End of Song, else = minutes
    var selectedPreset by rememberSaveable { mutableIntStateOf(-1) }
    var showCustomInput by rememberSaveable { mutableStateOf(false) }
    var customMinutes by rememberSaveable { mutableStateOf("") }

    data class Preset(
        val label: String,
        val minutes: Int,
    )

    val presets =
        listOf(
            Preset("5 min", 5),
            Preset("10 min", 10),
            Preset("15 min", 15),
            Preset("30 min", 30),
            Preset("45 min", 45),
            Preset("1 hour", 60),
        )

    // Whether the Set button should be enabled
    val isSetEnabled =
        when {
            selectedPreset == END_OF_SONG_SENTINEL -> true
            selectedPreset > 0 && !showCustomInput -> true
            showCustomInput && (customMinutes.toIntOrNull() ?: 0) > 0 -> true
            else -> false
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Drag handle
                Card(
                    modifier = Modifier.width(40.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().disabled),
                    shape = RoundedCornerShape(50),
                ) {}

                Spacer(modifier = Modifier.height(16.dp))

                // Title row with alarm icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        imageVector = WaveIcons.AccessAlarm,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(seed),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.sleep_timer_off),
                        style = typo().titleMedium,
                        color = rememberSurfaceDarkColors().content,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Preset grid: 3 columns × 2 rows ──────────────────────
                val presetRows = presets.chunked(3)
                presetRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { preset ->
                            val isSelected = selectedPreset == preset.minutes && !showCustomInput
                            OutlinedButton(
                                onClick = {
                                    selectedPreset = preset.minutes
                                    showCustomInput = false
                                    customMinutes = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) seed.copy(alpha = 0.15f) else Color.Transparent,
                                    ),
                                border =
                                    BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) seed else rememberSurfaceDarkColors().disabled,
                                    ),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                            ) {
                                Text(
                                    text = preset.label,
                                    style = typo().bodySmall,
                                    color = if (isSelected) seed else rememberSurfaceDarkColors().subtitle,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── End of Song + Custom row ─────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // End of Song
                    val isEndSelected = selectedPreset == END_OF_SONG_SENTINEL && !showCustomInput
                    OutlinedButton(
                        onClick = {
                            selectedPreset = END_OF_SONG_SENTINEL
                            showCustomInput = false
                            customMinutes = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isEndSelected) seed.copy(alpha = 0.15f) else Color.Transparent,
                            ),
                        border =
                            BorderStroke(
                                width = if (isEndSelected) 1.5.dp else 1.dp,
                                color = if (isEndSelected) seed else rememberSurfaceDarkColors().disabled,
                            ),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.sleep_timer_end_label),
                            style = typo().bodySmall,
                            color = if (isEndSelected) seed else rememberSurfaceDarkColors().subtitle,
                            fontWeight = if (isEndSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }

                    // Custom
                    val isCustomSelected = showCustomInput
                    OutlinedButton(
                        onClick = {
                            showCustomInput = true
                            selectedPreset = -1
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isCustomSelected) seed.copy(alpha = 0.15f) else Color.Transparent,
                            ),
                        border =
                            BorderStroke(
                                width = if (isCustomSelected) 1.5.dp else 1.dp,
                                color = if (isCustomSelected) seed else rememberSurfaceDarkColors().disabled,
                            ),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.theme_color_custom),
                            style = typo().bodySmall,
                            color = if (isCustomSelected) seed else rememberSurfaceDarkColors().subtitle,
                            fontWeight = if (isCustomSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }

                // ── Custom input (animated expand) ───────────────────────
                AnimatedVisibility(visible = showCustomInput) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customMinutes,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 4) {
                                    customMinutes = value
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(Res.string.sleep_minutes),
                                    style = typo().bodySmall,
                                )
                            },
                            suffix = {
                                Text(
                                    text = stringResource(Res.string.minutes_short),
                                    style = typo().bodySmall,
                                    color = rememberSurfaceDarkColors().disabled,
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Set button ───────────────────────────────────────────
                Button(
                    onClick = {
                        when {
                            selectedPreset == END_OF_SONG_SENTINEL -> {
                                onSetTimer(END_OF_SONG_SENTINEL)
                                coroutineScope.launch {
                                    modelBottomSheetState.hide()
                                    onDismiss()
                                }
                            }
                            showCustomInput -> {
                                val mins = customMinutes.toIntOrNull() ?: 0
                                if (mins > 0) {
                                    onSetTimer(mins)
                                    coroutineScope.launch {
                                        modelBottomSheetState.hide()
                                        onDismiss()
                                    }
                                } else {
                                    showToast(
                                        runBlocking { getString(Res.string.sleep_timer_set_error) },
                                        ToastGravity.Bottom,
                                    )
                                }
                            }
                            selectedPreset > 0 -> {
                                onSetTimer(selectedPreset)
                                coroutineScope.launch {
                                    modelBottomSheetState.hide()
                                    onDismiss()
                                }
                            }
                            else -> {
                                showToast(
                                    runBlocking { getString(Res.string.sleep_timer_set_error) },
                                    ToastGravity.Bottom,
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = seed,
                            disabledContainerColor = seed.copy(alpha = 0.3f),
                        ),
                    enabled = isSetEnabled,
                ) {
                    Text(
                        text = stringResource(Res.string.set),
                        style = typo().labelSmall,
                        color = rememberSurfaceDarkColors().content,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                EndOfModalBottomSheet()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistModalBottomSheet(
    isBottomSheetVisible: Boolean,
    listLocalPlaylist: List<LocalPlaylistEntity>,
    listYouTubePlaylist: List<PlaylistsResult>,
    videoId: String? = null,
    onClick: (LocalPlaylistEntity) -> Unit,
    onYTPlaylistClick: (PlaylistsResult) -> Unit,
    onDismiss: () -> Unit,
    onCreatePlaylist: ((String) -> Unit)? = null,
    isPlaylistListLoading: Boolean = false,
) {
    val hiddenPlaylistNames = setOf("new episodes", "episodes for later")
    val alreadyPresentMessage = stringResource(Res.string.already_in_playlist)
    val visibleLocalPlaylists = listLocalPlaylist.filterNot {
        it.title.trim().lowercase() in hiddenPlaylistNames
    }
    val visibleYouTubePlaylists = listYouTubePlaylist.filterNot {
        it.title.trim().lowercase() in hiddenPlaylistNames
    }
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val hideModalBottomSheet: () -> Unit =
        {
            coroutineScope.launch {
                modelBottomSheetState.hide()
                onDismiss()
            }
        }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = modelBottomSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            Card(
                modifier =
                    Modifier.dialogMotionSource(clearPreviousOrigin = true)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                        .fillMaxWidth()
                        .wrapContentHeight(),
                shape = BottomSheetDefaults.ExpandedShape,
                colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp),
                ) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier = Modifier.width(60.dp).height(4.dp),
                        colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))

                    val linkedYouTubePlaylistIds = visibleLocalPlaylists.mapNotNull {
                        it.youtubePlaylistId?.removePrefix("VL")
                    }.toSet()
                    val unrepresentedYouTubePlaylists =
                        visibleYouTubePlaylists.filterNot { it.browseId.removePrefix("VL") in linkedYouTubePlaylistIds }
                    LazyColumn {
                        if (onCreatePlaylist != null) {
                            item {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .dialogMotionClickable {
                                                showCreatePlaylistDialog = true
                                            }
                                            .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        imageVector = WaveIcons.PlaylistAdd,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(Res.string.create_playlist),
                                        style = typo().labelSmall,
                                        color = rememberSurfaceDarkColors().content,
                                    )
                                }
                            }
                        }
                        if (isPlaylistListLoading) {
                            item {
                                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = rememberSurfaceDarkColors().content)
                                    Spacer(Modifier.width(12.dp))
                                    Text(stringResource(Res.string.loading), color = rememberSurfaceDarkColors().content)
                                }
                            }
                        }
                        if (!isPlaylistListLoading && visibleLocalPlaylists.isEmpty() && unrepresentedYouTubePlaylists.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(Res.string.no_playlist_found),
                                    style = typo().labelSmall,
                                    modifier = Modifier.padding(20.dp),
                                    color = rememberSurfaceDarkColors().disabled,
                                )
                            }
                        }
                        items(if (isPlaylistListLoading) emptyList() else visibleLocalPlaylists) { playlist ->
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable(
                                            onClick = {
                                                if (playlist.tracks?.contains(videoId) == true) {
                                                    showToast(alreadyPresentMessage, ToastGravity.Bottom)
                                                } else {
                                                    onClick(playlist)
                                                    hideModalBottomSheet()
                                                }
                                            },
                                        ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp).align(Alignment.CenterStart),
                                ) {
                                    Crossfade(targetState = playlist.tracks?.contains(videoId) == true) {
                                        if (it) {
                                            Image(
                                                imageVector = WaveIcons.Done,
                                                contentDescription = "",
                                                colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                            )
                                        } else {
                                            Image(
                                                imageVector = WaveIcons.PlaylistAdd,
                                                contentDescription = "",
                                                colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = playlist.title,
                                        style = typo().labelSmall,
                                        color =
                                            if (playlist.tracks?.contains(videoId) == true) {
                                                rememberSurfaceDarkColors().disabled
                                            } else {
                                                rememberSurfaceDarkColors().content
                                            },
                                    )
                                }
                            }
                        }
                        items(if (isPlaylistListLoading) emptyList() else unrepresentedYouTubePlaylists) { playlist ->
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable(onClick = {
                                            onYTPlaylistClick(playlist)
                                            hideModalBottomSheet()
                                        }),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp).align(Alignment.CenterStart),
                                ) {
                                    Image(
                                        imageVector = WaveIcons.PlaylistAdd,
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = playlist.title,
                                        style = typo().labelSmall,
                                        color = rememberSurfaceDarkColors().content,
                                    )
                                }
                            }
                        }
                    }
                    EndOfModalBottomSheet()
                }
            }
        }
    }
    if (isBottomSheetVisible && showCreatePlaylistDialog && onCreatePlaylist != null) {
        CreatePlaylistDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                onCreatePlaylist(name)
                hideModalBottomSheet()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistModalBottomSheet(
    isBottomSheetVisible: Boolean,
    artists: List<Artist>,
    navController: NavController,
    onBeforeNavigate: suspend () -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideModalBottomSheet: () -> Unit =
        {
            coroutineScope.launch {
                modelBottomSheetState.hide()
                onDismiss()
            }
        }
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = modelBottomSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            Card(
                modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier = Modifier.width(60.dp).height(4.dp),
                        colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    LazyColumn {
                        items(artists) { artist ->
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val id = artist.id
                                            if (!id.isNullOrBlank()) {
                                                coroutineScope.launch {
                                                    // Let the nested artist picker and its parent player
                                                    // sheet finish dismissing before navigating. This
                                                    // keeps the destination animation visible instead of
                                                    // letting the sheets cover most of its fade/slide.
                                                    // NonCancellable keeps the final navigation alive
                                                    // when dismissing the sheets removes this composable.
                                                    withContext(NonCancellable) {
                                                        modelBottomSheetState.hide()
                                                        onDismiss()
                                                        onBeforeNavigate()
                                                        navController.navigate(ArtistDestination(id))
                                                    }
                                                }
                                            }
                                        },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(20.dp).align(Alignment.CenterStart),
                                ) {
                                    Image(
                                        imageVector = WaveIcons.PeopleAlt,
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(rememberSurfaceDarkColors().content),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = artist.name, style = typo().labelSmall)
                                }
                            }
                        }
                        item { EndOfModalBottomSheet() }
                    }
                }
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun PlaylistBottomSheet(
    onDismiss: () -> Unit,
    playlistId: String,
    playlistName: String,
    isYourYouTubePlaylist: Boolean,
    onEditTitle: (newTitle: String) -> Unit = {},
    onDelete: () -> Unit = {},
    onAddToQueue: (() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideModalBottomSheet: () -> Unit =
        {
            coroutineScope.launch {
                modelBottomSheetState.hide()
                onDismiss()
            }
        }
    var showEditTitle by remember { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    if (showEditTitle) {
        var newTitle by remember { mutableStateOf(playlistName) }
        val showEditTitleSheetState =
            rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val hideEditTitleBottomSheet: () -> Unit =
            {
                coroutineScope.launch {
                    showEditTitleSheetState.hide()
                    onDismiss()
                }
            }
        ModalBottomSheet(
            onDismissRequest = { showEditTitle = false },
            sheetState = showEditTitleSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            contentWindowInsets = { WindowInsets.ime },
        ) {
            Card(
                modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier = Modifier.width(60.dp).height(4.dp),
                        colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { s -> newTitle = s },
                        label = { Text(text = stringResource(Res.string.title)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    val playlistNameError = stringResource(Res.string.playlist_name_cannot_be_empty)
                    TextButton(
                        onClick = {
                            if (newTitle.isBlank()) {
                                showToast(playlistNameError, ToastGravity.Bottom)
                            } else {
                                onEditTitle(newTitle)
                                hideEditTitleBottomSheet()
                                hideModalBottomSheet()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(Res.string.save))
                    }
                    EndOfModalBottomSheet()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(5.dp))
                Card(
                    modifier = Modifier.width(60.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(modifier = Modifier.height(5.dp))
                if (onAddToQueue != null) {
                    ActionButton(
                        icon = WaveIcons.QueueMusic,
                        text = Res.string.add_to_queue,
                    ) {
                        onAddToQueue()
                        hideModalBottomSheet()
                    }
                }
                if (isYourYouTubePlaylist) {
                    ActionButton(icon = WaveIcons.Edit, text = Res.string.edit_title) {
                        showEditTitle = true
                    }
                    if (playlistId.removePrefix("VL") != "LM") {
                        ActionButton(icon = WaveIcons.Delete, text = Res.string.delete_playlist) {
                            showDeleteConfirmation = true
                        }
                    }
                }
                val shareTitle = stringResource(Res.string.share)
                ActionButton(icon = WaveIcons.Share, text = Res.string.share) {
                    val url = "https://music.youtube.com/playlist?list=${playlistId.replaceFirst("VL", "")}"
                    shareUrl(shareTitle, url)
                }
                EndOfModalBottomSheet()
            }
        }
    }

    if (showDeleteConfirmation) {
        AmoledAlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            raisedDarkContainer = true,
            title = {
                Text(
                    text = stringResource(Res.string.delete_playlist),
                    style = typo().titleMedium,
                )
            },
            text = { Text(stringResource(Res.string.delete_playlist_confirmation)) },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                        hideModalBottomSheet()
                    },
                ) {
                    Text(stringResource(Res.string.delete_playlist))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistBottomSheet(
    isBottomSheetVisible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    ytPlaylistId: String? = null,
    onEditTitle: (newTitle: String) -> Unit,
    onEditThumbnail: (newThumbnailUri: String) -> Unit,
    onAddToQueue: () -> Unit,
    onSync: () -> Unit,
    onUpdatePlaylist: () -> Unit,
    onDelete: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var showEditTitle by remember { mutableStateOf(false) }
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideModalBottomSheet: () -> Unit =
        {
            coroutineScope.launch {
                modelBottomSheetState.hide()
                onDismiss()
            }
        }
    // The picked file is cropped before it is used. A cover slot is square, so an uncropped 16:9
    // photo would be squashed to fit — which is what it used to do.
    var imageAwaitingCrop by remember { mutableStateOf<ByteArray?>(null) }
    val resultLauncher =
        photoPickerResult { pickedUri ->
            pickedUri?.let { uri ->
                coroutineScope.launch { imageAwaitingCrop = readLocalImageBytes(uri) }
            }
        }
    imageAwaitingCrop?.let { bytes ->
        ImageCropperDialog(
            imageBytes = bytes,
            titleText = stringResource(Res.string.crop_cover),
            confirmText = stringResource(Res.string.save),
            cancelText = stringResource(Res.string.cancel),
            onDismiss = { imageAwaitingCrop = null },
            onCropped = { cropped ->
                imageAwaitingCrop = null
                coroutineScope.launch {
                    // Written into the app's own storage, NOT reused from the picker's uri: that
                    // one still points at the original uncropped file, and on Android the read
                    // permission granted for it does not outlive the process.
                    // Named after the CONTENT, not the clock. A fixed name would be overwritten
                    // in place and Coil, which caches by url, would keep showing the previous
                    // cover; a timestamp would leave a new file behind every time the user
                    // re-picked the same picture. Hashing gives a fresh name for a new image and
                    // the same name for the same one.
                    persistPickedImage(cropped, "cover_${cropped.contentHashCode().toUInt()}.jpg")
                        ?.let(onEditThumbnail)
                }
            },
        )
    }
    if (showEditTitle) {
        var newTitle by remember { mutableStateOf(title) }
        val showEditTitleSheetState =
            rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val hideEditTitleBottomSheet: () -> Unit =
            {
                coroutineScope.launch {
                    showEditTitleSheetState.hide()
                    onDismiss()
                }
            }
        ModalBottomSheet(
            onDismissRequest = { showEditTitle = false },
            sheetState = showEditTitleSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            contentWindowInsets = { WindowInsets.ime },
        ) {
            Card(
                modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier = Modifier.width(60.dp).height(4.dp),
                        colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { s -> newTitle = s },
                        label = { Text(text = stringResource(Res.string.title)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    val playlistNameError = stringResource(Res.string.playlist_name_cannot_be_empty)
                    TextButton(
                        onClick = {
                            if (newTitle.isBlank()) {
                                showToast(playlistNameError, ToastGravity.Bottom)
                            } else {
                                onEditTitle(newTitle)
                                hideEditTitleBottomSheet()
                                hideModalBottomSheet()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(Res.string.save))
                    }
                    EndOfModalBottomSheet()
                }
            }
        }
    }
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = modelBottomSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            Card(
                modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier = Modifier.width(60.dp).height(4.dp),
                        colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    ActionButton(icon = WaveIcons.Edit, text = Res.string.edit_title) {
                        showEditTitle = true
                    }
                    ActionButton(icon = WaveIcons.AddPhotoAlternate, text = Res.string.edit_thumbnail) {
                        resultLauncher.launch()
                    }
                    ActionButton(icon = WaveIcons.QueueMusic, text = Res.string.add_to_queue) {
                        onAddToQueue()
                    }
                    ActionButton(
                        icon =
                            if (ytPlaylistId != null) {
                                WaveIcons.SyncDisabled
                            } else {
                                WaveIcons.Sync
                            },
                        text =
                            if (ytPlaylistId != null) {
                                Res.string.synced
                            } else {
                                Res.string.sync
                            },
                    ) {
                        onSync()
                    }
                    ActionButton(
                        icon = WaveIcons.Update,
                        text = Res.string.update_playlist,
                        enable = (ytPlaylistId != null),
                    ) {
                        onUpdatePlaylist()
                    }
                    ActionButton(icon = WaveIcons.Delete, text = Res.string.delete_playlist) {
                        onDelete()
                        hideModalBottomSheet()
                    }
                    val shareTitle = stringResource(Res.string.share_url)
                    ActionButton(
                        icon = WaveIcons.Share,
                        text = if (ytPlaylistId != null) Res.string.share else Res.string.sync_first,
                        enable = (ytPlaylistId != null),
                    ) {
                        val url = "https://music.youtube.com/playlist?list=${ytPlaylistId?.replaceFirst("VL", "")}"
                        shareUrl(shareTitle, url)
                    }
                    EndOfModalBottomSheet()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortPlaylistBottomSheet(
    selectedState: FilterState,
    onDismiss: () -> Unit,
    onSortChanged: (FilterState) -> Unit,
) {
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filterOptions =
        remember {
            listOf(
                FilterState.CustomOrder,
                FilterState.NewerFirst,
                FilterState.OlderFirst,
                FilterState.Title,
            )
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(5.dp))
                Card(
                    modifier = Modifier.width(60.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Text(
                    stringResource(Res.string.sort_by),
                    style = typo().labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .padding(start = 16.dp, top = 16.dp, bottom = 24.dp)
                            .align(Alignment.Start),
                )
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(filterOptions, key = { it.hashCode() }) { filterOption ->
                        val isSelected = filterOption == selectedState
                        Row(
                            Modifier
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSortChanged(filterOption)
                                    onDismiss()
                                }.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(filterOption.displayNameRes()),
                                style = typo().labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) seed else rememberSurfaceDarkColors().content,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Image(
                                    imageVector = WaveIcons.Done,
                                    contentDescription = stringResource(Res.string.selected),
                                    colorFilter = ColorFilter.tint(seed),
                                    modifier = Modifier.size(32.dp),
                                )
                            } else {
                                Spacer(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
                EndOfModalBottomSheet()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevLogInBottomSheet(
    onDismiss: () -> Unit,
    type: DevLogInType,
    onDone: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var value by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(5.dp))
                Card(
                    modifier = Modifier.width(60.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = runBlocking { type.getTitle() }, style = typo().labelSmall)
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = value,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    onValueChange = { value = it },
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(5.dp))
                TextButton(
                    onClick = {
                        if (value.isNotEmpty() && value.isNotBlank()) {
                            showToast(runBlocking { getString(Res.string.processing) }, ToastGravity.Bottom)
                            onDismiss()
                            onDone(value)
                        } else {
                            showToast(runBlocking { getString(Res.string.can_not_be_empty) }, ToastGravity.Bottom)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                ) {
                    Text(text = stringResource(Res.string.set), style = typo().labelSmall)
                }
                Spacer(modifier = Modifier.height(5.dp))
                EndOfModalBottomSheet()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevCookieLogInBottomSheet(
    onDismiss: () -> Unit,
    type: DevLogInType,
    cookies: List<Pair<String, String?>>,
) {
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val modelBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modelBottomSheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().container),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(5.dp))
                Card(
                    modifier = Modifier.width(60.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = rememberSurfaceDarkColors().handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.list_all_cookies_of_this_page),
                    style = typo().labelSmall,
                )
                cookies.forEach { cookie ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(12.dp),
                    ) {
                        Text(text = cookie.first, style = typo().bodyMedium, modifier = Modifier.weight(1f))
                        SelectionContainer(modifier = Modifier.weight(2f)) {
                            Text(text = cookie.second ?: "", style = typo().bodyMedium)
                        }
                        val copied = stringResource(Res.string.copied_to_clipboard)
                        IconButton(onClick = {
                            copyToClipboard(cookie.first, cookie.second ?: "")
                            showToast(copied, ToastGravity.Bottom)
                        }) {
                            Icon(imageVector = WaveIcons.ContentCopy, contentDescription = stringResource(Res.string.copy_action))
                        }
                    }
                }
                EndOfModalBottomSheet()
            }
        }
    }
}

@Composable
fun EndOfModalBottomSheet() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                        .value
                        .toInt()
                        .dp + 8.dp,
                ),
    ) {}
}

sealed class DevLogInType {
    data object Spotify : DevLogInType()

    data object YouTube : DevLogInType()

    data object Discord : DevLogInType()

    suspend fun getTitle(): String =
        when (this) {
            is Spotify -> getString(Res.string.your_sp_dc_param_of_spotify_cookie)
            is YouTube -> getString(Res.string.your_youtube_cookie)
            is Discord -> getString(Res.string.your_discord_token)
        }
}
