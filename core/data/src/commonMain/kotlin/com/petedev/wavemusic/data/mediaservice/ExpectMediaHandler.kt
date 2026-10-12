package com.petedev.wavemusic.data.mediaservice

import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler
import com.petedev.wavemusic.domain.repository.AnalyticsRepository
import com.petedev.wavemusic.domain.repository.LocalPlaylistRepository
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

expect fun createMediaServiceHandler(
    dataStoreManager: DataStoreManager,
    songRepository: SongRepository,
    streamRepository: StreamRepository,
    localPlaylistRepository: LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: CoroutineScope,
): MediaPlayerHandler