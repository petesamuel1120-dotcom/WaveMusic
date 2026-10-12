package com.petedev.wavemusic.data.io

import com.petedev.wavemusic.data.db.documentDirectory
import okio.FileSystem

actual fun fileSystem(): FileSystem = FileSystem.SYSTEM
actual fun fileDir(): String = documentDirectory() + "/SimpMusic"