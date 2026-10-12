package com.petedev.wavemusic.data.parser

import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LibraryGridContinuationTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test fun terminalGridWithoutItemsDoesNotAbortThePlaylistPicker() {
        val response = json.decodeFromString<BrowseResponse>(
            """{"responseContext":{},"continuationContents":{"gridContinuation":{}}}""",
        )
        assertTrue(assertNotNull(response.continuationContents?.gridContinuation).items.isEmpty())
    }

    @Test fun realGridItemsAndNextTokensArePreserved() {
        val response = json.decodeFromString<BrowseResponse>(
            """{"responseContext":{},"continuationContents":{"gridContinuation":{
                "items":[{}],"continuations":[{"nextContinuationData":{"continuation":"next"}}]
            }}}""",
        )
        val grid = assertNotNull(response.continuationContents?.gridContinuation)
        assertEquals(1, grid.items.size)
        assertEquals("next", grid.continuations?.single()?.nextContinuationData?.continuation)
    }
}
