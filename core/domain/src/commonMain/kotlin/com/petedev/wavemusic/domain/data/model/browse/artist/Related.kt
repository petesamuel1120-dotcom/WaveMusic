package com.petedev.wavemusic.domain.data.model.browse.artist

data class Related(
    val browseId: Any,
    val results: List<ResultRelated>,
)