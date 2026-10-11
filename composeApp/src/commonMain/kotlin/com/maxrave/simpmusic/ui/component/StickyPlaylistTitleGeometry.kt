package com.maxrave.simpmusic.ui.component

internal data class StickyTitlePosition(val left: Float, val top: Float, val width: Float, val progress: Float)

internal fun stickyPlaylistTitlePosition(
    sourceTop: Float, targetTop: Float, sourceCenter: Float, targetCenter: Float,
    sourceWidth: Float, targetWidth: Float, transitionDistance: Float,
    sourceStartTop: Float? = null,
): StickyTitlePosition {
    // Fit the handoff into the actual gap above the title. In a short landscape hero,
    // a fixed distance can already overlap the starting position before any scroll.
    val distance = minOf(transitionDistance, sourceStartTop?.minus(targetTop) ?: transitionDistance)
        .coerceAtLeast(1f)
    val fraction = ((targetTop + distance - sourceTop) / distance).coerceIn(0f, 1f)
    val progress = fraction * fraction * (3f - 2f * fraction)
    val width = sourceWidth + (targetWidth - sourceWidth) * progress
    val center = sourceCenter + (targetCenter - sourceCenter) * progress
    return StickyTitlePosition(center - width / 2f, maxOf(sourceTop, targetTop), width, progress)
}
