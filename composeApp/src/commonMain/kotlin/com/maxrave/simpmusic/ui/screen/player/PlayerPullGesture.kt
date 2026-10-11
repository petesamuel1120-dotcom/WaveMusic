package com.maxrave.simpmusic.ui.screen.player

internal const val MINI_PLAYER_VERTICAL_DRAG_GAIN = 1.4f

// The mini-player opens after its amplified offset crosses 5 px. Use the corresponding
// finger travel for both directions, after the drag detector has passed touch slop.
internal const val PLAYER_VERTICAL_PULL_DISTANCE_PX = 5f / MINI_PLAYER_VERTICAL_DRAG_GAIN

internal fun playerPullCrossedThreshold(distancePx: Float): Boolean =
    distancePx > PLAYER_VERTICAL_PULL_DISTANCE_PX
