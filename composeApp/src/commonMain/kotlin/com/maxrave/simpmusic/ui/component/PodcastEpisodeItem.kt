package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.domain.data.model.podcast.PodcastBrowse
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.painterResource
import simpmusic.composeapp.generated.resources.Res

@Composable
fun PodcastEpisodeFullWidthItem(
    modifier: Modifier = Modifier,
    episode: PodcastBrowse.EpisodeItem,
    onClick: (String) -> Unit,
    onMoreClickListener: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    playbackProgress: Float? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick(episode.videoId) }
                .animateContentSize(),
    ) {
        Row(
            Modifier
                .padding(vertical = 10.dp, horizontal = pageContentHorizontalInset(12.dp))
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.width(50.dp)) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(episode.thumbnail.lastOrNull()?.url)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(episode.thumbnail.lastOrNull()?.url)
                            .crossfade(true)
                            .build(),
                    placeholder = rememberHolderPainter(),
                    error = rememberHolderPainter(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.size(50.dp),
                )
                playbackProgress?.let { progress ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp)
                            .size(width = 50.dp, height = 3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                        )
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp)
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = episode.title,
                    style = typo().labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically),
                )

                Text(
                    text = "${episode.createdDay ?: ""}${if (!episode.durationString.isNullOrEmpty()) " • ${episode.durationString}" else ""}",
                    style = typo().bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically),
                )

                val description = episode.description
                if (description != null) {
                    Text(
                        text = description,
                        style = typo().bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(align = Alignment.CenterVertically),
                    )
                }
            }

            if (trailingContent != null) {
                trailingContent()
            } else if (onMoreClickListener != null) {
                RippleIconButton(
                    imageVector = SimpIcons.MoreVert,
                    fillMaxSize = false,
                    tint = MaterialTheme.colorScheme.onSurface,
                    showOnyxContainer = false,
                ) {
                    onMoreClickListener.invoke()
                }
            }
        }
    }
}
