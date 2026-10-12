package com.petedev.wavemusic.ui.component

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import com.kmpalette.palette.graphics.Palette
import com.petedev.wavemusic.data.dataStore.prefersReducedVisualEffects
import com.petedev.wavemusic.utils.ByteBudgetCache

/** Keeps recently opened playlist covers ready while their screens are recreated. */
internal object PlaylistArtworkCache {
    private const val MAX_ENTRIES = 8

    private val entries = ByteBudgetCache<String, CachedPlaylistArtwork>(
        maxEntries = MAX_ENTRIES,
        maxBytes = (if (prefersReducedVisualEffects()) 8L else 24L) * 1024L * 1024L,
    ) { artwork ->
        val bitmap = artwork.bitmap
        bitmap.width.toLong() * bitmap.height * if (bitmap.config == ImageBitmapConfig.F16) 8L else 4L
    }

    fun get(thumbnail: String?): CachedPlaylistArtwork? {
        if (thumbnail == null) return null
        return entries[thumbnail]
    }

    fun touch(thumbnail: String?) {
        if (thumbnail == null) return
        entries[thumbnail]
    }

    fun put(
        thumbnail: String,
        bitmap: ImageBitmap,
        palette: Palette,
    ) {
        entries[thumbnail] = CachedPlaylistArtwork(bitmap, palette)
    }
}

internal data class CachedPlaylistArtwork(
    val bitmap: ImageBitmap,
    val palette: Palette,
)
