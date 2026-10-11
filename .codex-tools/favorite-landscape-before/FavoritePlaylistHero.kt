package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
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
    var heroTopInRoot by remember { mutableIntStateOf(0) }
    var trackCountBottomInRoot by remember { mutableIntStateOf(Int.MIN_VALUE) }
    val density = LocalDensity.current
    Box(
        Modifier.fillMaxWidth().onGloballyPositioned {
            heroTopInRoot = it.positionInRoot().y.toInt()
        },
    ) {
        // Capture the artwork and ambient ground separately from the glass action controls.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .frostedHeaderSource(backdrop, auroraBackground = backgroundColor)
                    .padding(horizontal = if (compactLandscape) 28.dp else 16.dp)
                    .padding(top = 12.dp, bottom = if (compactLandscape) 16.dp else FavoritePlaylistHeroActionSpace),
        ) {
            if (compactLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    FavoritePlaylistCover(coverRequest = coverRequest)
                    Spacer(Modifier.size(24.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.favorite),
                            modifier = stickyTitle?.sourceModifier(typo().titleMedium) ?: Modifier,
                            style = typo().titleMedium,
                            color = contentColor,
                            textAlign = TextAlign.Start,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                            style = typo().bodyMedium,
                            color = contentColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.onGloballyPositioned {
                                trackCountBottomInRoot = (it.positionInRoot().y + it.size.height).toInt()
                            },
                        )
                    }
                }
            } else {
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
        }
        if (!compactLandscape) {
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
        } else if (trackCountBottomInRoot != Int.MIN_VALUE) {
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
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 28.dp + FavoritePlaylistHeroCoverSize + 24.dp, end = 28.dp)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = trackCountBottomInRoot - heroTopInRoot + with(density) { 12.dp.roundToPx() },
                            )
                        },
            )
        }
    }
}

@Composable
private fun FavoritePlaylistCover(coverRequest: ImageRequest) {
    AsyncImage(
        model = coverRequest,
        placeholder = rememberHolderPainter(),
        error = rememberHolderPainter(),
        contentDescription = "Latest favorite song cover",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(FavoritePlaylistHeroCoverSize).clip(RoundedCornerShape(12.dp)),
    )
}
