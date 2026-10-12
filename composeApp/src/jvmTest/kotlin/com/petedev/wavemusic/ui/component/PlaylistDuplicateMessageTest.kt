package com.petedev.wavemusic.ui.component

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.already_in_playlist
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaylistDuplicateMessageTest {
    @Test
    fun duplicateFeedbackUsesTheUserFacingResource() = runBlocking<Unit> {
        assertEquals("Already in this playlist", getString(Res.string.already_in_playlist))
    }
}
