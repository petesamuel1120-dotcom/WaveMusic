package com.petedev.wavemusic.ui.screen.player

/** A single finger sequence can expand and return without ever dismissing playback. */
internal class MiniPlayerPullGesture(private val travelPx: Float) {
    var distancePx = 0f
        private set
    var hasExpanded = false
        private set

    fun drag(deltaPx: Float): Float {
        distancePx = (distancePx + deltaPx).coerceIn(-travelPx.coerceAtLeast(1f), Float.MAX_VALUE)
        if (distancePx < 0f) hasExpanded = true
        if (hasExpanded) distancePx = distancePx.coerceAtMost(0f)
        return if (distancePx >= 0f) 0f else
            (-distancePx * MINI_PLAYER_VERTICAL_DRAG_GAIN / travelPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
    }

    fun shouldExpand() = hasExpanded && playerPullCrossedThreshold(-distancePx)

    fun shouldDismissPlayback(thresholdPx: Float) = !hasExpanded && distancePx > thresholdPx
}
