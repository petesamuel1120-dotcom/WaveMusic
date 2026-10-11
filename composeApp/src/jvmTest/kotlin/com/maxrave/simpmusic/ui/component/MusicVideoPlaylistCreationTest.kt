@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3ComponentOverrideApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewModelScope
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.*
import com.maxrave.domain.repository.*
import com.maxrave.domain.utils.LocalResource
import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.viewModel.NowPlayingBottomSheetViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.lang.reflect.Proxy
import javax.swing.SwingUtilities
import kotlin.test.*

class MusicVideoPlaylistCreationTest {
    private class Fixture(val authenticated: Boolean = false) : AutoCloseable {
        var song: SongEntity? = null
        val songFlow = MutableStateFlow<SongEntity?>(null)
        val inserted = mutableListOf<Pair<LocalPlaylistEntity, List<String>>>()
        var syncCalls = 0
        lateinit var model: NowPlayingBottomSheetViewModel
        private val media = fake<MediaPlayerHandler> { name, _ ->
            when (name) {
                "getSleepTimerState" -> MutableStateFlow(SleepTimerState(false, 0))
                "getNowPlayingState" -> MutableStateFlow(NowPlayingTrackState.initial())
                "getControlState" -> MutableStateFlow(ControlState(false, false, RepeatState.None, false, false, false, false, 1f))
                else -> error("Unexpected player access: $name")
            }
        }
        private val settings = fake<DataStoreManager> { name, _ ->
            when (name) {
                "getLoggedIn" -> flowOf(if (authenticated) DataStoreManager.TRUE else DataStoreManager.FALSE)
                "getCookie" -> flowOf(if (authenticated) "test-session" else "")
                "getString" -> flowOf<String?>(null)
                else -> error("Unexpected settings access: $name")
            }
        }
        private val local = fake<LocalPlaylistRepository> { name, args ->
            when (name) {
                "getAllLocalPlaylists" -> flowOf(emptyList<LocalPlaylistEntity>())
                "insertLocalPlaylistWithTracks" -> {
                    @Suppress("UNCHECKED_CAST")
                    inserted += (args!![0] as LocalPlaylistEntity) to (args[1] as List<String>)
                    42L
                }
                "syncLocalPlaylistToYouTubePlaylist" -> {
                    syncCalls++
                    flowOf(LocalResource.Success("Synced"))
                }
                else -> error("Unexpected playlist access: $name")
            }
        }
        private val remote = fake<PlaylistRepository> { name, _ ->
            when (name) {
                "getLibraryPlaylist" -> flowOf(emptyList<PlaylistsResult>())
                else -> error("Unexpected remote access: $name")
            }
        }
        private val songs = fake<SongRepository> { name, _ ->
            when (name) {
                "getSongById" -> flowOf(song)
                "getSongAsFlow" -> songFlow
                else -> error("Unexpected song access: $name")
            }
        }
        init {
            startKoin { modules(module { single<MediaPlayerHandler> { media } }) }
            onMain { model = NowPlayingBottomSheetViewModel(settings, local, remote, songs) }
        }
        fun load(video: Boolean) {
            song = sample(video)
            songFlow.value = song
            onMain { model.setSongEntity(song) }
            await { model.uiState.value.songUIState.videoId == song!!.videoId }
            assertEquals(video, model.uiState.value.songUIState.isVideo)
        }
        fun await(condition: () -> Boolean) {
            val deadline = System.nanoTime() + 3_000_000_000L
            while (!condition() && System.nanoTime() < deadline) { onMain {}; Thread.sleep(5) }
            assertTrue(condition(), "The creation operation must complete")
        }
        override fun close() {
            onMain { model.viewModelScope.cancel() }
            stopKoin()
        }
    }

    @Test
    fun musicVideoCreationSavesTheCurrentVideoLocallyWithoutAnAccount() {
        Fixture().use { f ->
            f.load(video = true)
            val create = assertNotNull(f.model.playlistCreationAction(), "Music videos must offer Create playlist")
            onMain { create("  MV favorites  ") }
            f.await { f.inserted.size == 1 }
            val (playlist, tracks) = f.inserted.single()
            assertEquals("MV favorites", playlist.title)
            assertEquals(listOf("music-video"), tracks)
            assertEquals(tracks, playlist.tracks)
            assertEquals(LocalPlaylistEntity.YouTubeSyncState.PendingSync, playlist.syncState)
            assertEquals(0, f.syncCalls)
        }
    }

    @Test
    fun signedInMusicVideoCreationKeepsTheExistingYouTubeSyncFlow() {
        Fixture(authenticated = true).use { f ->
            f.load(video = true)
            onMain { assertNotNull(f.model.playlistCreationAction()).invoke("Video mix") }
            f.await { f.syncCalls == 1 }
            assertEquals(listOf("music-video"), f.inserted.single().second)
            assertEquals(LocalPlaylistEntity.YouTubeSyncState.Syncing, f.inserted.single().first.syncState)
        }
    }

    @Test
    fun audioCreationStillWorksAndBlankNamesOrMissingItemsCreateNothing() {
        Fixture().use { f ->
            assertNull(f.model.playlistCreationAction())
            onMain { f.model.createYouTubeMusicPlaylistWithCurrentSong("No current item") }
            assertTrue(f.inserted.isEmpty())
            f.load(video = false)
            val create = assertNotNull(f.model.playlistCreationAction())
            onMain { create(" ") }
            assertTrue(f.inserted.isEmpty())
            onMain { create("Audio mix") }
            f.await { f.inserted.size == 1 }
            assertEquals(listOf("song"), f.inserted.single().second)
        }
    }

    @Test
    fun emptyMusicVideoPickerOffersCreationAndTheAnimatedFormCreatesTheVideoPlaylist() {
        Fixture().use { f ->
            f.load(video = true)
            val scene = ImageComposeScene(450, 900) {
                val origins = remember { DialogOriginTracker() }
                MaterialTheme(colorScheme = darkColorScheme()) {
                    CompositionLocalProvider(
                        LocalAdvancedDialogAnimations provides true,
                        LocalAppIsDarkTheme provides true,
                        LocalDialogOrigins provides origins,
                        LocalBasicAlertDialogOverride provides AnimatedAppAlertDialogOverride,
                    ) {
                        AddToPlaylistModalBottomSheet(true, emptyList(), emptyList(), "music-video",
                            onClick = {}, onYTPlaylistClick = {}, onDismiss = {},
                            onCreatePlaylist = f.model.playlistCreationAction())
                    }
                }
            }
            var time = 0L
            fun frames(count: Int = 45) { repeat(count) { scene.render(time.also { time += 16_666_667L }).close() } }
            fun nodes(): List<SemanticsNode> {
                fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
                return scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
            }
            fun text(node: SemanticsNode) = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }.orEmpty()
            try {
                frames()
                val create = nodes().first { text(it) == "Create playlist" && it.config.getOrNull(SemanticsActions.OnClick) != null }
                assertTrue(create.config[SemanticsActions.OnClick].action!!.invoke())
                frames()
                nodes().first { it.config.getOrNull(SemanticsActions.SetText) != null }
                    .config[SemanticsActions.SetText].action!!.invoke(AnnotatedString("MV mix"))
                frames(2)
                nodes().last { text(it) == "Create playlist" && it.config.getOrNull(SemanticsActions.OnClick) != null }
                    .config[SemanticsActions.OnClick].action!!.invoke()
                frames(90)
                f.await { f.inserted.size == 1 }
                assertEquals(listOf("music-video"), f.inserted.single().second)
            } finally { scene.close() }
        }
    }

    private companion object {
        fun onMain(action: () -> Unit) {
            if (SwingUtilities.isEventDispatchThread()) action() else SwingUtilities.invokeAndWait(action)
        }
        inline fun <reified T> fake(noinline handler: (String, Array<out Any?>?) -> Any?): T =
            Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
                when (method.name) {
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> proxy === args?.firstOrNull()
                    "toString" -> "Test${T::class.java.simpleName}"
                    else -> handler(method.name, args)
                }
            } as T
        fun sample(video: Boolean) = SongEntity(
            videoId = if (video) "music-video" else "song", title = "Playback item",
            artistId = listOf("artist"), artistName = listOf("Artist"), duration = "3:00", durationSeconds = 180,
            isAvailable = true, isExplicit = false, likeStatus = "INDIFFERENT",
            videoType = if (video) "MUSIC_VIDEO_TYPE_OMV" else "MUSIC_VIDEO_TYPE_ATV",
            category = if (video) "Video" else "Song", resultType = if (video) "Video" else "Song",
        )
    }
}
