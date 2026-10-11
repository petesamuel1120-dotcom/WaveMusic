package com.maxrave.simpmusic.ui.component

import androidx.lifecycle.viewModelScope
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.mediaservice.handler.NowPlayingTrackState
import com.maxrave.domain.mediaservice.handler.ControlState
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.LocalResource
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.lang.reflect.Proxy
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectedSongsPlaylistCreationTest {
    private class Fixture(val authenticated: Boolean, val failSync: Boolean = false) : AutoCloseable {
        val inserted = mutableListOf<Pair<LocalPlaylistEntity, List<String>>>()
        var syncCalls = 0
        var pendingCalls = 0
        var libraryReads = 0
        val loggedIn = MutableStateFlow(if (authenticated) DataStoreManager.TRUE else DataStoreManager.FALSE)
        val cookie = MutableStateFlow(if (authenticated) "session" else "")
        var library = listOf(remote("VLLM", "Liked Music"), remote("VLplayer", "Player mix"), remote("VLlibrary", "Library mix"))
        val localRows = MutableStateFlow(emptyList<LocalPlaylistEntity>())
        val remoteAdds = mutableListOf<Pair<String, String>>()
        val localAdds = mutableListOf<String>()
        var cachedSongs = listOf(song("second"), song("first"))
        val seededSongs = mutableListOf<String>()
        var remoteDelay = 0L
        var libraryDelay = 0L
        var completedRemoteAdds = 0
        val remoteThrowIds = mutableSetOf<String>()
        val remoteRepository = fake<PlaylistRepository> { name, _ ->
            when (name) {
                "getLibraryPlaylist" -> {
                    libraryReads++
                    flow<List<PlaylistsResult>?> { delay(libraryDelay); emit(library) }
                }
                else -> error("Unexpected remote access: $name")
            }
        }
        val settings = fake<DataStoreManager> { name, _ ->
            when (name) {
                "getLoggedIn" -> loggedIn
                "getCookie" -> cookie
                else -> error("Unexpected setting: $name")
            }
        }
        val media = fake<MediaPlayerHandler> { name, _ ->
            when (name) {
                "getNowPlayingState" -> MutableStateFlow(NowPlayingTrackState.initial())
                "getControlState" -> MutableStateFlow(ControlState(false, false, RepeatState.None, false, false, false, false, 1f))
                else -> error("Unexpected player access: $name")
            }
        }
        val songs = fake<SongRepository> { name, args ->
            when (name) {
                "getSongsByListVideoId" -> {
                    @Suppress("UNCHECKED_CAST")
                    val ids = args!![0] as List<String>
                    flowOf(cachedSongs.filter { it.videoId in ids })
                }
                "insertSong" -> {
                    val row = args!![0] as SongEntity
                    seededSongs += row.videoId
                    cachedSongs = cachedSongs + row
                    flowOf(1L)
                }
                else -> error("Unexpected song access: $name")
            }
        }
        val playlists = fake<LocalPlaylistRepository> { name, args ->
            when (name) {
                "getAllLocalPlaylists" -> localRows
                "addYouTubePlaylistItem" -> {
                    remoteAdds += (args!![0] as String) to (args[1] as String)
                    if (args[1] in remoteThrowIds) error("Test request failure")
                    // The repository can emit more than one success for one operation.
                    flow {
                        delay(remoteDelay)
                        emit(LocalResource.Success("STATUS_SUCCEEDED"))
                        emit(LocalResource.Success("STATUS_SUCCEEDED"))
                        completedRemoteAdds++
                    }
                }
                "addTrackToLocalPlaylist" -> {
                    localAdds += (args!![1] as SongEntity).videoId
                    flowOf(LocalResource.Success("Added"))
                }
                "insertLocalPlaylistWithTracks" -> {
                    @Suppress("UNCHECKED_CAST")
                    inserted += (args!![0] as LocalPlaylistEntity) to (args[1] as List<String>)
                    42L
                }
                "syncLocalPlaylistToYouTubePlaylist" -> {
                    syncCalls++
                    if (failSync) flowOf(LocalResource.Error<String>("Sync failed"))
                    else flowOf(LocalResource.Success("Synced"))
                }
                "updateLocalPlaylistYouTubePlaylistSyncState" -> {
                    assertEquals(42L, args!![0])
                    assertEquals(LocalPlaylistEntity.YouTubeSyncState.PendingSync, args[1])
                    pendingCalls++
                    Unit
                }
                else -> error("Unexpected playlist access: $name")
            }
        }
        lateinit var model: SongSelectionViewModel
        init {
            startKoin { modules(module {
                single<MediaPlayerHandler> { media }
                single<DataStoreManager> { settings }
                single<PlaylistRepository> { remoteRepository }
            }) }
            onMain { model = SongSelectionViewModel(songs, playlists) }
        }
        fun create(title: String = "  Mix  ", ids: List<String> = listOf("first", "second", "first")) {
            onMain { model.createPlaylistWithSelectedSongs(title, ids) }
        }
        fun openPicker() {
            onMain {
                model.viewModelScope.launch { model.listYouTubePlaylist.collect {} }
                model.refreshYouTubePlaylists()
            }
        }
        fun await(condition: () -> Boolean) {
            val deadline = System.nanoTime() + 3_000_000_000L
            while (!condition() && System.nanoTime() < deadline) { onMain {}; Thread.sleep(5) }
            assertTrue(condition())
        }
        override fun close() {
            onMain { model.viewModelScope.cancel() }
            stopKoin()
        }
    }

    @Test fun loggedOutCreationKeepsAllSelectedTracksInOrderWithoutDuplicates() {
        Fixture(false).use { f ->
            f.create()
            f.await { f.inserted.size == 1 }
            assertEquals("Mix", f.inserted.single().first.title)
            assertEquals(listOf("first", "second"), f.inserted.single().second)
            assertEquals(LocalPlaylistEntity.YouTubeSyncState.PendingSync, f.inserted.single().first.syncState)
            assertEquals(0, f.syncCalls)
        }
    }

    @Test fun loggedInCreationUsesExistingYouTubeSync() {
        Fixture(true).use { f ->
            f.create()
            f.await { f.syncCalls == 1 }
            assertEquals(1, f.inserted.size)
            assertEquals(LocalPlaylistEntity.YouTubeSyncState.Syncing, f.inserted.single().first.syncState)
            assertEquals(listOf("first", "second"), f.inserted.single().second)
        }
    }

    @Test fun syncFailurePreservesTheLocalPlaylistForRetry() {
        Fixture(true, failSync = true).use { f ->
            f.create()
            f.await { f.pendingCalls == 1 }
            assertEquals(1, f.inserted.size)
        }
    }

    @Test fun emptyNamesAndSelectionsDoNotCreatePlaylists() {
        Fixture(false).use { f ->
            f.create("   ")
            f.create(ids = emptyList())
            onMain {}
            assertTrue(f.inserted.isEmpty())
        }
    }

    @Test fun pickerIncludesPlaylistsCreatedInPlayerAndLibraryButNotLikedMusic() {
        Fixture(true).use { f ->
            f.openPicker()
            f.await { f.model.listYouTubePlaylist.value.size == 2 }
            assertEquals(listOf("Player mix", "Library mix"), f.model.listYouTubePlaylist.value.map { it.title })
        }
    }

    @Test fun reopeningPickerRefreshesPlaylistsCreatedElsewhere() {
        Fixture(true).use { f ->
            f.openPicker()
            f.await { f.model.listYouTubePlaylist.value.size == 2 }
            f.library = f.library + remote("VLnew", "New player mix")
            onMain { f.model.refreshYouTubePlaylists() }
            f.await { f.model.listYouTubePlaylist.value.size == 3 }
            assertTrue(f.libraryReads >= 2)
        }
    }

    @Test fun loggedOutPickerDoesNotRequestTheYouTubeLibrary() {
        Fixture(false).use { f ->
            f.openPicker()
            onMain {}
            assertTrue(f.model.listYouTubePlaylist.value.isEmpty())
            assertEquals(0, f.libraryReads)
        }
    }

    @Test fun logoutClearsTheRemotePlaylistList() {
        Fixture(true).use { f ->
            f.openPicker()
            f.await { f.model.listYouTubePlaylist.value.size == 2 }
            onMain { f.loggedIn.value = DataStoreManager.FALSE; f.cookie.value = "" }
            f.await { f.model.listYouTubePlaylist.value.isEmpty() }
        }
    }

    @Test fun remotePlaylistReceivesEverySelectedTrackOnceInOrder() {
        Fixture(true).use { f ->
            onMain { f.model.addToYouTubePlaylist(remote("VLplayer", "Player mix"), listOf("first", "second", "first")) }
            f.await { f.remoteAdds.size == 2 }
            assertEquals(listOf("VLplayer" to "first", "VLplayer" to "second"), f.remoteAdds)
            assertTrue(f.localAdds.isEmpty())
        }
    }

    @Test fun linkedPlaylistUsesTheLocalMirrorPathWithNormalizedIds() {
        Fixture(true).use { f ->
            f.localRows.value = listOf(LocalPlaylistEntity(id = 42L, title = "Linked", youtubePlaylistId = "player", tracks = emptyList()))
            onMain { f.model.addToYouTubePlaylist(remote("VLplayer", "Linked"), listOf("first", "second")) }
            f.await { f.localAdds.size == 2 }
            assertEquals(listOf("first", "second"), f.localAdds)
            assertTrue(f.remoteAdds.isEmpty())
        }
    }

    @Test fun all25VisibleTracksAreAddedEvenWhenOnlyFourWereCached() {
        Fixture(true).use { f ->
            val tracks = (0 until 25).map { song("track-$it") }
            f.cachedSongs = tracks.take(4)
            f.localRows.value = listOf(LocalPlaylistEntity(id = 42L, title = "Local", tracks = emptyList()))
            onMain { f.model.addToPlaylist(42L, tracks.map { it.videoId }, tracks) }
            f.await { f.localAdds.size == 25 }
            assertEquals(tracks.map { it.videoId }, f.localAdds)
            assertEquals(tracks.drop(4).map { it.videoId }, f.seededSongs)
        }
    }

    @Test fun navigationDoesNotCancelThe25TrackRemoteAddition() {
        Fixture(true).use { f ->
            val ids = (0 until 25).map { "track-$it" }
            f.remoteDelay = 10L
            onMain { f.model.addToYouTubePlaylist(remote("VLplayer", "Player mix"), ids) }
            f.await { f.remoteAdds.size >= 4 }
            onMain { f.model.viewModelScope.cancel() }
            f.await { f.completedRemoteAdds == 25 }
            assertEquals(ids, f.remoteAdds.map { it.second })
        }
    }

    @Test fun oneFailedRequestDoesNotSilentlyStopTheRemainingSelection() {
        Fixture(true).use { f ->
            val ids = (0 until 25).map { "track-$it" }
            f.remoteThrowIds += "track-4"
            onMain { f.model.addToYouTubePlaylist(remote("VLplayer", "Player mix"), ids) }
            f.await { f.completedRemoteAdds == 24 }
            assertEquals(ids, f.remoteAdds.map { it.second })
        }
    }

    @Test fun incompleteMetadataCannotSilentlyAddOnlyFourOf25Tracks() {
        Fixture(true).use { f ->
            val ids = (0 until 25).map { "track-$it" }
            f.cachedSongs = ids.take(4).map(::song)
            f.localRows.value = listOf(LocalPlaylistEntity(id = 42L, title = "Local", tracks = emptyList()))
            onMain { f.model.addToPlaylist(42L, ids) }
            onMain {}
            assertTrue(f.localAdds.isEmpty())
        }
    }

    @Test fun pickerLoadingStateWaitsForTheBackendList() {
        Fixture(true).use { f ->
            f.libraryDelay = 150L
            f.openPicker()
            f.await { f.libraryReads > 0 }
            assertTrue(f.model.playlistListLoading.value)
            f.await { f.model.listYouTubePlaylist.value.size == 2 && !f.model.playlistListLoading.value }
        }
    }

    private companion object {
        fun remote(id: String, title: String) = PlaylistsResult("Owner", id, "Playlist", "1", "Playlist", emptyList(), title)
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
        fun song(id: String) = SongEntity(
            videoId = id, title = id, artistId = listOf("artist"), artistName = listOf("Artist"),
            duration = "3:00", durationSeconds = 180, isAvailable = true, isExplicit = false,
            likeStatus = "INDIFFERENT", videoType = "MUSIC_VIDEO_TYPE_ATV", category = "Song", resultType = "Song",
        )
    }
}
