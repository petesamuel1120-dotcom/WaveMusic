package com.maxrave.kotlinytmusicscraper.models.body

import com.maxrave.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class DeletePlaylistBody(
    val context: Context,
    val playlistId: String,
)
