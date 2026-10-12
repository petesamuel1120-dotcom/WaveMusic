package com.petedev.wavemusic.data.dataStore

import android.content.Context
import android.app.ActivityManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.petedev.wavemusic.common.SETTINGS_FILENAME
import createDataStore
import org.koin.mp.KoinPlatform.getKoin

private val reducedVisualEffectsDeviceDefault: Boolean by lazy {
    val manager = getKoin().get<Context>().getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (manager == null) {
        false
    } else {
        manager.isLowRamDevice
    }
}

actual fun prefersReducedVisualEffects(): Boolean = reducedVisualEffectsDeviceDefault

actual fun createDataStoreInstance(): DataStore<Preferences> {
    return createDataStore(
        producePath = { getKoin().get<Context>().filesDir.resolve("datastore/$SETTINGS_FILENAME.preferences_pb").absolutePath }
    )
}
