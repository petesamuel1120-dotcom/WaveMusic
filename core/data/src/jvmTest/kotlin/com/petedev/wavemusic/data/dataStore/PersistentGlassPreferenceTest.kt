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

class PersistentGlassPreferenceTest {
    @Test
    fun headerButtonsPersistIndependentlyOfBackdropAndIntensity() = runBlocking {
        val file = File.createTempFile("persistent-header-buttons-", ".preferences_pb")
        var job = SupervisorJob()
        fun manager() = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
        ))
        try {
            val first = manager()
            assertEquals(DataStoreManager.FALSE, first.persistentHeaderButtons.first())
            first.setLiquidGlassIntensity(0.73f)
            first.setPersistentLiquidGlassHeader(true)
            first.setPersistentHeaderButtons(true)
            assertEquals(DataStoreManager.TRUE, first.persistentHeaderButtons.first())
            job.cancelAndJoin()
            job = SupervisorJob()
            val reopened = manager()
            assertEquals(DataStoreManager.TRUE, reopened.persistentHeaderButtons.first())
            reopened.setPersistentLiquidGlassHeader(false)
            assertEquals(DataStoreManager.TRUE, reopened.persistentHeaderButtons.first())
            reopened.setPersistentHeaderButtons(false)
            assertEquals(DataStoreManager.FALSE, reopened.persistentHeaderButtons.first())
            assertEquals(DataStoreManager.FALSE, reopened.persistentLiquidGlassHeader.first())
            assertEquals(0.73f, reopened.liquidGlassIntensity.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }

    @Test
    fun topGlassDefaultsOffAndSurvivesReopeningWithoutChangingIntensity() = runBlocking {
        val file = File.createTempFile("persistent-top-glass-", ".preferences_pb")
        var job = SupervisorJob()
        fun manager() = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
        ))
        try {
            val first = manager()
            assertEquals(DataStoreManager.FALSE, first.persistentLiquidGlassHeader.first())
            first.setLiquidGlassIntensity(0.93f)
            first.setPersistentLiquidGlassHeader(true)
            assertEquals(DataStoreManager.TRUE, first.persistentLiquidGlassHeader.first())
            assertEquals(0.93f, first.liquidGlassIntensity.first())
            job.cancelAndJoin()
            job = SupervisorJob()
            val reopened = manager()
            assertEquals(DataStoreManager.TRUE, reopened.persistentLiquidGlassHeader.first())
            assertEquals(0.93f, reopened.liquidGlassIntensity.first())
            reopened.setPersistentLiquidGlassHeader(false)
            assertEquals(DataStoreManager.FALSE, reopened.persistentLiquidGlassHeader.first())
            assertEquals(0.93f, reopened.liquidGlassIntensity.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }
}
