package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics

/** Keeps short search results independent of the artwork and the playlist's scroll position. */
@Composable
internal fun FilteredPlaylistList(
    state: LazyListState,
    searchState: LazyListState,
    filtering: Boolean,
    completedQuery: String,
    contentPadding: PaddingValues,
    searchPadding: PaddingValues,
    modifier: Modifier = Modifier,
    header: LazyListScope.() -> Unit,
    rows: LazyListScope.(filtered: Boolean) -> Unit,
) {
    LaunchedEffect(filtering, completedQuery) {
        if (filtering) searchState.scrollToItem(0)
    }
    Box(modifier) {
        // Keep the original list mounted so clearing search restores the exact position.
        // Its rows stay unfiltered, preventing LazyColumn from clamping back into the hero.
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize().then(
                if (filtering) Modifier.alpha(0f).clearAndSetSemantics {} else Modifier,
            ),
        ) {
            header()
            rows(false)
        }
        if (filtering) {
            LazyColumn(
                state = searchState,
                contentPadding = searchPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                rows(true)
            }
        }
    }
}
