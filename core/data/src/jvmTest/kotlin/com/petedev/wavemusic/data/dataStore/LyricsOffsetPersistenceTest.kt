package com.petedev.wavemusic.data.dataStore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toPath
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LyricsOffsetPersistenceTest {
    @Test
    fun rapidAdjustmentsPreserveEveryTapAndSurviveReopening() = runBlocking {
        val directory = createTempDirectory("lyrics-offset-test-").toFile()
        val file = directory.resolve("settings.preferences_pb")
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        fun store() = PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { file.absolutePath.toPath() })
        try {
            val manager = DataStoreManagerImpl(store())
            manager.setLyricsOffsetMs(1234)
            manager.setLyricsProvider("YOUTUBE")
            withTimeout(20_000) {
                coroutineScope {
                    repeat(40) { launch(Dispatchers.Default) { manager.adjustLyricsOffsetMs(500) } }
                }
                assertEquals(21234, manager.lyricsOffsetMs.first())
                coroutineScope {
                    repeat(40) { launch(Dispatchers.Default) { manager.adjustLyricsOffsetMs(-500) } }
                }
            }
            assertEquals(1234, manager.lyricsOffsetMs.first())
            assertEquals("YOUTUBE", manager.lyricsProvider.first())

            // The first local edit starts from the displayed effective value and then stands alone.
            manager.setLyricsOffsetMs(1000)
            manager.adjustSongLyricsOffsetMs("song-a", 500)
            assertEquals(1500, manager.getSongLyricsOffsetMs("song-a").first())
            manager.setLyricsOffsetMs(3000)
            assertEquals(1500, manager.getEffectiveLyricsOffsetMs("song-a").first())
            assertEquals(3000, manager.getEffectiveLyricsOffsetMs("song-b").first())

            // Zero is a real override, not a request to fall back to the global offset.
            manager.adjustSongLyricsOffsetMs("song-a", -1500)
            manager.setLyricsOffsetMs(3500)
            assertEquals(0, manager.getEffectiveLyricsOffsetMs("song-a").first())
            manager.adjustSongLyricsOffsetMs("song-b", -500)
            manager.setLyricsOffsetMs(0) // Settings Default.
            assertEquals(3000, manager.getEffectiveLyricsOffsetMs("song-b").first())
            assertEquals(0, manager.getEffectiveLyricsOffsetMs("song-c").first())

            manager.clearSongLyricsOffsetMs("song-a") // Song Default removes the override.
            assertNull(manager.getSongLyricsOffsetMs("song-a").first())
            manager.setLyricsOffsetMs(2000)
            assertEquals(2000, manager.getEffectiveLyricsOffsetMs("song-a").first())
            assertEquals(3000, manager.getEffectiveLyricsOffsetMs("song-b").first())

            coroutineScope {
                repeat(40) { launch(Dispatchers.Default) { manager.adjustSongLyricsOffsetMs("rapid-song", 500) } }
            }
            assertEquals(22000, manager.getEffectiveLyricsOffsetMs("rapid-song").first())
            assertEquals(2000, manager.lyricsOffsetMs.first())

            scope.coroutineContext[Job]!!.cancelAndJoin()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val reopened = DataStoreManagerImpl(store())
            assertEquals(2000, reopened.lyricsOffsetMs.first())
            assertEquals("YOUTUBE", reopened.lyricsProvider.first())
            assertEquals(3000, reopened.getEffectiveLyricsOffsetMs("song-b").first())
            assertEquals(22000, reopened.getSongLyricsOffsetMs("rapid-song").first())
            reopened.clearSongLyricsOffsetMs("song-b")
            assertEquals(2000, reopened.getEffectiveLyricsOffsetMs("song-b").first())
            reopened.setLyricsOffsetMs(2500)
            assertEquals(2500, reopened.getEffectiveLyricsOffsetMs("song-b").first())

            reopened.setLyricsOffsetMs(Int.MAX_VALUE)
            reopened.adjustLyricsOffsetMs(500)
            assertEquals(Int.MAX_VALUE, reopened.lyricsOffsetMs.first())
            reopened.setLyricsOffsetMs(Int.MIN_VALUE)
            reopened.adjustLyricsOffsetMs(-500)
            assertEquals(Int.MIN_VALUE, reopened.lyricsOffsetMs.first())
        } finally {
            scope.coroutineContext[Job]!!.cancelAndJoin()
            file.delete()
            directory.delete()
        }
    }
}
