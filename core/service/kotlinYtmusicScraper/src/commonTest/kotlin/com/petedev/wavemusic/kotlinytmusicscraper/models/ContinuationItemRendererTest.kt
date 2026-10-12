package com.petedev.wavemusic.kotlinytmusicscraper.models

import com.petedev.wavemusic.kotlinytmusicscraper.models.youtube.data.ContinuationItemRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.youtube.data.YouTubeDataPage
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ContinuationItemRendererTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun directContinuationStillDecodes() {
        val item = json.decodeFromString<ContinuationItemRenderer>(
            """{"trigger":"CONTINUATION_TRIGGER_ON_ITEM_SHOWN","continuationEndpoint":{
                "clickTrackingParams":"tracking","continuationCommand":{
                    "token":"direct-next-page","request":"CONTINUATION_REQUEST_TYPE_BROWSE"}}}""",
        )
        assertEquals("direct-next-page", item.continuationEndpoint?.continuationCommand?.token)
        assertEquals("CONTINUATION_TRIGGER_ON_ITEM_SHOWN", item.trigger)
        assertEquals(item, json.decodeFromString<ContinuationItemRenderer>(json.encodeToString(item)))
    }

    @Test
    fun wrappedCommandsKeepBrowseAndWatchTokens() {
        val item = json.decodeFromString<ContinuationItemRenderer>(
            """{"continuationEndpoint":{"commandExecutorCommand":{"commands":[
                {"voteCommand":{"vote":"LIKE"}},
                {"continuationCommand":{"token":"watch-token","request":"CONTINUATION_REQUEST_TYPE_WATCH"}},
                {"continuationCommand":{"token":"browse-next-page","request":"CONTINUATION_REQUEST_TYPE_BROWSE"}}
            ]}}}""",
        )
        val commands = item.continuationEndpoint?.commandExecutorCommand?.commands.orEmpty()
        assertEquals(3, commands.size)
        assertEquals(null, commands[0].continuationCommand)
        assertEquals("watch-token", commands[1].continuationCommand?.token)
        assertEquals("browse-next-page", commands[2].continuationCommand?.token)
        assertEquals(item, json.decodeFromString<ContinuationItemRenderer>(json.encodeToString(item)))
    }

    @Test
    fun watchPageStillUsesTheSharedContinuationModel() {
        val page = json.decodeFromString<YouTubeDataPage>(
            """{"contents":{"twoColumnWatchNextResults":{"results":{"results":{"contents":[
                {"itemSectionRenderer":{"contents":[{"continuationItemRenderer":{
                    "continuationEndpoint":{"continuationCommand":{"token":"watch-next-page"}}
                }}]}}
            ]}}}}}""",
        )
        val item = page.contents?.twoColumnWatchNextResults?.results?.results?.content
            ?.single()?.itemSectionRenderer?.contents?.single()?.continuationItemRenderer
        assertEquals("watch-next-page", item?.continuationEndpoint?.continuationCommand?.token)
    }
}
