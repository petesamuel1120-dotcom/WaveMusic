package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.ui.icon.Shuffle
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.n_track
import com.petedev.wavemusic.resources.shuffle

@Composable
internal fun ArtistLikedSongsHero(
    title: String,
    thumbnail: String?,
    trackCount: Int,
    backgroundColor: Color,
    onShuffle: () -> Unit,
    stickyTitle: StickyPlaylistTitleState?,
    onArtistClick: () -> Unit,
) {
    val backdrop = rememberBackdrop(backgroundColor)
    val screen = getScreenSizeInfo()
    // Keep the hero's hit target even when its paint is supplied by the header overlay.
    val artistNameClick = Modifier.clickable(onClick = onArtistClick)
    if (screen.wDP >= screen.hDP) {
        val landscapeHero = onlineLandscapeHeroLayout(maxCoverSize = 220.dp, extraTopPadding = 8.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = landscapeHero.horizontalPadding)
                .padding(top = 4.dp, bottom = 8.dp),
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .frostedHeaderSource(backdrop, auroraBackground = backgroundColor),
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = thumbnail,
                    placeholder = rememberHolderPainter(),
                    error = rememberHolderPainter(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(landscapeHero.coverSize).clip(RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.size(landscapeHero.columnSpacing))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth().then(artistNameClick).then(
                            stickyTitle?.sourceModifier(landscapeChromeTextStyle(typo().titleMedium), textAlign = TextAlign.Start) ?: Modifier,
                        ),
                        style = landscapeChromeTextStyle(typo().titleMedium),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                        style = landscapeChromeTextStyle(typo().bodyMedium),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(12.dp))
                    PlaylistGlassActionPill(
                        backdrop = backdrop,
                        icon = WaveIcons.Shuffle,
                        text = stringResource(Res.string.shuffle),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = onShuffle,
                        enabled = trackCount > 0,
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth(),
                    )
                }
            }
        }
        return
    }
    Box(Modifier.fillMaxWidth()) {
        // Keep the captured source behind its glass controls; including the pill in its own
        // backdrop can create a recursive capture on Liquid Glass devices.
        Box(
            Modifier
                .matchParentSize()
                .frostedHeaderSource(backdrop, auroraBackground = backgroundColor),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = glassActionSize() + 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = thumbnail,
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(180.dp).clip(RoundedCornerShape(18.dp)),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = title,
                modifier = artistNameClick.then(stickyTitle?.sourceModifier(typo().titleLarge) ?: Modifier),
                style = typo().titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = pluralStringResource(Res.plurals.n_track, trackCount, trackCount),
                style = typo().bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
        PlaylistGlassActionPill(
            backdrop = backdrop,
            icon = WaveIcons.Shuffle,
            text = stringResource(Res.string.shuffle),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onShuffle,
            enabled = trackCount > 0,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .widthIn(max = 240.dp)
                    .fillMaxWidth(),
        )
    }
}
