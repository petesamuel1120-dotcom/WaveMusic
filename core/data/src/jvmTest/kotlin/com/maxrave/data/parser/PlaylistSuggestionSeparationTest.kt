package com.maxrave.data.parser

import com.maxrave.kotlinytmusicscraper.models.response.BrowseResponse
import com.maxrave.kotlinytmusicscraper.parser.fromPlaylistContinuationToTracks
import com.maxrave.kotlinytmusicscraper.parser.getPlaylistContinuation
import com.maxrave.kotlinytmusicscraper.parser.playlistMemberPage
import com.maxrave.kotlinytmusicscraper.parser.getReloadParams
import com.maxrave.kotlinytmusicscraper.parser.getSuggestionSongItems
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.*

class PlaylistSuggestionSeparationTest {
    @Test
    fun emptyOwnedPlaylistDoesNotFollowTheSuggestionsContinuationOrCountItsRows() = runBlocking<Unit> {
        val response = initial("0 songs", emptyList(), null, listOf(row("recommendation", false)), "suggestions-next")
        assertEquals("suggestions-next", response.getPlaylistContinuation(), "The separate Suggestions loader still needs its token")
        var calls = 0
        val playlist = assertNotNull(parseFullPlaylistMemberBrowse(response, "PLtarget", "views") {
            calls++
            error("An empty owned playlist must not page through recommendations")
        })
        assertTrue(playlist.isEditable)
        assertTrue(playlist.tracks.isEmpty())
        assertEquals(0, playlist.trackCount)
        assertEquals(0, calls)
    }

    @Test
    fun ownedRowsWithOnlyVideoIdsAreNotMembersEvenWhenTheCountIsMissing() {
        val response = initial("", listOf(row("placeholder", false, placeholder = true)), null)
        val playlist = assertNotNull(parsePlaylistMemberBrowse(response, "PLtarget", "views")).first
        assertTrue(playlist.isEditable)
        assertTrue(playlist.tracks.isEmpty())
        assertEquals(0, playlist.trackCount)
    }

    @Test
    fun actualMemberTokenWinsOverARecommendationTokenInTheSameResponse() {
        val response = initial("3 songs", listOf(row("member", true)), "members-next",
            listOf(row("recommendation", false)), "suggestions-next")
        assertEquals("suggestions-next", response.getPlaylistContinuation(), "Reproduce the old generic-token path")
        val (playlist, token) = assertNotNull(parsePlaylistMemberBrowse(response, "PLtarget", "views"))
        assertEquals(listOf("member"), playlist.tracks.map { it.videoId })
        assertEquals("members-next", token)
    }

    @Test
    fun mixedAppendActionsKeepOnlyMembersAndTheirOwnContinuation() {
        val response = append(listOf(row("member", true), row("placeholder", false, true)), "members-next",
            listOf(row("recommendation", false)), "suggestions-next")
        assertEquals("suggestions-next", response.getPlaylistContinuation())
        val page = response.playlistMemberPage(requireMembership = true)
        assertEquals(listOf("member"), page.rows.map { it.videoId })
        assertEquals("members-next", page.continuation)
    }

    @Test
    fun ownedContinuationRejectsUnmarkedRowsEvenIfTheActionTargetsAPlaylist() {
        val response = append(listOf(row("placeholder", false, true)), "suggestions-next")
        val page = response.playlistMemberPage(requireMembership = true)
        assertTrue(page.rows.isEmpty())
        assertNull(page.continuation)
    }

    @Test
    fun genericRecommendationShelvesDoNotBecomePlaylistContinuationRows() {
        val response = response(""""continuationContents":{"sectionListContinuation":{"contents":[
            {"musicShelfRenderer":{"title":{"runs":[{"text":"Suggestions"}]},"contents":[${row("suggested", false)}],
             "continuations":[{"reloadContinuationData":{"continuation":"reload-suggestions"}}]}}]}}""")
        assertTrue(response.playlistMemberPage().rows.isEmpty())
        assertNull(response.playlistMemberPage().continuation)
        assertTrue(response.fromPlaylistContinuationToTracks().isEmpty())
    }

    @Test
    fun fullPlaylistCountIsNotInflatedByContinuationRowsOrDuplicateRows() = runBlocking<Unit> {
        val initial = initial("4 songs", listOf(row("one", true), row("two", true)), "members-next")
        val requested = mutableListOf<String>()
        val result = assertNotNull(parseFullPlaylistMemberBrowse(initial, "PLtarget", "views") { token ->
            requested += token
            append(listOf(row("two", true), row("three", true), row("four", true)), null,
                listOf(row("recommendation", false)), "suggestions-next")
        })
        assertEquals(listOf("members-next"), requested)
        assertEquals(listOf("one", "two", "three", "four"), result.tracks.map { it.videoId })
        assertEquals(4, result.trackCount)
    }

    @Test
    fun publicPlaylistRowsWithoutEditTokensStillWorkInDedicatedPlaylistShelves() = runBlocking<Unit> {
        val result = assertNotNull(parseFullPlaylistMemberBrowse(
            initial("2 songs", listOf(row("one", false)), "members-next", editable = false), "PLpublic", "views",
        ) {
            response(""""continuationContents":{"musicPlaylistShelfContinuation":{"contents":[${row("two", false)}]}}""")
        })
        assertFalse(result.isEditable)
        assertEquals(listOf("one", "two"), result.tracks.map { it.videoId })
        assertEquals(2, result.trackCount)
    }

    @Test
    fun failedFullPlaylistPageDoesNotReturnAPartialSnapshotAsSuccess() = runBlocking<Unit> {
        assertFailsWith<IllegalStateException> {
            parseFullPlaylistMemberBrowse(initial("2 songs", listOf(row("one", true)), "next"), "PLtarget", "views") {
                error("Network failed")
            }
        }
    }

    @Test
    fun suggestionsInTheInitialResponseRemainAvailableOnlyInTheSuggestionsFeed() {
        val response = response(""""contents":{"twoColumnBrowseResultsRenderer":{"secondaryContents":{"sectionListRenderer":{"contents":[
            {"musicPlaylistShelfRenderer":{"playlistId":"PLtarget","collapsedItemCount":0,"contents":[]}},
            {"musicShelfRenderer":{"contents":[${row("suggested", false)}],
                "continuations":[{"reloadContinuationData":{"continuation":"reload-suggestions"}}]}}]}}}}""")
        assertTrue(response.playlistMemberPage().rows.isEmpty())
        assertNull(response.playlistMemberPage().continuation)
        assertEquals("reload-suggestions", response.getReloadParams())
        assertEquals(listOf("suggested"), response.getSuggestionSongItems().map { it.id })
    }

    @Test
    fun directSuggestionContinuationsAreKeptSeparateFromMembers() {
        val response = response(""""continuationContents":{"musicShelfContinuation":{"contents":[${row("suggested", false)}],
            "continuations":[{"reloadContinuationData":{"continuation":"reload-suggestions"}}]}}""")
        assertTrue(response.playlistMemberPage().rows.isEmpty())
        assertEquals("reload-suggestions", response.getReloadParams())
        assertEquals(listOf("suggested"), response.getSuggestionSongItems().map { it.id })
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        fun response(body: String): BrowseResponse = json.decodeFromString("{$body,\"responseContext\":{}}")
        fun row(id: String, member: Boolean, placeholder: Boolean = false): String {
            val membership = when {
                member -> "\"playlistItemData\":{\"videoId\":\"$id\",\"playlistSetVideoId\":\"set-$id\"},"
                placeholder -> "\"playlistItemData\":{\"videoId\":\"$id\"},"
                else -> ""
            }
            return """{"musicResponsiveListItemRenderer":{$membership
                "flexColumns":[{"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"$id","navigationEndpoint":{"watchEndpoint":{"videoId":"$id"}}}]}}},
                    {"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"Artist"}]}}}],
                "navigationEndpoint":{"watchEndpoint":{"videoId":"$id"}}}}"""
        }
        fun token(value: String) = """{"continuationItemRenderer":{"continuationEndpoint":{"continuationCommand":{"token":"$value"}}}}"""
        fun initial(
            count: String, members: List<String>, next: String?, suggestions: List<String> = emptyList(),
            suggestionToken: String? = null, editable: Boolean = true,
        ): BrowseResponse {
            val detail = """{"title":{"runs":[{"text":"My playlist"}]},"subtitle":{"runs":[{"text":"Playlist"},{"text":" • "},{"text":"Owner"}]},
                "secondSubtitle":{"runs":[{"text":"$count"}]},"thumbnail":{},"menu":{"menuRenderer":{"items":[]}}}"""
            val header = if (editable) """{"musicEditablePlaylistDetailHeaderRenderer":{"header":{"musicDetailHeaderRenderer":$detail}}}"""
                else """{"musicDetailHeaderRenderer":$detail}"""
            val contents = (members + listOfNotNull(next?.let(::token))).joinToString(",")
            val suggestionShelf = if (suggestions.isEmpty()) "" else """,
                {"musicShelfRenderer":{"title":{"runs":[{"text":"Suggestions"}]},"contents":[${suggestions.joinToString(",")}]}}"""
            val sectionToken = suggestionToken?.let { """, "continuations":[{"nextContinuationData":{"continuation":"$it"}}]""" }.orEmpty()
            return response(""""header":$header,"contents":{"singleColumnBrowseResultsRenderer":{"tabs":[{"tabRenderer":{"content":{"sectionListRenderer":{
                "contents":[{"musicPlaylistShelfRenderer":{"playlistId":"PLtarget","collapsedItemCount":0,"contents":[$contents]}}$suggestionShelf]$sectionToken}}}}]}}""")
        }
        fun append(members: List<String>, next: String?, suggestions: List<String> = emptyList(), suggestionToken: String? = null): BrowseResponse {
            val items = (members + listOfNotNull(next?.let(::token))).joinToString(",")
            val extra = if (suggestions.isEmpty()) "" else """,{"appendContinuationItemsAction":{"targetId":"music-shelf",
                "continuationItems":[${(suggestions + listOfNotNull(suggestionToken?.let(::token))).joinToString(",")}]}}"""
            return response(""""onResponseReceivedActions":[{"appendContinuationItemsAction":{"targetId":"music-playlist-shelf","continuationItems":[$items]}}$extra]""")
        }
    }
}
