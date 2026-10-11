package com.maxrave.data.dataStore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LyricsTranslationPreferenceTest {
    @Test
    fun newSongsStayOffEvenWhenTheTranslationButtonIsEnabled() = runBlocking {
        Fixture().use { fixture ->
            val manager = fixture.manager()
            manager.setShowLyricsTranslationButton(true)
            assertFalse(manager.getSongLyricsTranslationEnabled("new-song").first())
            manager.setSongLyricsTranslationEnabled("enabled-song", true)
            assertTrue(manager.getSongLyricsTranslationEnabled("enabled-song").first())
            assertFalse(manager.getSongLyricsTranslationEnabled("another-new-song").first())
            manager.setSongLyricsTranslationEnabled("", true)
            assertFalse(manager.getSongLyricsTranslationEnabled("").first())
        }
    }

    @Test
    fun explicitOnAndOffChoicesSurviveAppRestartIndependentlyForEachSong() = runBlocking {
        Fixture().use { fixture ->
            var manager = fixture.manager()
            manager.setSongLyricsTranslationEnabled("song-a", true)
            manager.setSongLyricsTranslationEnabled("song-b", true)
            manager.setSongLyricsTranslationEnabled("song-b", false)
            fixture.restart()
            manager = fixture.manager()
            assertTrue(manager.getSongLyricsTranslationEnabled("song-a").first())
            assertFalse(manager.getSongLyricsTranslationEnabled("song-b").first())
            assertFalse(manager.getSongLyricsTranslationEnabled("new-song").first())
            manager.setSongLyricsTranslationEnabled("song-a", false)
            fixture.restart()
            manager = fixture.manager()
            assertFalse(manager.getSongLyricsTranslationEnabled("song-a").first())
            assertFalse(manager.getSongLyricsTranslationEnabled("song-b").first())
        }
    }

    private class Fixture : AutoCloseable {
        private val directory = createTempDirectory("lyrics-translation-pref-test-").toFile()
        private val file = directory.resolve("settings.preferences_pb")
        private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var activeManager: DataStoreManagerImpl? = null

        fun manager(): DataStoreManagerImpl = activeManager ?: DataStoreManagerImpl(
            PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { file.absolutePath.toPath() }),
        ).also { activeManager = it }

        suspend fun restart() {
            scope.coroutineContext[Job]!!.cancelAndJoin()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            activeManager = null
        }

        override fun close() = runBlocking {
            scope.coroutineContext[Job]!!.cancelAndJoin()
            file.delete()
            directory.delete()
            Unit
        }
    }
}
