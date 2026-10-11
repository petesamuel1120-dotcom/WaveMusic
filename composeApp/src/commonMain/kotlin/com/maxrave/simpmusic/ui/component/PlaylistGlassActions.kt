package com.maxrave.simpmusic.ui.component

import simpmusic.composeapp.generated.resources.pause_action
import simpmusic.composeapp.generated.resources.play_action

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.baseline_downloaded
import simpmusic.composeapp.generated.resources.download
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloading
import simpmusic.composeapp.generated.resources.shuffle

/** Keep this row outside the layer that captures [backdrop], like the header's glass controls. */
@Composable
fun PlaylistGlassActions(
    backdrop: PlatformBackdrop,
    isPlaying: Boolean,
    downloadState: Int,
    contentColor: Color,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shuffleAsSideButton: Boolean = false,
    playContainerColor: Color? = null,
    playContentColor: Color = contentColor,
) {
    val screen = getScreenSizeInfo()
    val maxActionWidth = if (screen.wDP >= screen.hDP) LandscapeHeroActionMaxWidth else 380.dp
    val shuffleDescription = stringResource(Res.string.shuffle)
    val downloadDescription =
        stringResource(
            when (downloadState) {
                DownloadState.STATE_DOWNLOADED -> Res.string.downloaded
                DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING -> Res.string.downloading
                else -> Res.string.download
            },
        )
    Row(
        modifier = modifier.widthIn(max = maxActionWidth).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (shuffleAsSideButton) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = enabled, role = Role.Button, onClick = onShuffle)
                        .semantics { contentDescription = shuffleDescription },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(glassActionSize()).liquidGlass(backdrop, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SimpIcons.Shuffle,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                text = if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action),
                contentColor = playContentColor,
                onClick = onPlayPause,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                containerColor = playContainerColor,
            )
        } else {
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                text = if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action),
                contentColor = playContentColor,
                onClick = onPlayPause,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                containerColor = playContainerColor,
            )
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = SimpIcons.Shuffle,
                text = stringResource(Res.string.shuffle),
                contentColor = contentColor,
                onClick = onShuffle,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
        // Keep the download on the right with the same 48 dp touch target in both orientations.
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onDownload)
                    .semantics { contentDescription = downloadDescription },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(glassActionSize()).liquidGlass(backdrop, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = downloadState, label = "playlistDownloadAction") { state ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (state) {
                            DownloadState.STATE_DOWNLOADED ->
                                Icon(
                                    painter = painterResource(Res.drawable.baseline_downloaded),
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp),
                                )
                            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING ->
                                DownloadingIndicator(Modifier.size(24.dp), color = contentColor)
                            else ->
                                Icon(
                                    imageVector = SimpIcons.Download,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(22.dp),
                                )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlaylistGlassActionPill(
    backdrop: PlatformBackdrop,
    icon: ImageVector,
    text: String,
    contentColor: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
) {
    Row(
        modifier =
            modifier
                .height(glassActionSize())
                .then(
                    if (containerColor == null) {
                        Modifier.liquidGlass(backdrop, CircleShape)
                    } else {
                        Modifier.background(containerColor, CircleShape)
                    },
                )
                .clip(CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = if (LocalLiquidGlassEnabled.current) 8.dp else 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = contentColor,
            style = typo().labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}
