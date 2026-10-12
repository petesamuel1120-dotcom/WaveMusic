@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import coil3.size.Scale
import com.petedev.wavemusic.domain.data.entities.ArtistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.repository.ArtistRepository
import com.petedev.wavemusic.utils.LibraryArtistThumbnailLoader
import com.petedev.wavemusic.utils.initialArtistThumbnailCount
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.Proxy
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class LibraryArtistThumbnailLoaderTest {
    private suspend fun CoroutineScope.awaitRequests() {
        coroutineContext[Job]!!.children.toList().joinAll()
    }

    @Test
    fun libraryPreloaderWarmsTheActualImageCacheBeforeArtistsIsOpened() = runBlocking<Unit> {
        val imageFile = File.createTempFile("library-artist-portrait-", ".png")
        ImageIO.write(BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB), "png", imageFile)
        val thumbnail = imageFile.toURI().toString().replace("file:/", "file:///")
        var lookups = 0
        val repository = Proxy.newProxyInstance(ArtistRepository::class.java.classLoader,
            arrayOf(ArtistRepository::class.java)) { proxy, method, args ->
            when (method.name) {
                "getArtistById" -> {
                    lookups++
                    flowOf(ArtistEntity(args!![0] as String, "Artist", thumbnail))
                }
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                "toString" -> "CachedArtistRepository"
                else -> error("A stored artist portrait must not need ${method.name}")
            }
        } as ArtistRepository
        startKoin { modules(module { single<ArtistRepository> { repository } }) }
        val cache = mutableStateMapOf<String, String>()
        val enabled = mutableStateOf(false)
        val sampleSong = SongEntity(
            videoId = "song", artistId = listOf("artist"), artistName = listOf("Artist"),
            duration = "3:00", durationSeconds = 180, isAvailable = true, isExplicit = false,
            likeStatus = "LIKE", title = "Song", videoType = "MUSIC_VIDEO_TYPE_ATV", category = null, resultType = null,
        )
        val songs = (0 until 100).map { sampleSong.copy(videoId = "song-$it", artistId = listOf("artist-$it")) }
        val scene = ImageComposeScene(120, 120) {
            // Compose only the Library preloader: no Artists grid or artist row is mounted.
            PreloadLibraryArtistThumbnails(songs, cache, enabled.value, 1, "Your library")
        }
        var time = 0L
        fun frame() { scene.render(time.also { time += 16_666_667L }).close() }
        try {
            frame()
            assertEquals(0, lookups, "Thumbnail work waits for Library to become active")
            enabled.value = true
            val imageLoader = SingletonImageLoader.get(PlatformContext.INSTANCE)
            val deadline = System.nanoTime() + 5_000_000_000L
            while (imageLoader.memoryCache?.keys?.none { it.key.startsWith(thumbnail) } != false && System.nanoTime() < deadline) {
                frame()
                Thread.sleep(5)
            }
            assertEquals(mapOf("artist-0" to thumbnail), cache.toMap(), "Do not preload the other 99 artists")
            assertEquals(1, lookups)
            val result = imageLoader.execute(
                ImageRequest.Builder(PlatformContext.INSTANCE).data(thumbnail).diskCacheKey(thumbnail)
                    .size(48).scale(Scale.FILL).build(),
            )
            val rowImage = assertIs<SuccessResult>(result, (result as? ErrorResult)?.throwable?.stackTraceToString())
            assertEquals(DataSource.MEMORY_CACHE, rowImage.dataSource,
                "The image used by an artist row must already be loaded before opening Artists")
        } finally {
            scene.close()
            stopKoin()
            imageFile.delete()
        }
    }

    @Test
    fun initialBudgetUsesTheAvailableViewportAndIncludesOnlyThePartlyVisibleLastRow() {
        assertEquals(9, initialArtistThumbnailCount(900, 300f, 68f))
        assertEquals(2, initialArtistThumbnailCount(400, 300f, 68f))
        assertEquals(6, initialArtistThumbnailCount(900, 300f, 104f), "Larger text reduces the initial count")
        assertEquals(0, initialArtistThumbnailCount(0, 300f, 68f), "Wait for the measured viewport")
        assertEquals(0, initialArtistThumbnailCount(200, 300f, 68f))
    }

    @Test
    fun artistsBelowTheInitialViewportLoadOnlyWhenTheyBecomeVisible() {
        val requested = mutableSetOf<String>()
        val cache = mutableStateMapOf<String, String>()
        val sample = SongEntity(
            videoId = "song", artistId = listOf("artist"), artistName = listOf("Artist"),
            duration = "3:00", durationSeconds = 180, isAvailable = true, isExplicit = false,
            likeStatus = "LIKE", title = "Song", videoType = "MUSIC_VIDEO_TYPE_ATV", category = null, resultType = null,
        )
        val songs = (0 until 100).map { sample.copy(videoId = "song-$it", artistId = listOf("artist-$it")) }
        val state = LazyGridState()
        var gridScope: CoroutineScope? = null
        val scene = ImageComposeScene(360, 210) {
            val scope = rememberCoroutineScope()
            gridScope = scope
            val loader = remember(scope) {
                LibraryArtistThumbnailLoader(scope, cache,
                    resolveThumbnail = { id -> requested += id; null },
                    preloadImage = { error("No image is available for this fixture") },
                )
            }
            MaterialTheme {
                FavoriteArtistsGrid(songs, state, cache, loader, PaddingValues(), 0.dp) {}
            }
        }
        var time = 0L
        fun frames() {
            repeat(6) { scene.render(time.also { time += 16_666_667L }).close() }
        }
        try {
            frames()
            assertTrue(requested.isNotEmpty())
            assertTrue(requested.all { it.removePrefix("artist-").toInt() < 4 }, "Load only the first visible rows: $requested")
            val scrolling = assertNotNull(gridScope).launch { state.scrollToItem(51) }
            frames()
            assertTrue(scrolling.isCompleted)
            assertTrue("artist-50" in requested, "Scrolling must start the newly visible artist's load")
            assertTrue("artist-99" !in requested, "The rest of a large page must stay unloaded")
            assertTrue(requested.size <= 8, "Do not load lazy precomposition or the skipped artists: $requested")
        } finally { scene.close() }
    }
    @Test
    fun libraryEntryLoadsAllArtistsWithoutComposingArtistRowsAndReusesCachedPortraits() = runBlocking<Unit> {
        val cache = mutableMapOf("stored" to "portrait-stored")
        val lookups = mutableListOf<String>()
        val images = mutableListOf<String>()
        val loader = LibraryArtistThumbnailLoader(this, cache,
            resolveThumbnail = { id -> lookups += id; "portrait-$id" },
            preloadImage = { url -> images += url; true },
        )
        loader.preload(listOf("stored", "first", "offscreen", "first", ""))
        awaitRequests()
        assertEquals(listOf("first", "offscreen"), lookups)
        assertEquals(listOf("portrait-stored", "portrait-first", "portrait-offscreen"), images)
        assertEquals("portrait-offscreen", cache["offscreen"])
        loader.preload(listOf("stored", "first", "offscreen"))
        awaitRequests()
        assertEquals(3, images.size, "Opening Artists after Library entry must reuse the completed loads")
        assertEquals(2, lookups.size)
    }

    @Test
    fun changingCategoryWhileLoadingDoesNotDuplicateRequestsAndConcurrencyIsBounded() = runBlocking<Unit> {
        val gate = CompletableDeferred<Unit>()
        val calls = mutableListOf<String>()
        var active = 0
        var peak = 0
        val loader = LibraryArtistThumbnailLoader(this, mutableMapOf(),
            resolveThumbnail = { id ->
                calls += id
                active++
                peak = maxOf(peak, active)
                gate.await()
                active--
                "portrait-$id"
            },
            preloadImage = { true },
        )
        val artists = (1..12).map(Int::toString)
        loader.preload(artists)
        yield()
        loader.preload(artists)
        yield()
        assertEquals(4, calls.size)
        gate.complete(Unit)
        awaitRequests()
        assertEquals(artists, calls)
        assertEquals(4, peak)
    }

    @Test
    fun unavailableArtistsAndFailedImagesRetryWithoutReloadingSuccessfulArtists() = runBlocking<Unit> {
        val cache = mutableMapOf<String, String>()
        val lookupCalls = mutableMapOf<String, Int>()
        var imageCalls = 0
        val loader = LibraryArtistThumbnailLoader(this, cache,
            resolveThumbnail = { id ->
                val attempt = lookupCalls.getOrDefault(id, 0) + 1
                lookupCalls[id] = attempt
                when {
                    id == "offline" && attempt == 1 -> error("Offline")
                    id == "missing" && attempt == 1 -> " "
                    else -> "portrait-$id"
                }
            },
            preloadImage = { url ->
                if (url == "portrait-image-retry") ++imageCalls > 1 else true
            },
        )
        val artists = listOf("good", "offline", "missing", "image-retry")
        loader.preload(artists)
        awaitRequests()
        assertEquals(setOf("good", "image-retry"), cache.keys)
        loader.preload(artists)
        awaitRequests()
        assertEquals(artists.toSet(), cache.keys)
        assertEquals(1, lookupCalls["good"])
        assertEquals(1, lookupCalls["image-retry"], "Retry the image using the already resolved portrait URL")
        assertEquals(2, lookupCalls["offline"])
        assertEquals(2, lookupCalls["missing"])
        assertEquals(2, imageCalls)
    }

    @Test
    fun changesToFavoriteSongsLoadNewArtistsAndKeepCompletedPortraits() = runBlocking<Unit> {
        val calls = mutableListOf<String>()
        val loader = LibraryArtistThumbnailLoader(this, mutableMapOf(),
            resolveThumbnail = { id -> calls += id; "portrait-$id" },
            preloadImage = { true },
        )
        loader.preload(listOf("first"))
        awaitRequests()
        loader.preload(listOf("first", "new"))
        awaitRequests()
        assertEquals(listOf("first", "new"), calls)
    }

    @Test
    fun leavingLibraryCancelsItsWorkWithoutPublishingIncompletePortraits() = runBlocking<Unit> {
        val owner = CoroutineScope(coroutineContext + Job())
        val cache = mutableMapOf<String, String>()
        var cancelled = false
        val loader = LibraryArtistThumbnailLoader(owner, cache,
            resolveThumbnail = {
                try { delay(10_000); "portrait" } finally { cancelled = true }
            },
            preloadImage = { true },
        )
        loader.preload(listOf("artist"))
        yield()
        owner.coroutineContext[Job]!!.cancelAndJoin()
        assertTrue(cancelled)
        assertTrue(cache.isEmpty())
    }
}
