package com.petedev.wavemusic.kotlinytmusicscraper.models.body

import com.petedev.wavemusic.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)