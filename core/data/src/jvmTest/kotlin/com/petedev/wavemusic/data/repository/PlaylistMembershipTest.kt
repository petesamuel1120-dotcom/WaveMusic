package com.petedev.wavemusic.data.repository

import com.petedev.wavemusic.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.*

class PlaylistMembershipTest {
    @Test
    fun confirmedDuplicateStopsAtTheFirstMatchingPage() = runBlocking<Unit> {
        var requests = 0
        val message = rejectedPlaylistAddMessage("song") {
            requests++
            Result.success(PlaylistMembershipPage(listOf("song"), "next"))
        }
        assertEquals(PLAYLIST_ITEM_ALREADY_PRESENT, message)
        assertEquals(1, requests)
    }

    @Test
    fun duplicatesOnLaterPagesAreRecognizedWithoutLoadingTheRest() = runBlocking<Unit> {
        val requests = mutableListOf<String?>()
        val message = rejectedPlaylistAddMessage("song") { token ->
            requests += token
            Result.success(if (token == null) PlaylistMembershipPage(listOf("other"), "next")
                else PlaylistMembershipPage(listOf("song"), "unused"))
        }
        assertEquals(PLAYLIST_ITEM_ALREADY_PRESENT, message)
        assertEquals(listOf(null, "next"), requests)
    }

    @Test
    fun missingMembershipNetworkErrorsAndUnknownLayoutsRemainFailures() = runBlocking<Unit> {
        for (page in listOf(
            Result.success(PlaylistMembershipPage(listOf("other"), null)),
            Result.success(null),
            Result.failure<PlaylistMembershipPage?>(IllegalStateException("Authentication/network failure")),
        )) {
            assertEquals("FAILED", rejectedPlaylistAddMessage("song") { page })
        }
    }

    @Test
    fun repeatedContinuationTokensAndLongPlaylistsHaveABoundedLookup() = runBlocking<Unit> {
        var repeated = 0
        assertEquals("FAILED", rejectedPlaylistAddMessage("song") {
            repeated++
            Result.success(PlaylistMembershipPage(emptyList(), "repeat"))
        })
        assertEquals(2, repeated)
        var pages = 0
        assertEquals("FAILED", rejectedPlaylistAddMessage("song") {
            pages++
            Result.success(PlaylistMembershipPage(emptyList(), pages.toString()))
        })
        assertEquals(100, pages)
    }

    @Test
    fun cancellationPropagatesInsteadOfShowingAnErrorToast() = runBlocking<Unit> {
        assertFailsWith<CancellationException> {
            rejectedPlaylistAddMessage("song") { Result.failure(CancellationException("Closed")) }
        }
    }

    @Test
    fun actualPlaylistShelvesInBothLayoutsProduceMembershipIds() {
        val shelf = """{"musicPlaylistShelfRenderer":{"playlistId":"PLtarget","collapsedItemCount":0,"contents":[$row]}}"""
        val layouts = listOf(
            """"contents":{"singleColumnBrowseResultsRenderer":{"tabs":[{"tabRenderer":{"content":{"sectionListRenderer":{"contents":[$shelf]}}}}]}}""",
            """"contents":{"twoColumnBrowseResultsRenderer":{"secondaryContents":{"sectionListRenderer":{"contents":[$shelf]}}}}""",
        )
        for (layout in layouts) {
            assertEquals(listOf("song"), assertNotNull(playlistMembershipPage(response(layout))).videoIds)
        }
    }

    @Test
    fun continuationPlaylistRowsAreReadAndSuggestionRowsAreExcluded() {
        val page = response(""""continuationContents":{"musicPlaylistShelfContinuation":{"contents":[$row]}}""")
        assertEquals(listOf("song"), assertNotNull(playlistMembershipPage(page)).videoIds)
        val suggestions = response(""""contents":{"twoColumnBrowseResultsRenderer":{"secondaryContents":{"sectionListRenderer":{"contents":[{"musicShelfRenderer":{"title":{"runs":[]},"contents":[$row]}}]}}}}""")
        assertNull(playlistMembershipPage(suggestions), "Suggested songs are not proof of playlist membership")
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        const val row = """{"musicResponsiveListItemRenderer":{"flexColumns":[],"playlistItemData":{"videoId":"song","playlistSetVideoId":"set-song"}}}"""
        fun response(contents: String): BrowseResponse = json.decodeFromString("{$contents,\"responseContext\":{}}")
    }
}
