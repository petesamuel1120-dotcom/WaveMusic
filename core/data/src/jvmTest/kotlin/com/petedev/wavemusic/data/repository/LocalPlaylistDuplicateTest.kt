package com.petedev.wavemusic.data.repository

import DatabaseDao
import com.petedev.wavemusic.data.db.MusicDatabase
import com.petedev.wavemusic.data.db.datasource.LocalDataSource
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.utils.LocalResource
import com.petedev.wavemusic.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT
import com.petedev.wavemusic.kotlinytmusicscraper.YouTube
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Proxy
import kotlin.test.*

class LocalPlaylistDuplicateTest {
    @Test
    fun duplicateIsReportedBeforeAnyDatabaseWritesAndNewTracksStillAddNormally() = runBlocking<Unit> {
        for (alreadyPresent in listOf(true, false)) {
            var writes = 0
            var tracks = if (alreadyPresent) listOf("song") else emptyList()
            val song = SongEntity(videoId = "song", title = "Song", duration = "3:00", durationSeconds = 180,
                isAvailable = true, isExplicit = false, likeStatus = "INDIFFERENT", videoType = "MUSIC_VIDEO_TYPE_ATV",
                category = "Song", resultType = "Song")
            val dao = Proxy.newProxyInstance(DatabaseDao::class.java.classLoader, arrayOf(DatabaseDao::class.java)) { _, method, args ->
                when (method.name) {
                    "getLocalPlaylist" -> LocalPlaylistEntity(id = 1, title = "Playlist", tracks = tracks)
                    "getSong" -> song
                    "insertPairSongLocalPlaylist" -> { writes++; Unit }
                    "updateLocalPlaylistTracks" -> {
                        @Suppress("UNCHECKED_CAST")
                        tracks = args!![0] as List<String>
                        writes++
                        Unit
                    }
                    else -> error("Unexpected database operation: ${method.name}")
                }
            } as DatabaseDao
            // Only the fake DAO is used; the generated database is never opened.
            val database = Class.forName("com.petedev.wavemusic.data.db.MusicDatabase_Impl").getDeclaredConstructor().newInstance() as MusicDatabase
            val repository = LocalPlaylistRepositoryImpl(LocalDataSource(dao, database), YouTube())
            val result = repository.addTrackToLocalPlaylist(1, song, "Added", "Synced", "Error").toList().last()
            if (alreadyPresent) {
                assertEquals(PLAYLIST_ITEM_ALREADY_PRESENT, assertIs<LocalResource.Error<String>>(result).message)
                assertEquals(0, writes)
            } else {
                assertEquals("Added", assertIs<LocalResource.Success<String>>(result).data)
                assertEquals(2, writes)
                assertEquals(listOf("song"), tracks)
            }
        }
    }
}
