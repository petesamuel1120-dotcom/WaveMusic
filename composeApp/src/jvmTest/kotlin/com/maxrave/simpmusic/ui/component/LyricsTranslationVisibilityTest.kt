package com.maxrave.simpmusic.ui.component

import com.maxrave.domain.data.model.metadata.Lyrics
import com.maxrave.domain.data.model.metadata.Line
import com.maxrave.simpmusic.viewModel.LyricsProvider
import com.maxrave.simpmusic.viewModel.NowPlayingScreenData
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class LyricsTranslationVisibilityTest {
    private val original = Lyrics(lines = listOf(Line("1000", "0", words = "Hello")), syncType = "LINE_SYNCED")
    private val translated = Lyrics(lines = listOf(Line("1000", "0", words = "Bonjour")), syncType = "LINE_SYNCED")
    private val available = NowPlayingScreenData.initial().copy(
        currentVideoId = "song-a",
        lyricsVideoId = "song-a",
        lyricsData = NowPlayingScreenData.LyricsData(original, translated to LyricsProvider.AI, LyricsProvider.SIMPMUSIC),
    )

    @Test
    fun availableOrCachedTranslationDoesNotEnableIt() {
        val displayed = available.withLyricsTranslation(false)
        assertNull(displayed.lyricsData?.translatedLyrics)
        assertSame(original, displayed.lyricsData?.lyrics)
        assertSame(translated, available.lyricsData?.translatedLyrics?.first)
    }

    @Test
    fun enabledChoiceShowsTheCurrentSongTranslation() {
        assertSame(available, available.withLyricsTranslation(true))
    }

    @Test
    fun lateTranslationArrivalCannotUndoAnOffChoice() {
        val loading = available.copy(lyricsData = available.lyricsData?.copy(translatedLyrics = null))
        assertNull(loading.withLyricsTranslation(false).lyricsData?.translatedLyrics)
        val completed = loading.copy(lyricsData = available.lyricsData)
        assertNull(completed.withLyricsTranslation(false).lyricsData?.translatedLyrics)
    }

    @Test
    fun switchingTracksDoesNotApplyTheNewSongChoiceToOldLyrics() {
        val switching = available.copy(currentVideoId = "song-b")
        assertNull(switching.withLyricsTranslation(true).lyricsData?.translatedLyrics)
        assertNull(available.copy(currentVideoId = null).withLyricsTranslation(true).lyricsData?.translatedLyrics)
    }
}
