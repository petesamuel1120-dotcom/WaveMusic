package com.petedev.wavemusic.ui.component

import com.petedev.wavemusic.resources.pause_action
import com.petedev.wavemusic.resources.play_action

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
import com.petedev.wavemusic.domain.data.entities.DownloadState
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.ui.icon.Download
import com.petedev.wavemusic.ui.icon.Pause
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.Shuffle
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.baseline_downloaded
import com.petedev.wavemusic.resources.download
import com.petedev.wavemusic.resources.downloaded
import com.petedev.wavemusic.resources.downloading
import com.petedev.wavemusic.resources.shuffle

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
    val maxActionWidth = if (screen.wDP >= screen.hDP) landscapeChromeSize(LandscapeHeroActionMaxWidth) else 380.dp
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
        horizontalArrangement = Arrangement.spacedBy(landscapeChromeSize(10.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (shuffleAsSideButton) {
            Box(
                modifier =
                    Modifier
                        .size(landscapeChromeSize(48.dp))
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
                        imageVector = WaveIcons.Shuffle,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(landscapeChromeSize(22.dp)),
                    )
                }
            }
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = if (isPlaying) WaveIcons.Pause else WaveIcons.PlayArrow,
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
                icon = if (isPlaying) WaveIcons.Pause else WaveIcons.PlayArrow,
                text = if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action),
                contentColor = playContentColor,
                onClick = onPlayPause,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                containerColor = playContainerColor,
            )
            PlaylistGlassActionPill(
                backdrop = backdrop,
                icon = WaveIcons.Shuffle,
                text = stringResource(Res.string.shuffle),
                contentColor = contentColor,
                onClick = onShuffle,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
        // Keep the download on the right with a touch target that follows the control scale.
        Box(
            modifier =
                Modifier
                    .size(landscapeChromeSize(48.dp))
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
                                    modifier = Modifier.size(landscapeChromeSize(22.dp)),
                                )
                            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING ->
                                DownloadingIndicator(Modifier.size(landscapeChromeSize(24.dp)), color = contentColor)
                            else ->
                                Icon(
                                    imageVector = WaveIcons.Download,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(landscapeChromeSize(22.dp)),
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
                .padding(horizontal = landscapeChromeSize(if (LocalLiquidGlassEnabled.current) 8.dp else 12.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(landscapeChromeSize(22.dp)))
        Spacer(Modifier.width(landscapeChromeSize(6.dp)))
        Text(
            text = text,
            color = contentColor,
            style = landscapeChromeTextStyle(typo().labelLarge),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}
