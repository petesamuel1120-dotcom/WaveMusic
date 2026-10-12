package com.petedev.wavemusic.data.dataStore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class FrostedGlassHeaderPreferenceTest {
    @Test fun everyStyleIncludingOffSurvivesRestart() = runBlocking {
        val file = File.createTempFile("header-style-", ".preferences_pb")
        var job = SupervisorJob()
        fun manager() = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
        ))
        try {
            assertEquals(FrostedGlassHeaderStyle.Moderate, manager().frostedGlassHeaderStyle.first())
            job.cancelAndJoin()
            for (style in FrostedGlassHeaderStyle.entries) {
                job = SupervisorJob()
                manager().setFrostedGlassHeaderStyle(style)
                job.cancelAndJoin()
                job = SupervisorJob()
                assertEquals(style, manager().frostedGlassHeaderStyle.first())
                job.cancelAndJoin()
            }
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }

    @Test fun fixedHeaderAndGlassSwitchesDoNotOverwriteTheStyle() = runBlocking {
        val file = File.createTempFile("independent-header-", ".preferences_pb")
        val job = SupervisorJob()
        try {
            val store = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
            ))
            store.setFrostedGlassHeaderStyle(FrostedGlassHeaderStyle.Fuller)
            store.setPersistentHeaderButtons(false)
            assertEquals(FrostedGlassHeaderStyle.Fuller, store.frostedGlassHeaderStyle.first())
            assertEquals(DataStoreManager.FALSE, store.persistentHeaderButtons.first())
            store.setEnableLiquidGlass(false)
            store.setReduceVisualEffects(true)
            assertEquals(FrostedGlassHeaderStyle.Fuller, store.frostedGlassHeaderStyle.first())
            store.setEnableLiquidGlass(true)
            store.setReduceVisualEffects(false)
            assertEquals(FrostedGlassHeaderStyle.Fuller, store.frostedGlassHeaderStyle.first())
            store.setFrostedGlassHeaderStyle(FrostedGlassHeaderStyle.Off)
            assertEquals(DataStoreManager.FALSE, store.persistentHeaderButtons.first())
            assertEquals(DataStoreManager.TRUE, store.enableLiquidGlass.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }

    @Test fun headerAndHorizonPersistIndependently() = runBlocking {
        val file = File.createTempFile("horizon-style-", ".preferences_pb")
        var job = SupervisorJob()
        fun manager() = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
        ))
        try {
            val first = manager()
            assertEquals(FrostedGlassHeaderStyle.Off, first.frostedGlassHorizonStyle.first())
            first.setFrostedGlassHeaderStyle(FrostedGlassHeaderStyle.Balanced)
            first.setFrostedGlassHorizonStyle(FrostedGlassHeaderStyle.Fuller)
            job.cancelAndJoin()
            job = SupervisorJob()
            val restarted = manager()
            assertEquals(FrostedGlassHeaderStyle.Balanced, restarted.frostedGlassHeaderStyle.first())
            assertEquals(FrostedGlassHeaderStyle.Fuller, restarted.frostedGlassHorizonStyle.first())
            restarted.setFrostedGlassHorizonStyle(FrostedGlassHeaderStyle.Off)
            assertEquals(FrostedGlassHeaderStyle.Balanced, restarted.frostedGlassHeaderStyle.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }

    @Test fun horizonStylesSurviveGlassAndReducedEffectsChanges() = runBlocking {
        val file = File.createTempFile("horizon-effects-", ".preferences_pb")
        val job = SupervisorJob()
        try {
            val store = DataStoreManagerImpl(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
            ))
            for (style in FrostedGlassHeaderStyle.entries) {
                store.setFrostedGlassHorizonStyle(style)
                store.setEnableLiquidGlass(false)
                store.setReduceVisualEffects(true)
                assertEquals(style, store.frostedGlassHorizonStyle.first())
                store.setEnableLiquidGlass(true)
                store.setReduceVisualEffects(false)
                assertEquals(style, store.frostedGlassHorizonStyle.first())
            }
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }

    @Test fun unknownStoredStylesFallBackToTheirDefaults() = runBlocking {
        val file = File.createTempFile("invalid-header-", ".preferences_pb")
        val job = SupervisorJob()
        try {
            val preferences = PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + job), produceFile = { file },
            )
            preferences.edit {
                it[DataStoreManagerImpl.FROSTED_GLASS_HEADER_STYLE] = "unrecognized"
                it[DataStoreManagerImpl.FROSTED_GLASS_HORIZON_STYLE] = "unrecognized"
            }
            val store = DataStoreManagerImpl(preferences)
            assertEquals(FrostedGlassHeaderStyle.Moderate, store.frostedGlassHeaderStyle.first())
            assertEquals(FrostedGlassHeaderStyle.Off, store.frostedGlassHorizonStyle.first())
        } finally {
            job.cancelAndJoin()
            file.delete()
        }
    }
}
