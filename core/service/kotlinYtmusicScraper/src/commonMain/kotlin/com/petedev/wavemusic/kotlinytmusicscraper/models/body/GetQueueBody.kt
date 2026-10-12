package com.petedev.wavemusic.kotlinytmusicscraper.models.body

import com.petedev.wavemusic.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetQueueBody(
    val context: Context,
    val videoIds: List<String>?,
    val playlistId: String?,
)