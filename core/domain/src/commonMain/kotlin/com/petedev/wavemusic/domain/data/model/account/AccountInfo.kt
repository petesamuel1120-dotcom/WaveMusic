package com.petedev.wavemusic.domain.data.model.account

import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail

data class AccountInfo(
    val name: String,
    val email: String,
    val pageId: String? = null,
    val thumbnails: List<Thumbnail>,
    val authUser: Int = 0,
)