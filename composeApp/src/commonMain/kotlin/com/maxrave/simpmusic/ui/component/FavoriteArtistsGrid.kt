package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.repository.ArtistRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.utils.LibraryArtistThumbnailLoader
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Precision
import coil3.size.Scale
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.no_favorite_artists

private data class FavoriteArtistRow(
    val artist: ArtistEntity,
    val songCount: Int,
)

/** Warm only the initial viewport before Artists opens; share its loader with visible rows. */
@Composable
internal fun PreloadLibraryArtistThumbnails(
    songs: List<SongEntity>,
    thumbnailCache: MutableMap<String, String>,
    enabled: Boolean,
    preloadCount: Int,
    retryKey: Any,
): LibraryArtistThumbnailLoader? {
    if (!enabled) return null
    val channelIds by produceState(emptyList<String>(), songs, preloadCount) {
        value = if (preloadCount <= 0) emptyList() else withContext(Dispatchers.Default) {
            groupFavoriteArtists(songs, limit = preloadCount).map { it.artist.channelId }
        }
    }
    val repository = koinInject<ArtistRepository>()
    val context = LocalPlatformContext.current
    val imageLoader = remember(context) { SingletonImageLoader.get(context) }
    val thumbnailSize = with(LocalDensity.current) { 48.dp.roundToPx() }
    val scope = rememberCoroutineScope()
    val loader = remember(scope, thumbnailCache, repository, imageLoader, thumbnailSize) {
        LibraryArtistThumbnailLoader(
            scope = scope,
            thumbnailCache = thumbnailCache,
            resolveThumbnail = { channelId ->
                val stored = repository.getArtistById(channelId).firstOrNull()?.thumbnails
                stored?.takeIf(String::isNotBlank) ?: run {
                    val browse = repository.getArtistData(channelId).lastOrNull()
                    if (browse is Resource.Success) {
                        browse.data?.thumbnails?.lastOrNull()?.url
                    } else null
                }
            },
            preloadImage = { thumbnail ->
                imageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(thumbnail)
                        .diskCacheKey(thumbnail)
                        .size(thumbnailSize)
                        .scale(Scale.FILL)
                        .precision(Precision.INEXACT)
                        .build(),
                ) is SuccessResult
            },
        )
    }
    LaunchedEffect(channelIds, loader, retryKey) { loader.preload(channelIds) }
    return loader
}

@Composable
internal fun FavoriteArtistsGrid(
    songs: List<SongEntity>,
    state: LazyGridState,
    thumbnailCache: MutableMap<String, String>,
    thumbnailLoader: LibraryArtistThumbnailLoader?,
    contentPadding: PaddingValues,
    categorySpacerHeight: Dp,
    onArtistClick: (String) -> Unit,
) {
    val artists = remember(songs) { groupFavoriteArtists(songs) }
    LaunchedEffect(state, artists, thumbnailLoader) {
        val artistIds = artists.mapTo(mutableSetOf()) { it.artist.channelId }
        snapshotFlow {
            state.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? String }.filter { it in artistIds }
        }.distinctUntilChanged().collect { thumbnailLoader?.preload(it) }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        state = state,
        contentPadding = contentPadding,
    ) {
        item(key = "library-category-row-spacer", span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.fillMaxWidth().height(categorySpacerHeight))
        }
        if (artists.isEmpty()) {
            item(key = "favorite-artists-empty") {
                Box(
                    Modifier.fillMaxWidth().height(200.dp).padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.no_favorite_artists),
                        style = typo().bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(artists, key = { it.artist.channelId }) { row ->
                FavoriteArtistItem(
                    row = row,
                    thumbnailCache = thumbnailCache,
                    subtitle = pluralStringResource(Res.plurals.liked_songs_count, row.songCount, row.songCount),
                    onClick = { onArtistClick(row.artist.channelId) },
                )
            }
        }
    }
}

@Composable
private fun FavoriteArtistItem(
    row: FavoriteArtistRow,
    thumbnailCache: MutableMap<String, String>,
    subtitle: String,
    onClick: () -> Unit,
) {
    val channelId = row.artist.channelId
    ArtistFullWidthItems(
        data = row.artist.copy(thumbnails = thumbnailCache[channelId]),
        subtitle = subtitle,
        startContentPadding = AppContentHorizontalInset,
        endContentPadding = AppContentHorizontalInset,
        onClickListener = onClick,
    )
}

private fun groupFavoriteArtists(songs: List<SongEntity>, limit: Int = Int.MAX_VALUE): List<FavoriteArtistRow> {
    data class Accumulator(
        val id: String,
        val name: String,
        var thumbnail: String?,
        var count: Int = 0,
    )

    val grouped = linkedMapOf<String, Accumulator>()
    songs.forEach { song ->
        song.artistId
            .orEmpty()
            .withIndex()
            .distinctBy { it.value }
            .forEach artist@{ (index, id) ->
                if (id.isBlank()) return@artist
                val name = song.artistName?.getOrNull(index)?.takeIf(String::isNotBlank) ?: return@artist
                val artist = grouped.getOrPut(id) { Accumulator(id, name, song.thumbnails) }
                if (artist.thumbnail.isNullOrBlank()) artist.thumbnail = song.thumbnails
                artist.count++
            }
    }
    return grouped.values
        .sortedWith(compareByDescending<Accumulator> { it.count }.thenBy { it.name.lowercase() })
        .take(limit.coerceAtLeast(0))
        .map { row ->
            FavoriteArtistRow(
                artist = ArtistEntity(channelId = row.id, name = row.name, thumbnails = row.thumbnail),
                songCount = row.count,
            )
        }
}
