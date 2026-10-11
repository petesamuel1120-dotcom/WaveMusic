package com.maxrave.simpmusic.ui.component

import simpmusic.composeapp.generated.resources.pause_action
import simpmusic.composeapp.generated.resources.play_action

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.ui.icon.Delete
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.component.ActionButton
import com.maxrave.simpmusic.ui.component.EndOfModalBottomSheet
import com.maxrave.simpmusic.ui.component.dialogMotionSource
import com.maxrave.simpmusic.ui.component.rememberSurfaceDarkColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.add_to_queue
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.logo_grey
import simpmusic.composeapp.generated.resources.n_track
import simpmusic.composeapp.generated.resources.remove_all_downloads
import simpmusic.composeapp.generated.resources.shuffle

private val DownloadedPlaylistHeroCoverSize = 180.dp
private val DownloadedPlaylistHeroActionSpace = 96.dp
internal val LibraryPlaylistHeroActionSpace = 92.dp

/** Simple artwork-free hero used by the Most Played page. */
@Composable
fun LibraryPlaylistHero(
    title: String,
    trackCount: Int,
    backgroundColor: Color,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    stickyTitle: StickyPlaylistTitleState? = null,
) {
    val backdrop = rememberBackdrop(backgroundColor)
    val contentColor = MaterialTheme.colorScheme.onBackground
    val screen = getScreenSizeInfo()
    val isLandscape = screen.wDP >= screen.hDP
    val textAlign = if (isLandscape) TextAlign.Start else TextAlign.Center
    Box(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .frostedHeaderSource(backdrop, auroraBackground = backgroundColor)
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = LibraryPlaylistHeroActionSpace),
            horizontalAlignment = if (isLandscape) Alignment.Start else Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                modifier = stickyTitle?.sourceModifier(typo().titleLarge, textAlign = textAlign) ?: Modifier,
                style = typo().titleLarge,
                color = contentColor,
                textAlign = textAlign,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                style = typo().bodyMedium,
                color = contentColor.copy(alpha = 0.6f),
                textAlign = textAlign,
            )
        }
        Row(
            modifier = Modifier
                .align(if (isLandscape) Alignment.BottomStart else Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = SimpIcons.PlayArrow,
                text = stringResource(Res.string.play_action),
                contentColor = contentColor,
                onClick = onPlay,
                enabled = trackCount > 0,
                modifier = Modifier.weight(1f),
            )
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = SimpIcons.Shuffle,
                text = stringResource(Res.string.shuffle),
                contentColor = contentColor,
                onClick = onShuffle,
                enabled = trackCount > 0,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** The downloads header follows the Favorites banner layout and keeps the current download cover. */
@Composable
fun DownloadedPlaylistHero(
    thumbnail: String?,
    trackCount: Int,
    isPlaying: Boolean,
    backgroundColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveAllDownloads: () -> Unit,
    stickyTitle: StickyPlaylistTitleState? = null,
) {
    val backdrop = rememberBackdrop(backgroundColor)
    val contentColor = MaterialTheme.colorScheme.onBackground
    val accentColor = MaterialTheme.colorScheme.primary
    val coverRequest = rememberPlaylistCoverRequest(thumbnail)
    val screen = getScreenSizeInfo()
    val isLandscape = screen.wDP >= screen.hDP

    if (isLandscape) {
        val landscapeHero = onlineLandscapeHeroLayout(extraTopPadding = 8.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = landscapeHero.horizontalPadding)
                .padding(top = 4.dp, bottom = 8.dp),
        ) {
            Box(Modifier.matchParentSize().frostedHeaderSource(backdrop, auroraBackground = backgroundColor))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                DownloadedPlaylistCover(coverRequest, trackCount == 0, landscapeHero.coverSize)
                Spacer(Modifier.size(landscapeHero.columnSpacing))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.downloaded),
                        modifier = Modifier.fillMaxWidth().then(
                            stickyTitle?.sourceModifier(typo().titleMedium, textAlign = TextAlign.Start) ?: Modifier,
                        ),
                        style = typo().titleMedium,
                        color = contentColor,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))
                    DownloadedPlaylistActions(
                        backdrop = backdrop,
                        isPlaying = isPlaying,
                        trackCount = trackCount,
                        contentColor = contentColor,
                        accentColor = accentColor,
                        onPlayPause = onPlayPause,
                        onShuffle = onShuffle,
                        onAddToQueue = onAddToQueue,
                        onRemoveAllDownloads = onRemoveAllDownloads,
                        modifier = Modifier.widthIn(max = LandscapeHeroActionMaxWidth),
                    )
                }
            }
        }
        return
    }

    Box(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .frostedHeaderSource(backdrop, auroraBackground = backgroundColor)
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = DownloadedPlaylistHeroActionSpace),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DownloadedPlaylistCover(coverRequest, trackCount == 0)
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.downloaded),
                modifier = stickyTitle?.sourceModifier(typo().titleMedium) ?: Modifier,
                style = typo().titleMedium,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
        DownloadedPlaylistActions(
            backdrop = backdrop,
            isPlaying = isPlaying,
            trackCount = trackCount,
            contentColor = contentColor,
            accentColor = accentColor,
            onPlayPause = onPlayPause,
            onShuffle = onShuffle,
            onAddToQueue = onAddToQueue,
            onRemoveAllDownloads = onRemoveAllDownloads,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun DownloadedPlaylistCover(
    coverRequest: ImageRequest,
    showLogo: Boolean,
    size: Dp = DownloadedPlaylistHeroCoverSize,
) {
    if (showLogo) {
        Image(
            painter = painterResource(Res.drawable.logo_grey),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)),
        )
    } else {
        AsyncImage(
            model = coverRequest,
            placeholder = rememberHolderPainter(),
            error = rememberHolderPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)),
        )
    }
}

@Composable
private fun DownloadedPlaylistActions(
    backdrop: com.maxrave.simpmusic.expect.ui.PlatformBackdrop,
    isPlaying: Boolean,
    trackCount: Int,
    contentColor: Color,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveAllDownloads: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screen = getScreenSizeInfo()
    val maxActionWidth = if (screen.wDP >= screen.hDP) LandscapeHeroActionMaxWidth else 380.dp
    Row(
        modifier = modifier.widthIn(max = maxActionWidth).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DownloadedPlaylistCircleAction(
            backdrop = backdrop,
            icon = SimpIcons.Shuffle,
            description = stringResource(Res.string.shuffle),
            contentColor = contentColor,
            enabled = trackCount > 0,
            onClick = onShuffle,
        )
        PlaylistGlassActionPill(
            backdrop = backdrop,
            icon = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
            text = if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onPlayPause,
            enabled = trackCount > 0,
            modifier = Modifier.weight(1f),
            containerColor = accentColor,
        )
        DownloadedPlaylistMenuAction(
            backdrop = backdrop,
            contentColor = contentColor,
            onAddToQueue = onAddToQueue,
            onRemoveAllDownloads = onRemoveAllDownloads,
        )
    }
}

@Composable
private fun DownloadedPlaylistMenuAction(
    backdrop: com.maxrave.simpmusic.expect.ui.PlatformBackdrop,
    contentColor: Color,
    onAddToQueue: () -> Unit,
    onRemoveAllDownloads: () -> Unit,
) {
    var showActions by remember { mutableStateOf(false) }
    Box {
        DownloadedPlaylistCircleAction(
            backdrop = backdrop,
            icon = SimpIcons.MoreVert,
            description = "More downloads",
            contentColor = contentColor,
            enabled = true,
            onClick = { showActions = true },
        )
    }
    DownloadedPlaylistMenuBottomSheet(
        visible = showActions,
        onDismiss = { showActions = false },
        onAddToQueue = onAddToQueue,
        onRemoveAllDownloads = onRemoveAllDownloads,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadedPlaylistMenuBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveAllDownloads: () -> Unit,
) {
    if (!visible) return
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideThen: (() -> Unit) -> Unit = { action ->
        scope.launch {
            sheetState.hide()
            onDismiss()
            action()
        }
    }
    val colors = rememberSurfaceDarkColors()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .5f),
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) },
    ) {
        Card(
            modifier = Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors().copy(containerColor = colors.container),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(5.dp))
                Card(
                    modifier = Modifier.width(60.dp).height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = colors.handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(Modifier.height(5.dp))
                ActionButton(icon = SimpIcons.QueueMusic, text = Res.string.add_to_queue) {
                    hideThen(onAddToQueue)
                }
                ActionButton(icon = SimpIcons.Delete, text = Res.string.remove_all_downloads) {
                    hideThen(onRemoveAllDownloads)
                }
                EndOfModalBottomSheet()
            }
        }
    }
}

@Composable
private fun DownloadedPlaylistCircleAction(
    backdrop: com.maxrave.simpmusic.expect.ui.PlatformBackdrop,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(glassActionSize()).liquidGlass(backdrop, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        }
    }
}
