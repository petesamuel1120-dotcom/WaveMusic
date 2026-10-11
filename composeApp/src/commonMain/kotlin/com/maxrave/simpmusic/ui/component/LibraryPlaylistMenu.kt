package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.type.PlaylistType
import com.maxrave.domain.utils.isRadioPlaylistId
import com.maxrave.simpmusic.viewModel.LibraryViewModel
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.sync_playlist_warning
import simpmusic.composeapp.generated.resources.unsync_playlist_warning
import simpmusic.composeapp.generated.resources.warning
import simpmusic.composeapp.generated.resources.yes

/** Uses the detail screens' menus without navigating away from the Library. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryPlaylistMenu(
    playlist: PlaylistType,
    viewModel: LibraryViewModel,
    onAddToQueue: (List<Track>) -> Unit,
    onDismiss: () -> Unit,
) {
    when (playlist) {
        is LocalPlaylistEntity -> {
            // Null means no confirmation; true syncs and false disconnects the local copy.
            var syncChoice by remember { mutableStateOf<Boolean?>(null) }
            LocalPlaylistBottomSheet(
                isBottomSheetVisible = true,
                onDismiss = onDismiss,
                title = playlist.title,
                ytPlaylistId = playlist.youtubePlaylistId,
                onEditTitle = { viewModel.renameLocalPlaylist(playlist.id, it) },
                onEditThumbnail = { viewModel.updateLocalPlaylistCover(playlist.id, it) },
                onAddToQueue = { viewModel.addLocalPlaylistToQueue(playlist.id) },
                onSync = {
                    syncChoice = playlist.syncState != LocalPlaylistEntity.YouTubeSyncState.Synced
                },
                onUpdatePlaylist = { viewModel.refreshSyncedLocalPlaylist(playlist.id) },
                onDelete = { viewModel.deleteLocalPlaylist(playlist.id) },
            )
            syncChoice?.let { shouldSync ->
                AmoledAlertDialog(
                    title = { Text(stringResource(Res.string.warning)) },
                    text = {
                        Text(
                            stringResource(
                                if (shouldSync) Res.string.sync_playlist_warning else Res.string.unsync_playlist_warning,
                            ),
                        )
                    },
                    onDismissRequest = { syncChoice = null },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.setLocalPlaylistSynced(playlist.id, shouldSync)
                            syncChoice = null
                            onDismiss()
                        }) {
                            Text(stringResource(Res.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { syncChoice = null }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    },
                )
            }
        }

        is PlaylistsResult -> {
            val isRadio = playlist.browseId.isRadioPlaylistId()
            PlaylistBottomSheet(
                onDismiss = onDismiss,
                playlistId = playlist.browseId,
                playlistName = playlist.title,
                isYourYouTubePlaylist = !isRadio,
                onEditTitle = { viewModel.renameYouTubePlaylist(playlist.browseId, it) },
                onDelete = { viewModel.deleteYouTubePlaylist(playlist.browseId) },
                onSaveToLocal = { viewModel.saveYouTubePlaylistToLocal(playlist.browseId) },
                onAddToQueue = if (isRadio) null else {
                    { viewModel.addYouTubePlaylistToQueue(playlist.browseId, onAddToQueue) }
                },
            )
        }

        else -> Unit
    }
}
