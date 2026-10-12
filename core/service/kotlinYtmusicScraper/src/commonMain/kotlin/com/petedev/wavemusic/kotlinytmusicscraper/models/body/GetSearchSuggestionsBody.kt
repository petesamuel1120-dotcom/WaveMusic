package com.petedev.wavemusic.kotlinytmusicscraper.models.body

import com.petedev.wavemusic.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)