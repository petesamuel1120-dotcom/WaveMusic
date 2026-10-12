package com.petedev.wavemusic.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import com.petedev.wavemusic.common.DB_NAME
import com.petedev.wavemusic.data.io.getHomeFolderPath
import java.io.File

actual fun getDatabaseBuilder(
    converters: Converters
): RoomDatabase.Builder<MusicDatabase> {
    return Room.databaseBuilder<MusicDatabase>(
        name = getDatabasePath()
    ).addTypeConverter(converters)
}

actual fun getDatabasePath(): String {
    val dbFile = File(getHomeFolderPath(listOf(".simpmusic", "db")), DB_NAME)
    return dbFile.absolutePath
}