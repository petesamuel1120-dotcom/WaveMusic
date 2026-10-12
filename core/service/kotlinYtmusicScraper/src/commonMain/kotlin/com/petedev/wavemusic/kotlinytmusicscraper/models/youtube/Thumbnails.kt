package com.petedev.wavemusic.kotlinytmusicscraper.models.youtube

import com.petedev.wavemusic.kotlinytmusicscraper.models.Thumbnail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Thumbnails(
    @SerialName("thumbnails")
    val thumbnails: List<Thumbnail>? = null,
)