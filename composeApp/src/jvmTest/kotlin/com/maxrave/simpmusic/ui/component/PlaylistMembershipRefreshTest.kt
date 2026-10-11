package com.maxrave.simpmusic.ui.component

import androidx.lifecycle.viewModelScope
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.browse.playlist.Author
import com.maxrave.domain.data.model.browse.playlist.PlaylistBrowse
import com.maxrave.domain.mediaservice.handler.*
import com.maxrave.domain.repository.*
import com.maxrave.domain.utils.*
import com.maxrave.simpmusic.viewModel.PlaylistViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.lang.reflect.Proxy
import javax.swing.SwingUtilities
import kotlin.test.*

class PlaylistMembershipRefreshTest {
    private class Fixture(val id: String) : AutoCloseable {
        var remoteTracks = listOf(track("song"))
        var entity: PlaylistEntity? = null
        var requests = 0
        var requiresMembership: Boolean? = null
        lateinit var model: PlaylistViewModel
        val songs = fake<SongRepository> { name, args ->
            when (name) {
                "insertSong" -> flowOf(1L)
                "getSongsByListVideoId" -> flowOf((args!![0] as List<*>).map { song(it as String) })
                "getContinueTrack" -> {
                    requiresMembership = args!![4] as Boolean
                    flowOf(arrayListOf(track("later-member")) to null)
                }
                else -> error("Unexpected song operation: $name")
            }
        }
        val local = fake<LocalPlaylistRepository> { name, args ->
            when (name) {
                "getSuggestionsTrackForYouTubePlaylist" -> flowOf(LocalResource.Success(null to listOf(track("new"))))
                "addYouTubePlaylistItem" -> {
                    remoteTracks = (remoteTracks + track(args!![1] as String)).distinctBy { it.videoId }
                    PlaylistMembershipChanges.record(id)
                    flowOf(LocalResource.Success("Added"))
                }
                else -> error("Unexpected local operation: $name")
            }
        }
        val playlists = fake<PlaylistRepository> { name, args ->
            when (name) {
                "getPlaylist" -> flowOf(entity)
                "getPlaylistData" -> {
                    requests++
                    flowOf(Resource.Success(PlaylistBrowse(Author("", "Owner"), "", "", 0, id, "PRIVATE",
                        emptyList(), "FM", remoteTracks.size, remoteTracks, "2026", isEditable = true) to null))
                }
                "insertAndReplacePlaylist" -> { entity = args!![0] as PlaylistEntity; Unit }
                "updatePlaylistInLibrary" -> Unit
                else -> error("Unexpected playlist operation: $name")
            }
        }
        val media = fake<MediaPlayerHandler> { name, _ ->
            when (name) {
                "getNowPlayingState" -> MutableStateFlow(NowPlayingTrackState.initial())
                "getControlState" -> MutableStateFlow(ControlState(false, false, RepeatState.None, false, false, false, false, 1f))
                else -> error("Unexpected player operation: $name")
            }
        }
        init {
            startKoin { modules(module { single<MediaPlayerHandler> { media } }) }
            newModel()
        }
        fun newModel() {
            onMain {
                if (::model.isInitialized) model.viewModelScope.cancel()
                model = PlaylistViewModel(songs, local, playlists)
            }
        }
        fun await(condition: () -> Boolean) {
            val deadline = System.nanoTime() + 4_000_000_000L
            while (!condition() && System.nanoTime() < deadline) { onMain {}; Thread.sleep(5) }
            assertTrue(condition(), "Playlist membership, count and cache must reconcile")
        }
        fun load() { onMain { model.getData(id) }; await { model.tracks.value.size == 1 && entity?.tracks == listOf("song") } }
        override fun close() { onMain { model.viewModelScope.cancel() }; stopKoin() }
    }

    @Test
    fun removingThenAddingFromAnotherScreenRefreshesTheLivePlaylistAndCount() {
        Fixture("PLlive-membership-test").use { f ->
            f.load()
            f.remoteTracks = emptyList()
            PlaylistMembershipChanges.record("VL${f.id}")
            f.await { f.model.tracks.value.isEmpty() && f.model.uiState.value.data?.trackCount == 0 }
            f.remoteTracks = listOf(track("song"))
            PlaylistMembershipChanges.record(f.id)
            f.await { f.model.tracks.value.map { it.videoId } == listOf("song") &&
                f.model.uiState.value.data?.trackCount == 1 && f.entity?.tracks == listOf("song") }
            assertTrue(f.requests >= 3, "Confirmed writes must bypass the loaded-page early return")
        }
    }

    @Test
    fun addingASuggestionDuringMembershipRefreshDoesNotDoubleTheDisplayedCount() {
        Fixture("PLsuggestion-membership-test").use { f ->
            f.load()
            onMain { f.model.getSuggestions(f.id) }
            f.await { f.model.suggestions.value.hasLoaded }
            onMain { f.model.addSuggestedTrackToPlaylist(f.id, track("new")) }
            f.await { f.model.tracks.value.map { it.videoId }.toSet() == setOf("song", "new") &&
                f.model.uiState.value.data?.trackCount == 2 }
            assertEquals(2, f.model.uiState.value.data?.trackCount)
            assertTrue(f.model.suggestions.value.tracks.none { it.videoId == "new" })
        }
    }

    @Test
    fun reenteringAfterAWriteDoesNotRestoreTheRecentEmptySnapshot() {
        Fixture("PLcached-membership-test").use { f ->
            f.load()
            f.remoteTracks = emptyList()
            PlaylistMembershipChanges.record(f.id)
            f.await { f.model.tracks.value.isEmpty() && f.model.uiState.value.data?.trackCount == 0 }
            onMain { f.model.viewModelScope.cancel() }
            f.remoteTracks = listOf(track("song"))
            PlaylistMembershipChanges.record("VL${f.id}")
            f.newModel()
            onMain { f.model.getData(f.id) }
            f.await { f.model.tracks.value.map { it.videoId } == listOf("song") && f.model.uiState.value.data?.trackCount == 1 }
        }
    }

    @Test
    fun onlineRefreshRepairsAPreviouslyContaminatedTrackListAndCount() {
        Fixture("PLdirty-cache-membership-test").use { f ->
            f.entity = PlaylistBrowse(Author("", "Owner"), "", "", 0, f.id, "PRIVATE", emptyList(), "FM", 9,
                listOf(track("song"), track("recommendation")), "2026").toPlaylistEntity()
            f.load()
            f.await { f.model.uiState.value.data?.trackCount == 1 && f.entity?.trackCount == 1 }
            assertEquals(listOf("song"), f.model.tracks.value.map { it.videoId })
            assertEquals(listOf("song"), f.entity?.tracks)
        }
    }

    @Test
    fun ownedPlaylistPaginationRequestsVerifiedMembersAndKeepsItsCountCurrent() {
        Fixture("PLowned-pagination-test").use { f ->
            f.load()
            assertEquals(true, f.model.uiState.value.data?.isEditable)
            onMain { f.model.getContinuationTrack(f.id, "member-page") }
            f.await { f.model.tracks.value.size == 2 }
            assertEquals(true, f.requiresMembership)
            assertEquals(2, f.model.uiState.value.data?.trackCount)
        }
    }

    private companion object {
        fun onMain(action: () -> Unit) { if (SwingUtilities.isEventDispatchThread()) action() else SwingUtilities.invokeAndWait(action) }
        inline fun <reified T> fake(noinline handler: (String, Array<out Any?>?) -> Any?): T =
            Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
                when (method.name) {
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> proxy === args?.firstOrNull()
                    "toString" -> "Test${T::class.java.simpleName}"
                    else -> handler(method.name, args)
                }
            } as T
        fun song(id: String) = SongEntity(videoId = id, title = id, duration = "3:00", durationSeconds = 180,
            artistId = listOf("artist"), artistName = listOf("Artist"), isAvailable = true, isExplicit = false,
            likeStatus = "INDIFFERENT", videoType = "MUSIC_VIDEO_TYPE_ATV", category = "Song", resultType = "Song")
        fun track(id: String) = song(id).toTrack()
    }
}
