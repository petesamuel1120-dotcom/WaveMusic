package com.petedev.wavemusic.data.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.petedev.wavemusic.common.SETTINGS_FILENAME
import com.petedev.wavemusic.data.io.getHomeFolderPath
import createDataStore
import java.io.File

actual fun prefersReducedVisualEffects(): Boolean = false

actual fun createDataStoreInstance(): DataStore<Preferences> = createDataStore(
    producePath = {
        val file = File(getHomeFolderPath(listOf(".simpmusic")), "$SETTINGS_FILENAME.preferences_pb")
        file.absolutePath
    }
)
