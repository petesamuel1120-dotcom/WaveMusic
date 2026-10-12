package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Keep the recently passed viewport composed when the user reverses a fling. The
// window is bounded by viewport size, rather than retaining the whole feed.
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun rememberPageScrollCache(): LazyLayoutCacheWindow = remember {
    LazyLayoutCacheWindow(aheadFraction = 0.5f, behindFraction = 1f)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberPageListState(): LazyListState =
    rememberLazyListState(cacheWindow = rememberPageScrollCache())

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberPageGridState(): LazyGridState =
    rememberLazyGridState(cacheWindow = rememberPageScrollCache())
