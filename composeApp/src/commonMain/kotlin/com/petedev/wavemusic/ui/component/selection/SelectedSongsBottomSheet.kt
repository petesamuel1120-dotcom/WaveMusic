package com.petedev.wavemusic.ui.component.selection

import com.petedev.wavemusic.ui.component.dialogMotionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.ui.component.ActionButton
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.ui.component.EndOfModalBottomSheet
import com.petedev.wavemusic.ui.component.rememberSurfaceDarkColors
import com.petedev.wavemusic.ui.icon.Download
import com.petedev.wavemusic.ui.icon.Favorite
import com.petedev.wavemusic.ui.icon.FavoriteBorder
import com.petedev.wavemusic.ui.icon.PlaylistAdd
import com.petedev.wavemusic.ui.icon.QueueMusic
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.SkipNext
import com.petedev.wavemusic.ui.theme.typo
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.add_to_a_playlist
import com.petedev.wavemusic.resources.add_to_queue
import com.petedev.wavemusic.resources.download
import com.petedev.wavemusic.resources.favorite
import com.petedev.wavemusic.resources.n_songs_selected
import com.petedev.wavemusic.resources.play_next
import com.petedev.wavemusic.resources.remove_from_favorites

/**
 * One row in [SelectedSongsBottomSheet] that only some screens have — "remove from playlist"
 * means nothing on an album or in search results, so it is passed in rather than hardcoded here.
 */
data class SongSelectionAction(
    val icon: ImageVector,
    val label: String,
    val tint: Color? = null,
    val onClick: () -> Unit,
)

/**
 * Actions for the current selection, opened from the overflow button in [SongSelectionTopAppBar].
 * A null callback hides its row, so a screen only offers what it can actually do.
 * Favorite actions also depend on the live favorite status of [selectedVideoIds].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedSongsBottomSheet(
    count: Int,
    selectedVideoIds: List<String>,
    onDismiss: () -> Unit,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    onAddToFavorite: (() -> Unit)? = null,
    onRemoveFromFavorite: (() -> Unit)? = null,
    extraActions: List<SongSelectionAction> = emptyList(),
) {
    val songRepository: SongRepository = koinInject()
    val favoriteVideoIds by remember(songRepository) {
        songRepository.observeLikedSongs().map { songs ->
            songs.mapTo(mutableSetOf()) { it.videoId } as Set<String>?
        }
    }.collectAsStateWithLifecycle(initialValue = null)
    // Wait for the actual favorite state instead of temporarily treating a loading list as
    // empty. Each action is offered only if it can change at least one selected song.
    val hasFavorites = favoriteVideoIds?.let { ids -> selectedVideoIds.any { it in ids } } == true
    val hasNonFavorites = favoriteVideoIds?.let { ids -> selectedVideoIds.any { it !in ids } } == true
    val coroutineScope = rememberCoroutineScope()
    val colors = rememberSurfaceDarkColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideThen: (() -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
            action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
            colors = CardDefaults.cardColors().copy(containerColor = colors.container),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(5.dp))
                Card(
                    modifier =
                        Modifier
                            .width(60.dp)
                            .height(4.dp),
                    colors = CardDefaults.cardColors().copy(containerColor = colors.handle),
                    shape = RoundedCornerShape(50),
                ) {}
                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = stringResource(Res.string.n_songs_selected, count),
                        style = typo().bodySmall,
                        color = colors.subtitle,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                    if (onPlayNext != null) {
                        ActionButton(
                            icon = WaveIcons.SkipNext,
                            text = Res.string.play_next,
                        ) { hideThen(onPlayNext) }
                    }
                    if (onAddToQueue != null) {
                        ActionButton(
                            icon = WaveIcons.QueueMusic,
                            text = Res.string.add_to_queue,
                        ) { hideThen(onAddToQueue) }
                    }
                    if (onAddToPlaylist != null) {
                        ActionButton(
                            icon = WaveIcons.PlaylistAdd,
                            text = Res.string.add_to_a_playlist,
                        ) { hideThen(onAddToPlaylist) }
                    }
                    if (onDownload != null) {
                        ActionButton(
                            icon = WaveIcons.Download,
                            text = Res.string.download,
                        ) { hideThen(onDownload) }
                    }
                    if (onAddToFavorite != null && hasNonFavorites) {
                        ActionButton(
                            icon = WaveIcons.Favorite,
                            text = Res.string.favorite,
                        ) { hideThen(onAddToFavorite) }
                    }
                    if (onRemoveFromFavorite != null && hasFavorites) {
                        ActionButton(
                            icon = WaveIcons.FavoriteBorder,
                            text = Res.string.remove_from_favorites,
                        ) { hideThen(onRemoveFromFavorite) }
                    }
                    extraActions.forEach { action ->
                        ActionButton(
                            icon = action.icon,
                            text = null,
                            textString = action.label,
                            textColor = action.tint,
                            iconColor = action.tint ?: Color.Unspecified,
                        ) { hideThen(action.onClick) }
                    }
                    EndOfModalBottomSheet()
                }
            }
        }
    }
}
