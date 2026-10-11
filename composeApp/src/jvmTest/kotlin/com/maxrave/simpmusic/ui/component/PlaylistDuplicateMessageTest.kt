package com.maxrave.simpmusic.ui.component

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.already_in_playlist
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaylistDuplicateMessageTest {
    @Test
    fun duplicateFeedbackUsesTheUserFacingResource() = runBlocking<Unit> {
        assertEquals("Already in this playlist", getString(Res.string.already_in_playlist))
    }
}
