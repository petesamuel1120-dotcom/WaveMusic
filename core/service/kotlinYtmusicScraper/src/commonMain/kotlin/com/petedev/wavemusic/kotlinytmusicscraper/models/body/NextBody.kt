package com.petedev.wavemusic.kotlinytmusicscraper.models.body

import com.petedev.wavemusic.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class NextBody(
    val context: Context,
    val videoId: String?,
    val playlistId: String?,
    val playlistSetVideoId: String?,
    val index: Int?,
    val params: String?,
    val continuation: String?,
)