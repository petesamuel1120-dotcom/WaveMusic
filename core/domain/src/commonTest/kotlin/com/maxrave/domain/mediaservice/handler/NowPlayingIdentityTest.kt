package com.maxrave.domain.mediaservice.handler

import com.maxrave.common.MERGING_DATA_TYPE
import com.maxrave.domain.data.player.GenericMediaItem
import com.maxrave.domain.data.player.GenericMediaMetadata
import com.maxrave.domain.extension.activeVideoId
import com.maxrave.domain.extension.toSongEntity
import com.maxrave.domain.extension.withMediaItem
import com.maxrave.domain.extension.withSongForMediaItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class NowPlayingIdentityTest {
    private fun item(id: String) = GenericMediaItem.EMPTY.copy(
        mediaId = MERGING_DATA_TYPE.VIDEO + id,
        metadata = GenericMediaMetadata.EMPTY.copy(title = "Title $id", artist = "Artist $id"),
    )
    private val a = item("a")
    private val b = item("b")
    private val songA = a.toSongEntity().copy(videoId = "a")
    private val songB = b.toSongEntity().copy(videoId = "b")
    private val stateA = NowPlayingTrackState(a, null, songA)

    @Test
    fun nextIdentityDoesNotCarryTheOutgoingTitleOrArtist() {
        val next = stateA.withMediaItem(b, null)
        assertNull(next.songEntity)
        assertEquals("b", next.activeVideoId())
        assertEquals("Title b", next.mediaItem.metadata.title)
        assertEquals("Artist b", next.mediaItem.metadata.artist)
    }

    @Test
    fun sameItemMetadataRefreshRetainsItsSavedSong() {
        val refreshed = stateA.withMediaItem(a, null)
        assertSame(songA, refreshed.songEntity)
        assertEquals(stateA, refreshed)
    }

    @Test
    fun outgoingDatabaseEmissionCannotRevertTheIncomingSong() {
        val next = stateA.withMediaItem(b, null).withSongForMediaItem(b.mediaId, songB)
        assertSame(next, next.withSongForMediaItem(a.mediaId, songA))
        assertEquals("b", next.activeVideoId())
        assertEquals("Title b", next.songEntity?.title)
    }

    @Test
    fun evenAMislabeledResultCannotAttachToAnotherItem() {
        val next = stateA.withMediaItem(b, null)
        assertSame(next, next.withSongForMediaItem(b.mediaId, songA))
        assertEquals(songB, next.withSongForMediaItem(b.mediaId, songB).songEntity)
    }

    @Test
    fun returningToThePreviousItemPreservesItsCompleteValidSnapshot() {
        val next = stateA.withMediaItem(b, null).withSongForMediaItem(b.mediaId, songB)
        val previous = next.withMediaItem(a, null).withSongForMediaItem(a.mediaId, songA)
        assertEquals(stateA, previous)
    }
}
