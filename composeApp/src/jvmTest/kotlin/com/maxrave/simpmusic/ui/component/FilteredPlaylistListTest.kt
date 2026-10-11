@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FilteredPlaylistListTest {
    @Test
    fun narrowingFavoritesToD12KeepsResultsVisibleInPortrait() = exercise(450, 900, 112, 42)

    @Test
    fun narrowingFavoritesToD12KeepsResultsVisibleAboveTheKeyboard() = exercise(450, 500, 112, 42)

    @Test
    fun narrowingFavoritesToD12KeepsResultsVisibleInLandscape() = exercise(900, 300, 48, 42)

    @Test
    fun searchingFromTheArtworkThenClearingRestoresTheArtwork() = exercise(450, 900, 112, 0)

    private fun exercise(width: Int, height: Int, topPadding: Int, originalIndex: Int) {
        val query = mutableStateOf("")
        val completedQuery = mutableStateOf("")
        val songs = listOf("D12 Purple Pills", "D19") + (2 until 346).map { "Download $it" }
        val results = mutableStateOf(emptyList<String>())
        var playlistState: LazyListState? = null
        var resultsState: LazyListState? = null
        var scope: CoroutineScope? = null
        var firstResultPosition: Offset? = null
        var time = 0L
        val scene = ImageComposeScene(width, height) {
            val currentQuery by query
            val currentResults by results
            val normal = rememberLazyListState()
            val search = rememberLazyListState()
            playlistState = normal
            resultsState = search
            scope = rememberCoroutineScope()
            FilteredPlaylistList(
                state = normal,
                searchState = search,
                filtering = currentQuery.isNotBlank(),
                completedQuery = completedQuery.value,
                contentPadding = PaddingValues(),
                searchPadding = PaddingValues(top = topPadding.dp),
                modifier = Modifier.fillMaxSize().background(Color.Black),
                header = {
                    item(key = "artwork") {
                        Box(Modifier.fillMaxWidth().height(420.dp).background(Color.Red))
                    }
                },
                rows = { filtered ->
                    val rows = if (filtered) currentResults else songs
                    items(rows, key = { it }) { song ->
                        Box(Modifier.fillMaxWidth().height(64.dp)
                            .background(if (filtered) Color.Green else Color.Blue)
                            .onGloballyPositioned {
                                if (filtered && song == rows.firstOrNull()) {
                                    firstResultPosition = it.positionInWindow()
                                }
                            }) {
                            BasicText(song)
                        }
                    }
                    item(key = "end") { Spacer(Modifier.height(24.dp)) }
                },
            )
        }
        fun frame(checkResult: Boolean = false) {
            time += 16_666_667L
            val image = scene.render(time)
            try {
                if (checkResult) {
                    val pixels = image.toComposeImageBitmap().toPixelMap()
                    assertEquals(Color.Green, pixels[width - 20, topPadding + 30],
                        "A matching track must stay beneath the search field instead of revealing artwork")
                }
            } finally {
                image.close()
            }
        }
        try {
            repeat(3) { frame() }
            val original = assertNotNull(playlistState)
            val search = assertNotNull(resultsState)
            assertNotNull(scope).launch { original.scrollToItem(originalIndex, if (originalIndex > 0) 17 else 0) }
            repeat(3) { frame() }
            val savedIndex = original.firstVisibleItemIndex
            val savedOffset = original.firstVisibleItemScrollOffset
            for (term in listOf("d", "d1", "d12")) {
                query.value = term
                // Filtering is asynchronous in the screen. Check the pending frame as well.
                frame()
                results.value = songs.filter { it.contains(term, ignoreCase = true) }
                completedQuery.value = term
                repeat(4) { frame(checkResult = true) }
                assertEquals(0, search.firstVisibleItemIndex)
                assertEquals(0, search.firstVisibleItemScrollOffset)
                assertEquals(topPadding.toFloat(), assertNotNull(firstResultPosition).y)
                assertTrue(topPadding + 64 < height)
                assertEquals(savedIndex, original.firstVisibleItemIndex)
                assertEquals(savedOffset, original.firstVisibleItemScrollOffset)
                if (term == "d") {
                    assertNotNull(scope).launch { search.scrollToItem(35, 12) }
                    repeat(3) { frame() }
                }
            }
            query.value = "no matching song"
            results.value = emptyList()
            completedQuery.value = query.value
            repeat(3) { frame() }
            query.value = ""
            completedQuery.value = ""
            repeat(3) { frame() }
            assertEquals(savedIndex, original.firstVisibleItemIndex, "Clearing search restores the playlist")
            assertEquals(savedOffset, original.firstVisibleItemScrollOffset)
        } finally {
            scene.close()
        }
    }
}
