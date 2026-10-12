package com.petedev.wavemusic.data.io

/** iOS has no playlist-cover picker wired up yet, so there is nothing to read. */
actual suspend fun readLocalImageBytes(uri: String): ByteArray? = null
