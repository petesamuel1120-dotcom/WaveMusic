package com.petedev.wavemusic.ui.component

import com.petedev.wavemusic.resources.pause_action
import com.petedev.wavemusic.resources.play_action

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.domain.data.entities.DownloadState
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.icon.DownloadForOffline
import com.petedev.wavemusic.ui.icon.Pause
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.Shuffle
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.baseline_downloaded
import com.petedev.wavemusic.resources.download
import com.petedev.wavemusic.resources.downloaded
import com.petedev.wavemusic.resources.downloading
import com.petedev.wavemusic.resources.shuffle

/** One action row for portrait and landscape, with a solid white primary action. */
@Composable
fun OnlinePlaylistActions(
    isPlaying: Boolean,
    downloadState: Int,
    backgroundColor: Color,
    contentColor: Color,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
    showSecondaryActions: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    val backdrop = rememberBackdrop(backgroundColor)
    val compact = isCompactLandscapeChrome()
    val actionSize = if (compact) landscapeChromeSize(36.dp) else 48.dp
    val shuffleDescription = stringResource(Res.string.shuffle)
    val downloadDescription = stringResource(
        when (downloadState) {
            DownloadState.STATE_DOWNLOADED -> Res.string.downloaded
            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING -> Res.string.downloading
            else -> Res.string.download
        },
    )
    Box(modifier) {
        // A local matte source stays separate from its controls even when the whole
        // hero is captured for the toolbar. Sampling that hero here would feed back
        // the action row into its own glass layer.
        Box(Modifier.matchParentSize().layerBackdrop(backdrop).background(backgroundColor))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(landscapeChromeSize(12.dp), horizontalAlignment),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showSecondaryActions) {
                LiquidGlassIconButton(
                    backdrop = backdrop,
                    imageVector = WaveIcons.Shuffle,
                    tint = contentColor,
                    modifier = Modifier.size(actionSize).semantics { contentDescription = shuffleDescription },
                    onClick = onShuffle,
                )
            }
            Row(
                modifier = Modifier.height(if (compact) landscapeChromeSize(40.dp) else 48.dp).widthIn(min = landscapeChromeSize(110.dp))
                    .clip(CircleShape).background(Color.White)
                    .clickable(role = Role.Button, onClick = onPlayPause).padding(horizontal = landscapeChromeSize(20.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isPlaying) WaveIcons.Pause else WaveIcons.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(landscapeChromeSize(22.dp)),
                )
                Spacer(Modifier.width(landscapeChromeSize(4.dp)))
                Text(if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action), color = Color.Black, style = landscapeChromeTextStyle(typo().labelLarge))
            }
            if (showSecondaryActions) {
                LiquidGlassContainer(backdrop, Modifier.size(actionSize)) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onDownload)
                            .semantics { contentDescription = downloadDescription },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (downloadState) {
                            DownloadState.STATE_DOWNLOADED -> Icon(
                                painter = painterResource(Res.drawable.baseline_downloaded),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(landscapeChromeSize(22.dp)),
                            )
                            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING ->
                                DownloadingIndicator(Modifier.size(landscapeChromeSize(24.dp)), color = contentColor)
                            else -> Icon(
                                imageVector = WaveIcons.DownloadForOffline,
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
