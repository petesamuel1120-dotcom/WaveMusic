package com.petedev.wavemusic.expect

import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.ui.mini_player.MiniPlayerManager

actual fun toggleMiniPlayer() {
    Logger.d("MiniPlayer", "Toggle called, current state: ${MiniPlayerManager.isOpen}")
    MiniPlayerManager.isOpen = !MiniPlayerManager.isOpen
    Logger.d("MiniPlayer", "New state: ${MiniPlayerManager.isOpen}")
}
