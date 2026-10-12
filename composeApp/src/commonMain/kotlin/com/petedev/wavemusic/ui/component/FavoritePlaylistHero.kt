package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.favorite
import com.petedev.wavemusic.resources.logo_grey
import com.petedev.wavemusic.resources.n_track

internal val FavoritePlaylistHeroActionSpace = 88.dp
private val FavoritePlaylistHeroCoverSize = 180.dp

@Composable
fun FavoritePlaylistHero(
    thumbnail: String?,
    trackCount: Int,
    isPlaying: Boolean,
    downloadState: Int,
    backgroundColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onDownload: () -> Unit,
    stickyTitle: StickyPlaylistTitleState? = null,
) {
    val backdrop = rememberBackdrop(backgroundColor)
    val contentColor = MaterialTheme.colorScheme.onBackground
    val accentColor = MaterialTheme.colorScheme.primary
    val coverRequest = rememberPlaylistCoverRequest(thumbnail)
    val screen = getScreenSizeInfo()
    val isLandscape = screen.wDP >= screen.hDP
    if (isLandscape) {
        val topPadding = 4.dp
        val bottomPadding = 8.dp
        // The list already reserves the status bar, toolbar and its 8.dp spacer.
        val landscapeHero = onlineLandscapeHeroLayout(extraTopPadding = 8.dp)
        Box(
            Modifier.fillMaxWidth().padding(horizontal = landscapeHero.horizontalPadding)
                .padding(top = topPadding, bottom = bottomPadding),
        ) {
            // Capture the ambient ground as a sibling; the controls must not capture themselves.
            Box(Modifier.matchParentSize().frostedHeaderSource(backdrop, auroraBackground = backgroundColor))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                FavoritePlaylistCover(coverRequest = coverRequest, showLogo = trackCount == 0, size = landscapeHero.coverSize)
                Spacer(Modifier.size(landscapeHero.columnSpacing))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.favorite),
                        modifier = Modifier.fillMaxWidth().then(
                            stickyTitle?.sourceModifier(landscapeChromeTextStyle(typo().titleMedium), textAlign = androidx.compose.ui.text.style.TextAlign.Start) ?: Modifier,
                        ),
                        style = landscapeChromeTextStyle(typo().titleMedium),
                        color = contentColor,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                        style = landscapeChromeTextStyle(typo().bodyMedium),
                        color = contentColor.copy(alpha = 0.6f),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))
                    PlaylistGlassActions(
                        backdrop = backdrop,
                        isPlaying = isPlaying,
                        downloadState = downloadState,
                        contentColor = contentColor,
                        accentColor = accentColor,
                        onPlayPause = onPlayPause,
                        onShuffle = onShuffle,
                        onDownload = onDownload,
                        shuffleAsSideButton = true,
                        enabled = trackCount > 0,
                        playContainerColor = accentColor,
                        playContentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
        return
    }
    Box(Modifier.fillMaxWidth()) {
        // Capture the artwork and ambient ground separately from the glass action controls.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .frostedHeaderSource(backdrop, auroraBackground = backgroundColor)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = FavoritePlaylistHeroActionSpace),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FavoritePlaylistCover(coverRequest = coverRequest, showLogo = trackCount == 0)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.favorite),
                    modifier = stickyTitle?.sourceModifier(typo().titleMedium) ?: Modifier,
                    style = typo().titleMedium,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                    style = typo().bodyMedium,
                    color = contentColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        PlaylistGlassActions(
            backdrop = backdrop,
            isPlaying = isPlaying,
            downloadState = downloadState,
            contentColor = contentColor,
            accentColor = accentColor,
            onPlayPause = onPlayPause,
            onShuffle = onShuffle,
            onDownload = onDownload,
            shuffleAsSideButton = true,
            enabled = trackCount > 0,
            playContainerColor = accentColor,
            playContentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun FavoritePlaylistCover(coverRequest: ImageRequest, showLogo: Boolean, size: Dp = FavoritePlaylistHeroCoverSize) {
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
