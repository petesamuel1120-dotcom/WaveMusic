package com.petedev.wavemusic.data.parser

import com.petedev.wavemusic.common.CHART_SUPPORTED_COUNTRY
import com.petedev.wavemusic.domain.utils.audioChartTitle
import com.petedev.wavemusic.domain.utils.isAudioChartPlaylist
import com.petedev.wavemusic.domain.utils.isConvertedGenreVideoChart
import com.petedev.wavemusic.kotlinytmusicscraper.models.SectionListRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChartParserTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun section(region: String): SectionListRenderer {
        val text = checkNotNull(javaClass.getResource("/charts/chart-$region.json")).readText()
        return checkNotNull(json.decodeFromString<BrowseResponse>(text).contents
            ?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer)
    }

    @Test fun usPodcastShelfDoesNotDiscardTheMusicChartsOrGenres() {
        val chart = assertNotNull(parseChart(section("US")))
        assertEquals(listOf("Video charts", "Genres"), chart.listChartItem.map { it.title })
        assertEquals(2, chart.listChartItem.first().playlists.size)
        assertEquals(8, chart.listChartItem.last().playlists.size)
        assertEquals(2, chart.artists.itemArtists.size)
        assertTrue(chart.listChartItem.flatMap { it.playlists }.all { it.id.startsWith("VL") })
    }

    @Test fun usShowsDailyMusicVideosOnTheLeftAndWeeklyVideosOnTheRight() {
        val chart = assertNotNull(parseChart(section("US"), "US"))
        val pair = chart.listChartItem.first().playlists
        assertEquals(listOf("Daily Top Music Videos - United States", "Top 100 Music Videos United States"), pair.map { it.title })
        assertEquals("VLPL4fGSI1pDJn61unMfmrUSz68RT8IFFnks", pair.first().id)
        assertEquals(2, pair.size)
        assertEquals("Genres", chart.listChartItem.last().title)
        assertEquals(8, chart.listChartItem.last().playlists.size)
        assertTrue(chart.listChartItem.last().playlists.all { it.title.contains("Music Videos") })
        assertTrue(chart.listChartItem.flatMap { it.playlists }.none { it.title.startsWith("Top 100 Songs") })
    }

    @Test fun globalShowsWeeklyAndDailyVideoChartPlaylists() {
        val chart = assertNotNull(parseChart(section("ZZ"), "ZZ"))
        assertEquals(1, chart.listChartItem.size)
        val pair = chart.listChartItem.single().playlists
        assertEquals("VLPL4fGSI1pDJn6t3TXLGiiJdD-sZbrG3tG0", pair.first().id)
        assertEquals("VLPL4fGSI1pDJn5kI81J1fYWK5eZRl1zJ5kM", pair.last().id)
    }

    @Test fun genreVideoChartsBypassAudioConversionAndRefreshOldSongCaches() {
        val genres = assertNotNull(parseChart(section("US"), "US")).listChartItem.last().playlists
        genres.forEach { playlist ->
            assertFalse(isAudioChartPlaylist(playlist.title, playlist.author))
            assertFalse(isConvertedGenreVideoChart(playlist.title, playlist.author))
            assertTrue(isConvertedGenreVideoChart(audioChartTitle(playlist.title), playlist.author))
        }
        assertTrue(isAudioChartPlaylist("Top 100 Songs United States", "YouTube Music"))
        assertFalse(isConvertedGenreVideoChart("Top 100 Songs United States", "YouTube Music"))
    }

    @Test fun everyCountryInThePickerHasItsOwnWeeklyVideoFallback() {
        val ids = mutableSetOf<String>()
        assertEquals(69, CHART_SUPPORTED_COUNTRY.items.size)
        CHART_SUPPORTED_COUNTRY.items.forEachIndexed { index, country ->
            val expectedName = if (country == "CZ") "Czechia" else CHART_SUPPORTED_COUNTRY.itemsData[index]
            val shelf = assertNotNull(parseChart(null, country), country).listChartItem.single()
            assertEquals("Video charts", shelf.title)
            val pair = shelf.playlists
            assertEquals(listOf("Top 100 Music Videos $expectedName"), pair.map { it.title }, country)
            pair.forEach { playlist ->
                assertTrue(ids.add(playlist.id), "Chart must belong to its country: ${playlist.id}")
                assertTrue(playlist.thumbnails.single().url.startsWith("https://"), country)
            }
        }
        assertEquals(69, ids.size)
    }

    @Test fun missingCarouselDataKeepsTheKnownWeeklyVideoChartAvailable() {
        val chart = assertNotNull(parseChart(null, "us"))
        assertEquals(1, chart.listChartItem.single().playlists.size)
        assertTrue(chart.artists.itemArtists.isEmpty())
        assertNull(parseChart(null, "XX"))
        assertNull(parseChart(null))
    }

    @Test fun oneMalformedPlaylistDoesNotDiscardItsValidNeighbours() {
        val section = section("ZZ")
        val first = checkNotNull(section.contents?.first()?.musicCarouselShelfRenderer)
        val malformed = first.contents.first().copy(musicTwoRowItemRenderer = null)
        val mixed = first.copy(contents = listOf(malformed) + first.contents)
        val data = section.copy(contents = listOf(section.contents!!.first().copy(musicCarouselShelfRenderer = mixed)))
        assertEquals(2, assertNotNull(parseChart(data)).listChartItem.single().playlists.size)
    }
}
