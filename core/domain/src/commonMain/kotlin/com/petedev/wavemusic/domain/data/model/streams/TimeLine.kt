package com.petedev.wavemusic.domain.data.model.streams

data class TimeLine(
    val current: Long,
    val total: Long,
    val bufferedPercent: Int,
    val loading: Boolean = true,
    val isCrossfading: Boolean = false,
) {
    val canSeek: Boolean
        get() = total > 0L

    /** A safe 0..1 playback fraction; unknown or out-of-range timestamps never fill the bar. */
    val currentProgressFraction: Float
        get() {
            val duration = total.takeIf { it > 0L } ?: return 0f
            return (current.coerceIn(0L, duration).toDouble() / duration.toDouble())
                .toFloat()
                .coerceIn(0f, 1f)
        }

    val bufferedProgressFraction: Float
        get() = bufferedPercent.coerceIn(0, 100) / 100f
}
