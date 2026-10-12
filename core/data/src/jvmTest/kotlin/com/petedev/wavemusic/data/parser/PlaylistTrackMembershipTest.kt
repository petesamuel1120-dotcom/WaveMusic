package com.petedev.wavemusic.data.parser

import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicShelfRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import kotlinx.serialization.json.Json
import kotlin.test.*

class PlaylistTrackMembershipTest {
    @Test
    fun explicitZeroTracksDoesNotBecomeTheRecommendationRowCount() {
        val result = assertNotNull(parsePlaylistData(header("0 songs"), listOf(row("suggested", false)), "PLzero", "views"))
        assertEquals(0, result.trackCount)
        assertTrue(result.tracks.isEmpty())
    }

    @Test
    fun mixedRowsCountOnlyRealPlaylistMembers() {
        val result = assertNotNull(parsePlaylistData(header("1 song"),
            listOf(row("member", true), row("suggested", false)), "PLmixed", "views"))
        assertEquals(1, result.trackCount)
        assertEquals(listOf("member"), result.tracks.map { it.videoId })
    }

    @Test
    fun missingCountFallsBackToParsedMembersRatherThanContinuationOrSuggestionRows() {
        val result = assertNotNull(parsePlaylistData(header(""),
            listOf(row("member", true), row("suggested", false)), "PLunknown", "views"))
        assertEquals(1, result.trackCount)
        assertEquals(listOf("member"), result.tracks.map { it.videoId })
    }

    @Test
    fun publicPlaylistRowsAndTotalCountsAcrossPagesRemainSupported() {
        val result = assertNotNull(parsePlaylistData(header("1,200 songs"), listOf(row("public", false)), "PLpublic", "views"))
        assertEquals(1200, result.trackCount)
        assertEquals(listOf("public"), result.tracks.map { it.videoId })
    }

    @Test
    fun editableHeaderWithoutACountRejectsRowsThatOnlyHaveAVideoId() {
        val editable = json.decodeFromString<BrowseResponse.Header.MusicEditablePlaylistDetailHeaderRenderer>("""
            {"header":{"musicDetailHeaderRenderer":{
                "title":{"runs":[{"text":"FM"}]},"subtitle":{"runs":[]},"secondSubtitle":{"runs":[]},
                "thumbnail":{},"menu":{"menuRenderer":{"items":[]}}}}}
        """.trimIndent())
        val placeholder = json.decodeFromString<MusicShelfRenderer.Content>("""
            {"musicResponsiveListItemRenderer":{"playlistItemData":{"videoId":"suggested"},
                "flexColumns":[{"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"Suggested"}]}}}]}}
        """.trimIndent())
        val result = assertNotNull(parsePlaylistData(editable, listOf(placeholder), "PLowned", "views"))
        assertTrue(result.isEditable)
        assertTrue(result.tracks.isEmpty())
        assertEquals(0, result.trackCount)
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        fun header(count: String): BrowseResponse.Header.MusicDetailHeaderRenderer = json.decodeFromString("""
            {"title":{"runs":[{"text":"FM"}]},"subtitle":{"runs":[{"text":"Playlist"},{"text":" • "},{"text":"Owner"}]},
             "secondSubtitle":{"runs":[{"text":"$count"}]},"thumbnail":{},"menu":{"menuRenderer":{"items":[]}}}
        """.trimIndent())
        fun row(id: String, member: Boolean): MusicShelfRenderer.Content {
            val membership = if (member) "\"playlistItemData\":{\"videoId\":\"$id\",\"playlistSetVideoId\":\"set-$id\"}," else ""
            return json.decodeFromString("""{"musicResponsiveListItemRenderer":{$membership
                "flexColumns":[{"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"$id","navigationEndpoint":{"watchEndpoint":{"videoId":"$id"}}}]}}}],
                "navigationEndpoint":{"watchEndpoint":{"videoId":"$id"}}}}""")
        }
    }
}
