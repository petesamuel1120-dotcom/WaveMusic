package com.maxrave.data.parser

import com.maxrave.kotlinytmusicscraper.models.response.BrowseResponse
import com.maxrave.kotlinytmusicscraper.parser.playlistMemberPage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.*

class FavoriteContinuationRegressionTest {
    @Test fun likedMusicWithOpaqueAppendTargetsLoadsAll360Tracks() = runBlocking<Unit> {
        val requested = mutableListOf<String>()
        val playlist = assertNotNull(parseFullPlaylistMemberBrowse(initial(100, "page-100"), "LM", "views") { next ->
            requested += next
            when (next) {
                "page-100" -> append(100, 100, "page-200", targetId = "a0b1c2d3-shelf-instance")
                "page-200" -> append(200, 100, "page-300", wrapped = true, targetId = "e4f5g6h7-shelf-instance")
                "page-300" -> append(300, 60, null, targetId = "i8j9k0l1-shelf-instance")
                else -> error("Unexpected continuation: $next")
            }
        })
        assertEquals(360, playlist.tracks.size)
        assertEquals(listOf("page-100", "page-200", "page-300"), requested)
        assertEquals((0 until 360).map { "song-$it" }, playlist.tracks.map { it.videoId })
    }

    @Test fun wrappedBrowseCommandsFetchAll251LikesInsteadOfStoppingAt100() = runBlocking<Unit> {
        val requested = mutableListOf<String>()
        val playlist = assertNotNull(parseFullPlaylistMemberBrowse(initial(100, "next-100", wrapped = true), "LM", "views") { next ->
            requested += next
            when (next) {
                "next-100" -> append(100, 100, "next-200", wrapped = true)
                "next-200" -> append(200, 51, null)
                else -> error("Unexpected continuation: $next")
            }
        })
        assertEquals(251, playlist.tracks.size)
        assertEquals(listOf("next-100", "next-200"), requested)
    }

    @Test fun wrappedBrowseTokenIsSelectedInsteadOfTheOtherCommandTokens() {
        assertEquals("browse-next", initial(1, "browse-next", wrapped = true)
            .playlistMemberPage(allowLikedMusicShelves = true).continuation)
        assertEquals("browse-next", append(100, 1, "browse-next", wrapped = true)
            .playlistMemberPage(allowLikedMusicShelves = true).continuation)
    }

    @Test fun watchOnlyWrappedCommandsAreNotUsedAsBrowseContinuation() {
        val response = response(""""onResponseReceivedActions":[{"appendContinuationItemsAction":{
            "targetId":"music-playlist-shelf", "continuationItems":[${row(100)},
                {"continuationItemRenderer":{"continuationEndpoint":{"commandExecutorCommand":{"commands":[
                    {"continuationCommand":{"request":"CONTINUATION_REQUEST_TYPE_WATCH","token":"watch-only"}}
                ]}}}}
            ]}}]""")
        assertNull(response.playlistMemberPage(allowLikedMusicShelves = true).continuation)
    }

    @Test fun favoritesFetchAll251TracksAcrossMixedContinuationFormats() = runBlocking<Unit> {
        val requested = mutableListOf<String>()
        val playlist = assertNotNull(parseFullPlaylistMemberBrowse(initial(100, "next-100"), "LM", "views") { token ->
            requested += token
            when (token) {
                "next-100" -> shelf(100, 100, "next-200")
                "next-200" -> append(200, 51, null)
                else -> error("Unexpected continuation: $token")
            }
        })
        assertEquals(listOf("next-100", "next-200"), requested)
        assertEquals((0 until 251).map { "song-$it" }, playlist.tracks.map { it.videoId })
        assertEquals(251, playlist.trackCount)
    }

    @Test fun explicitFavoritesContinuationWinsOverTheFirstPageDisplayedCount() {
        assertEquals("next", assertNotNull(parsePlaylistMemberBrowse(initial(100, "next"), "LM", "views")).second)
        assertEquals("next", assertNotNull(parsePlaylistMemberBrowse(initial(100, "next"), "VLLM", "views")).second)
    }

    @Test fun genericInitialLikedMusicShelfIsAccepted() {
        val page = assertNotNull(parsePlaylistMemberBrowse(initial(2, "next", generic = true), "LM", "views"))
        assertEquals(2, page.first.tracks.size)
        assertEquals("next", page.second)
    }

    @Test fun sectionLevelLikedMusicContinuationIsKept() {
        val page = initial(100, null, sectionToken = "section-next").playlistMemberPage(allowLikedMusicShelves = true)
        assertEquals("section-next", page.continuation)
    }

    @Test fun ordinaryAndEditablePlaylistsStillRejectGenericRecommendationShelves() {
        for (response in listOf(shelf(100, 100, "next"), append(100, 100, "next"), append(100, 100, "next", targetId = "opaque-shelf-instance"))) {
            val ordinary = response.playlistMemberPage()
            assertTrue(ordinary.rows.isEmpty())
            assertFalse(ordinary.hasPlaylistSource)
            assertNull(ordinary.continuation)
            assertTrue(response.playlistMemberPage(requireMembership = true, allowLikedMusicShelves = true).rows.isEmpty())
        }
    }

    @Test fun unrecognizedLikedMusicPageCannotBeMistakenForCompletedSync() = runBlocking<Unit> {
        assertFailsWith<IllegalStateException> {
            parseFullPlaylistMemberBrowse(initial(100, "next"), "LM", "views") { response("") }
        }
    }

    @Test fun emptyFavoritesAreAValidCompletedCollection() = runBlocking<Unit> {
        val playlist = assertNotNull(parseFullPlaylistMemberBrowse(initial(0, null), "LM", "views") {
            error("An empty favorites collection must not fetch another page")
        })
        assertTrue(playlist.tracks.isEmpty())
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        fun response(fields: String): BrowseResponse = json.decodeFromString("""{"responseContext":{}${if (fields.isEmpty()) "" else "," + fields}}""")
        fun row(index: Int) = """{"musicResponsiveListItemRenderer":{
            "playlistItemData":{"videoId":"song-$index"},
            "flexColumns":[
                {"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"Song $index","navigationEndpoint":{"watchEndpoint":{"videoId":"song-$index"}}}]}}},
                {"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"Artist","navigationEndpoint":{"browseEndpoint":{"browseId":"artist"}}}]}}}
            ],"navigationEndpoint":{"watchEndpoint":{"videoId":"song-$index"}}}}"""
        fun rows(start: Int, count: Int) = (start until start + count).map(::row)
        fun token(value: String, wrapped: Boolean = false): String {
            val command = if (wrapped) """"commandExecutorCommand":{"commands":[
                {"playlistVotingRefreshPopupCommand":{}},
                {"continuationCommand":{"request":"CONTINUATION_REQUEST_TYPE_WATCH","token":"not-a-browse-token"}},
                {"continuationCommand":{"request":"CONTINUATION_REQUEST_TYPE_BROWSE","token":"$value"}}
            ]}""" else """"continuationCommand":{"token":"$value"}"""
            return """{"continuationItemRenderer":{"continuationEndpoint":{$command}}}"""
        }
        fun initial(count: Int, next: String?, generic: Boolean = false, sectionToken: String? = null, wrapped: Boolean = false): BrowseResponse {
            val items = (rows(0, count) + listOfNotNull(next?.let { token(it, wrapped) })).joinToString(",")
            val shelf = if (generic) "musicShelfRenderer" else "musicPlaylistShelfRenderer"
            val sectionContinuation = sectionToken?.let { """, "continuations":[{"nextContinuationData":{"continuation":"$it"}}]""" }.orEmpty()
            return response(""""header":{"musicDetailHeaderRenderer":{
                "title":{"runs":[{"text":"Liked Music"}]},"subtitle":{"runs":[{"text":"Playlist"},{"text":" • "},{"text":"Owner"}]},
                "secondSubtitle":{"runs":[{"text":"$count songs"}]},"thumbnail":{},"menu":{"menuRenderer":{"items":[]}}}},
                "contents":{"singleColumnBrowseResultsRenderer":{"tabs":[{"tabRenderer":{"content":{"sectionListRenderer":{
                    "contents":[{"$shelf":{"playlistId":"LM","collapsedItemCount":0,"contents":[$items]}}]$sectionContinuation}}}}]}}""")
        }
        fun shelf(start: Int, count: Int, next: String?): BrowseResponse {
            val continuations = next?.let { """, "continuations":[{"nextContinuationData":{"continuation":"$it"}}]""" }.orEmpty()
            return response(""""continuationContents":{"musicShelfContinuation":{"contents":[${rows(start, count).joinToString(",")}]$continuations}}""")
        }
        fun append(start: Int, count: Int, next: String?, wrapped: Boolean = false, targetId: String = "music-shelf"): BrowseResponse {
            val items = (rows(start, count) + listOfNotNull(next?.let { token(it, wrapped) })).joinToString(",")
            return response(""""onResponseReceivedActions":[{"appendContinuationItemsAction":{"targetId":"$targetId","continuationItems":[$items]}}]""")
        }
    }
}
