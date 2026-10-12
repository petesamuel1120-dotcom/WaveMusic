package com.petedev.wavemusic.kotlinytmusicscraper.pages

import com.petedev.wavemusic.kotlinytmusicscraper.models.AlbumItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.BrowseEndpoint
import com.petedev.wavemusic.kotlinytmusicscraper.models.VideoItem

data class ExplorePage(
    val released: List<AlbumItem>,
    val musicVideo: List<VideoItem>,
    /** Where the album shelf's "More" button leads. That shelf only comes back to a signed-in session. */
    val releasedMoreEndpoint: BrowseEndpoint? = null,
    /** Where the music-video shelf's "More" button leads: the full list, `FEmusic_new_releases_videos`. */
    val musicVideoMoreEndpoint: BrowseEndpoint? = null,
)