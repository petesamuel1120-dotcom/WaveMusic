package com.petedev.wavemusic.data.dataStore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.petedev.wavemusic.domain.manager.DataStoreManager
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class HideStatusBarPreferenceTest {
    @Test fun statusBarDefaultsVisibleAndBothChoicesSurviveRestart() = runBlocking {
        val file = File.createTempFile("hide-status-bar-", ".preferences_pb")
        var job = SupervisorJob()
        fun manager() = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
        ))
        try {
            val first = manager()
            assertEquals(DataStoreManager.FALSE, first.hideStatusBar.first())
            first.setHideStatusBar(true)
            assertEquals(DataStoreManager.TRUE, first.hideStatusBar.first())
            job.cancelAndJoin()
            job = SupervisorJob()
            val reopened = manager()
            assertEquals(DataStoreManager.TRUE, reopened.hideStatusBar.first())
            reopened.setHideStatusBar(false)
            job.cancelAndJoin()
            job = SupervisorJob()
            assertEquals(DataStoreManager.FALSE, manager().hideStatusBar.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }

    @Test fun statusBarAndReducedVisualEffectsAreIndependentChoices() = runBlocking {
        val file = File.createTempFile("status-bar-effects-", ".preferences_pb")
        val job = SupervisorJob()
        try {
            val store = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
            ))
            store.setReduceVisualEffects(true)
            assertEquals(DataStoreManager.FALSE, store.hideStatusBar.first())
            store.setHideStatusBar(true)
            store.setReduceVisualEffects(false)
            assertEquals(DataStoreManager.TRUE, store.hideStatusBar.first())
            store.setReduceVisualEffects(true)
            store.setHideStatusBar(false)
            assertEquals(DataStoreManager.TRUE, store.reduceVisualEffects.first())
            assertEquals(DataStoreManager.FALSE, store.hideStatusBar.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }
}
