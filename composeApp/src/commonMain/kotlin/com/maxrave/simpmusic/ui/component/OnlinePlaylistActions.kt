package com.maxrave.simpmusic.ui.component

import simpmusic.composeapp.generated.resources.pause_action
import simpmusic.composeapp.generated.resources.play_action

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
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.icon.DownloadForOffline
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.baseline_downloaded
import simpmusic.composeapp.generated.resources.download
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloading
import simpmusic.composeapp.generated.resources.shuffle

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
    val actionSize = if (compact) 36.dp else 48.dp
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
            horizontalArrangement = Arrangement.spacedBy(12.dp, horizontalAlignment),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showSecondaryActions) {
                LiquidGlassIconButton(
                    backdrop = backdrop,
                    imageVector = SimpIcons.Shuffle,
                    tint = contentColor,
                    modifier = Modifier.size(actionSize).semantics { contentDescription = shuffleDescription },
                    onClick = onShuffle,
                )
            }
            Row(
                modifier = Modifier.height(if (compact) 40.dp else 48.dp).widthIn(min = 110.dp)
                    .clip(CircleShape).background(Color.White)
                    .clickable(role = Role.Button, onClick = onPlayPause).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action), color = Color.Black, style = typo().labelLarge)
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
                                modifier = Modifier.size(22.dp),
                            )
                            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING ->
                                DownloadingIndicator(Modifier.size(24.dp), color = contentColor)
                            else -> Icon(
                                imageVector = SimpIcons.DownloadForOffline,
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
