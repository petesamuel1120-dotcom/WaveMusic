package com.petedev.wavemusic.ui.component

import com.petedev.wavemusic.ui.component.selection.MAX_SONG_SELECTION
import com.petedev.wavemusic.ui.component.selection.SongSelectionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SongSelectionStateTest {
    private val songs = (1..100).map { "song-$it" }

    @Test
    fun bulkFromSongTenSelectsTenThroughThirtyFour() {
        val state = SongSelectionState("Limit reached")
        state.start("song-10")
        state.toggleSelectAll(songs)
        assertEquals((10..34).map { "song-$it" }, state.selected)
        assertEquals(MAX_SONG_SELECTION, state.count)
    }

    @Test
    fun favoritesFollowTheDisplayedOrderTowardOlderSongs() {
        val newestFirst = songs.asReversed()
        val state = SongSelectionState("Limit reached")
        state.start("song-75")
        state.toggleSelectAll(newestFirst)
        assertEquals((75 downTo 51).map { "song-$it" }, state.selected)
    }

    @Test
    fun nearTheEndSelectsOnlyRemainingSongsWithoutWrapping() {
        val state = SongSelectionState("Limit reached")
        state.start("song-95")
        state.toggleSelectAll(songs)
        assertEquals((95..100).map { "song-$it" }, state.selected)
    }

    @Test
    fun repeatedBulkPressesClearAndRestoreTheSameBatch() {
        val state = SongSelectionState("Limit reached")
        state.start("song-40")
        state.toggleSelectAll(songs)
        val batch = state.selected.toList()
        state.toggleSelectAll(songs)
        assertTrue(state.selected.isEmpty())
        assertTrue(state.isActive)
        state.toggleSelectAll(songs)
        assertEquals(batch, state.selected)
    }

    @Test
    fun aNewManualPickAfterClearingStartsANewBatch() {
        val state = SongSelectionState("Limit reached")
        state.start("song-10")
        state.toggleSelectAll(songs)
        state.toggleSelectAll(songs)
        state.toggle("song-50")
        state.toggleSelectAll(songs)
        assertEquals((50..74).map { "song-$it" }, state.selected)
    }

    @Test
    fun filteredAndReorderedRowsDetermineTheRange() {
        val filtered = songs.filterIndexed { index, _ -> index % 2 == 0 }.asReversed()
        val state = SongSelectionState("Limit reached")
        state.start("song-61")
        state.toggleSelectAll(filtered)
        assertEquals(filtered.dropWhile { it != "song-61" }.take(MAX_SONG_SELECTION), state.selected)
    }

    @Test
    fun blankAndDuplicateIdsDoNotConsumeTheBatchLimit() {
        val state = SongSelectionState("Limit reached")
        state.start("song-10")
        state.toggleSelectAll(songs.flatMap { listOf(it, "", it) })
        assertEquals((10..34).map { "song-$it" }, state.selected)
    }
}
