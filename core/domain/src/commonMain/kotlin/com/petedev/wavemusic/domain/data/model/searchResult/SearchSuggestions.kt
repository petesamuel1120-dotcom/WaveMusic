package com.petedev.wavemusic.domain.data.model.searchResult

import com.petedev.wavemusic.domain.data.type.SearchResultType

data class SearchSuggestions(
    val queries: List<String>,
    val recommendedItems: List<SearchResultType>,
)