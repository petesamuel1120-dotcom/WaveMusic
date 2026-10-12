package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.ui.icon.Add
import com.petedev.wavemusic.ui.icon.ArrowForwardIos
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.library_new_playlist
import com.petedev.wavemusic.resources.library_see_all
import com.petedev.wavemusic.resources.library_your_playlists

/** A compact shelf of the two latest available playlists, plus the playlist creation tile. */
@Composable
internal fun YourPlaylistsShelf(
    playlists: List<PlaylistsResult>,
    localPlaylists: List<LocalPlaylistEntity>,
    showLocalPlaylists: Boolean,
    onCreatePlaylist: () -> Unit,
    onSeeAll: () -> Unit,
    onPlaylistClick: (PlaylistsResult) -> Unit,
    onLocalPlaylistClick: (LocalPlaylistEntity) -> Unit,
    onPlaylistLongClick: ((PlaylistsResult) -> Unit)? = null,
    onLocalPlaylistLongClick: ((LocalPlaylistEntity) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppContentHorizontalInset),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.library_your_playlists),
                style = typo().titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onSeeAll,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.library_see_all),
                    style = typo().labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = WaveIcons.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp).size(13.dp),
                )
            }
        }

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppContentHorizontalInset),
        ) {
            val tileWidth = ((maxWidth - 20.dp) / 3).coerceAtMost(150.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NewPlaylistTile(
                    modifier = Modifier.width(tileWidth),
                    onClick = onCreatePlaylist,
                )
                if (showLocalPlaylists) {
                    localPlaylists.take(2).forEach { playlist ->
                        Box(Modifier.width(tileWidth)) {
                            HomeItemContentPlaylist(
                                onClick = { onLocalPlaylistClick(playlist) },
                                onLongClick = onLocalPlaylistLongClick?.let { callback -> { callback(playlist) } },
                                data = playlist,
                                thumbSize = tileWidth,
                                forceDark = false,
                                horizontalInset = 0.dp,
                                showSubtitle = false,
                            )
                        }
                    }
                } else {
                    playlists.take(2).forEach { playlist ->
                        YouTubePlaylistTile(
                            playlist = playlist,
                            width = tileWidth,
                            onClick = { onPlaylistClick(playlist) },
                            onLongClick = onPlaylistLongClick?.let { callback -> { callback(playlist) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun NewPlaylistTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val createAnchor = rememberDialogMotionAnchor(cornerRadius = 24.dp)
    Column(
        modifier = modifier.clip(shape).clickable {
            createAnchor.record()
            onClick()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape)
                    .background(adaptiveCardContainerColor(MaterialTheme.colorScheme.surfaceContainerLow))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)), shape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(48.dp).then(createAnchor.modifier).background(
                    adaptiveCardContainerColor(MaterialTheme.colorScheme.surfaceContainerHighest, accent = true),
                    CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = WaveIcons.Add,
                    contentDescription = stringResource(Res.string.library_new_playlist),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Text(
            text = stringResource(Res.string.library_new_playlist),
            style = typo().labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun YouTubePlaylistTile(
    playlist: PlaylistsResult,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val artworkSize = width * 0.9f
    val fallback = painterPlaylistThumbnail(
        playlist.title,
        style = typo().bodySmall,
        artworkSize to artworkSize,
    )
    val isEmptyPlaylist = playlist.itemCount.trim().startsWith("0")

    Column(
        modifier =
            Modifier
                .width(width)
                .clip(RoundedCornerShape(18.dp))
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(bottom = 8.dp),
    ) {
        if (isEmptyPlaylist) {
            LocalPlaylistArtworkPlaceholder(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
            )
        } else {
            AsyncImage(
                model = playlist.thumbnails.lastOrNull()?.url,
                placeholder = fallback,
                error = fallback,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
            )
        }
        Text(
            text = playlist.title,
            style = typo().labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 7.dp),
        )
    }
}
