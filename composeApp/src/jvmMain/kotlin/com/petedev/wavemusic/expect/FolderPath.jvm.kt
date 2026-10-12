package com.petedev.wavemusic.expect

actual fun getDownloadFolderPath(): String = System.getProperty("user.home") + "/Downloads"