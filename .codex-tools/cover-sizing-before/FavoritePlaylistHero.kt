package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.favorite
import simpmusic.composeapp.generated.resources.n_track

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
    val coverRequest = rememberPlaylistCoverRequest(thumbnail)
    val compactLandscape = isCompactLandscapeChrome()
    if (compactLandscape) {
        val topPadding = 4.dp
        val bottomPadding = 8.dp
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        // The list already reserves the status bar, toolbar and its 8.dp spacer.
        val availableHeight = getScreenSizeInfo().hDP.dp - LocalPageBottomInset.current -
            statusBarHeight - pageHeaderContentHeight() - 8.dp - topPadding - bottomPadding
        val coverSize = minOf(FavoritePlaylistHeroCoverSize, availableHeight).coerceAtLeast(48.dp)
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = topPadding, bottom = bottomPadding),
        ) {
            // Capture the ambient ground as a sibling; the controls must not capture themselves.
            Box(Modifier.matchParentSize().frostedHeaderSource(backdrop, auroraBackground = backgroundColor))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                FavoritePlaylistCover(coverRequest = coverRequest, size = coverSize)
                Spacer(Modifier.size(24.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.favorite),
                        modifier = Modifier.fillMaxWidth().then(
                            stickyTitle?.sourceModifier(typo().titleMedium, hideSource = false) ?: Modifier,
                        ),
                        style = typo().titleMedium,
                        color = contentColor,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                        style = typo().bodyMedium,
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
                        accentColor = MaterialTheme.colorScheme.primary,
                        onPlayPause = onPlayPause,
                        onShuffle = onShuffle,
                        onDownload = onDownload,
                        enabled = trackCount > 0,
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
                FavoritePlaylistCover(coverRequest = coverRequest)
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
            accentColor = MaterialTheme.colorScheme.primary,
            onPlayPause = onPlayPause,
            onShuffle = onShuffle,
            onDownload = onDownload,
            enabled = trackCount > 0,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun FavoritePlaylistCover(coverRequest: ImageRequest, size: Dp = FavoritePlaylistHeroCoverSize) {
    AsyncImage(
        model = coverRequest,
        placeholder = rememberHolderPainter(),
        error = rememberHolderPainter(),
        contentDescription = "Latest favorite song cover",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)),
    )
}
