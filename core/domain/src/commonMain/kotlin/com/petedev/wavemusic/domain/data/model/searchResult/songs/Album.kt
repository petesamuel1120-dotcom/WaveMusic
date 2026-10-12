package com.petedev.wavemusic.domain.data.model.searchResult.songs

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val name: String,
)