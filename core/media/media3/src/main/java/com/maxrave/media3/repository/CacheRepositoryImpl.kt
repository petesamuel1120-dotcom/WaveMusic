package com.maxrave.media3.repository

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import com.maxrave.common.Config
import com.maxrave.domain.repository.CacheRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@UnstableApi
internal class CacheRepositoryImpl : CacheRepository, KoinComponent {
    // A cache index can contain a large amount of metadata. Resolve only the cache requested by
    // the operation instead of opening player, download, and canvas indexes just to read one.
    private val playerCache: SimpleCache by inject(named(Config.PLAYER_CACHE))
    private val downloadCache: SimpleCache by inject(named(Config.DOWNLOAD_CACHE))
    private val canvasCache: SimpleCache by inject(named(Config.CANVAS_CACHE))

    override suspend fun getCacheSize(cacheName: String): Long =
        when (cacheName) {
            Config.PLAYER_CACHE -> {
                playerCache.cacheSpace
            }
            Config.DOWNLOAD_CACHE -> {
                downloadCache.cacheSpace
            }
            Config.CANVAS_CACHE -> {
                canvasCache.cacheSpace
            }
            else -> 0L
        }

    override fun clearCache(cacheName: String) {
        when (cacheName) {
            Config.PLAYER_CACHE -> {
                playerCache.keys.forEach { key ->
                    playerCache.removeResource(key)
                }
            }
            Config.DOWNLOAD_CACHE -> {
                downloadCache.keys.forEach { key ->
                    downloadCache.removeResource(key)
                }
            }
            Config.CANVAS_CACHE -> {
                canvasCache.keys.forEach { key ->
                    canvasCache.removeResource(key)
                }
            }
            else -> { /* no-op */ }
        }
    }

    override suspend fun getAllCacheKeys(cacheName: String): List<String> =
        when (cacheName) {
            Config.PLAYER_CACHE -> {
                playerCache.keys.toList()
            }
            Config.DOWNLOAD_CACHE -> {
                downloadCache.keys.toList()
            }
            Config.CANVAS_CACHE -> {
                canvasCache.keys.toList()
            }
            else -> emptyList()
        }
}
