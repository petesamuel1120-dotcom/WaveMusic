@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3ComponentOverrideApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.simpmusic.ui.theme.*
import org.jetbrains.skia.Bitmap
import org.jetbrains.compose.resources.getString
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.playlist_name_cannot_be_empty
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class PlaylistDialogsTest {
    private class Fixture(val advanced: Boolean = true, val dark: Boolean = true, val createOnly: Boolean = false) : AutoCloseable {
        val origins = DialogOriginTracker()
        val visible = mutableStateOf(true)
        var dismissed = 0
        val created = mutableListOf<String>()
        val selected = mutableListOf<String>()
        val scheme = if (dark) darkColorScheme(surface = Color(0xFF141C24))
            else lightColorScheme(surfaceContainerHigh = Color(0xFFF0E4D4))
        val container = if (dark) scheme.surface else scheme.surfaceContainerHigh
        val scene = ImageComposeScene(450, 900) {
            // Match a player with its own forced-dark palette while app dialogs use the app theme.
            MaterialTheme(colorScheme = darkColorScheme(surface = Color(0xFF601010))) {
                CompositionLocalProvider(
                    LocalAdvancedDialogAnimations provides advanced,
                    LocalDialogOrigins provides origins,
                    LocalAppColorScheme provides scheme,
                    LocalAppIsDarkTheme provides dark,
                    LocalForceDarkText provides true,
                    LocalBasicAlertDialogOverride provides if (advanced) AnimatedAppAlertDialogOverride else DefaultBasicAlertDialogOverride,
                ) {
                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        if (visible.value) {
                            if (createOnly) CreatePlaylistDialog(
                                onDismissRequest = { dismissed++; visible.value = false },
                                onCreate = { created += it },
                            ) else AddToPlaylistModalBottomSheet(
                                isBottomSheetVisible = true,
                                listLocalPlaylist = listOf(
                                    LocalPlaylistEntity(id = 1, title = "FM"),
                                    LocalPlaylistEntity(id = 2, title = "Already added", tracks = listOf("song")),
                                    LocalPlaylistEntity(id = 3, title = "Linked", youtubePlaylistId = "linked"),
                                    LocalPlaylistEntity(id = 4, title = "New episodes"),
                                ),
                                listYouTubePlaylist = listOf(
                                    playlist("linked", "Duplicate remote"), playlist("remote", "Remote"),
                                    playlist("hidden", "Episodes for later"),
                                ),
                                videoId = "song",
                                onClick = { selected += it.title },
                                onYTPlaylistClick = { selected += it.title },
                                onDismiss = { dismissed++; visible.value = false },
                                onCreatePlaylist = { created += it },
                            )
                        }
                    }
                }
            }
        }
        var time = 0L
        fun frames(count: Int = 45) { repeat(count) { scene.render(time.also { time += 16_666_667L }).close() } }
        fun nodes(): List<SemanticsNode> {
            fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
            return scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
        }
        fun text(node: SemanticsNode) = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }.orEmpty()
        fun button(label: String) = nodes().first { text(it) == label && it.config.getOrNull(SemanticsActions.OnClick) != null }
        fun click(label: String) { assertTrue(button(label).config[SemanticsActions.OnClick].action!!.invoke()) }
        fun setName(value: String) {
            val field = nodes().first { it.config.getOrNull(SemanticsActions.SetText) != null }
            assertTrue(field.config[SemanticsActions.SetText].action!!.invoke(AnnotatedString(value)))
            frames(2)
        }
        fun assertContainerPixels() {
            scene.render(time.also { time += 16_666_667L }).use { image ->
                Bitmap.makeFromImage(image).use { bitmap ->
                    val color = container.toArgb()
                    var matching = 0
                    for (y in 0 until bitmap.height step 3) for (x in 0 until bitmap.width step 3) {
                        if (bitmap.getColor(x, y) == color) matching++
                    }
                    assertTrue(matching > 1_000, "The surface must use the shared app dialog shade, not the player's forced-dark shade: $matching")
                }
            }
        }
        override fun close() { scene.close() }
    }

    @Test
    fun pickerKeepsItsOriginalBottomSheetLayoutWithAdvancedEffectsOnOrOff() {
        for (advanced in listOf(true, false)) Fixture(advanced = advanced).use { f ->
            f.frames()
            assertEquals(0f, f.origins.backgroundProgress(null), "The picker itself must not use dialog motion")
            assertTrue(f.button("Create playlist").boundsInRoot.top > 450f, "Keep the picker at the bottom of the screen")
            assertTrue(f.nodes().none { f.text(it) == "Add to a playlist" || f.text(it) == "Cancel" },
                "Keep the original drag-handle/row layout, without the added dialog heading and Cancel button")
            f.click("FM")
            assertEquals(listOf("FM"), f.selected)
            f.frames()
            assertEquals(listOf("FM"), f.selected)
            assertEquals(1, f.dismissed)
            assertEquals(0f, f.origins.backgroundProgress(null))
        }
    }

    @Test
    fun pickerPreservesFilteringAndDuplicateAttemptsKeepThePickerOpenWithoutAdding() {
        Fixture().use { f ->
            f.frames()
            val text = f.nodes().map(f::text)
            assertTrue("Linked" in text && "Remote" in text)
            assertTrue("New episodes" !in text && "Episodes for later" !in text && "Duplicate remote" !in text)
            f.click("Already added")
            f.frames(2)
            assertTrue(f.selected.isEmpty(), "Do not call the add callback for a known duplicate")
            assertTrue(f.visible.value, "Keep the picker open so another playlist can be selected")
            f.click("Remote")
            f.frames()
            assertEquals(listOf("Remote"), f.selected)
        }
    }

    @Test
    fun createRowAnchorsItsDialogAndCancelReturnsToThePicker() {
        Fixture().use { f ->
            f.frames()
            val row = f.button("Create playlist")
            f.click("Create playlist")
            val origin = assertNotNull(f.origins.takeRecent())
            assertNotNull(origin.source)
            assertEquals(row.boundsInRoot, origin.bounds)
            f.origins.record(origin.bounds, origin.source, origin.cornerRadiusPx)
            f.frames()
            val source = assertNotNull(origin.source)
            val motion = assertNotNull(source.motion)
            assertEquals(origin.bounds, motion.origin?.bounds, "The form must expand from the Create row in the bottom sheet")
            assertEquals(1f, motion.progress.value)
            assertEquals(0f, source.opacity, "Only the traveling copy should draw while its dialog is open")
            assertTrue(f.nodes().any { it.config.getOrNull(SemanticsActions.SetText) != null })
            // The child dialog owns the last semantics owner; its Cancel must leave the picker open.
            val cancel = f.nodes().last { f.text(it) == "Cancel" && it.config.getOrNull(SemanticsActions.OnClick) != null }
            cancel.config[SemanticsActions.OnClick].action!!.invoke()
            f.frames()
            assertTrue(f.visible.value)
            assertEquals(0, f.dismissed)
            assertTrue(f.nodes().none { it.config.getOrNull(SemanticsActions.SetText) != null })
            assertEquals(0f, f.origins.backgroundProgress(null))
            assertNull(source.motion)
            assertEquals(1f, source.opacity, "Cancel restores the Create row without flicker")
        }
    }

    @Test
    fun blankNamesShowAnErrorImmediatelyAndValidNamesCommitOnceAfterClosing() {
        Fixture(createOnly = true).use { f ->
            f.frames()
            f.click("Create playlist")
            f.frames(2)
            assertEquals(1f, f.origins.backgroundProgress(null), "Validation must keep the dialog open without a closing/reopening animation")
            val message = runBlocking { getString(Res.string.playlist_name_cannot_be_empty) }
            assertTrue(f.nodes().any { f.text(it).contains(message) }, "Show the localized validation message: ${f.nodes().map(f::text)}")
            assertTrue(f.created.isEmpty())
            f.setName("  Road trip  ")
            f.click("Create playlist")
            f.button("Create playlist").config[SemanticsActions.OnClick].action!!.invoke()
            assertTrue(f.created.isEmpty())
            f.frames()
            assertEquals(listOf("Road trip"), f.created)
            assertEquals(1, f.dismissed)
        }
    }

    @Test
    fun creatingFromThePickerCommitsOnceAndClosesBothDialogs() {
        Fixture().use { f ->
            f.frames()
            f.click("Create playlist")
            f.frames()
            f.setName("  New mix  ")
            val confirm = f.nodes().last { f.text(it) == "Create playlist" && it.config.getOrNull(SemanticsActions.OnClick) != null }
            confirm.config[SemanticsActions.OnClick].action!!.invoke()
            assertTrue(f.created.isEmpty())
            f.frames(90)
            assertEquals(listOf("New mix"), f.created)
            assertEquals(1, f.dismissed)
            assertEquals(0f, f.origins.backgroundProgress(null))
        }
    }

    @Test
    fun disablingAdvancedAnimationsKeepsCreationUsableFromTheOriginalSheet() {
        for (dark in listOf(true, false)) Fixture(advanced = false, dark = dark).use { f ->
            f.frames()
            assertEquals(0f, f.origins.backgroundProgress(null))
            f.click("Create playlist")
            f.frames()
            f.setName("Normal mix")
            f.nodes().last { f.text(it) == "Create playlist" && it.config.getOrNull(SemanticsActions.OnClick) != null }
                .config[SemanticsActions.OnClick].action!!.invoke()
            f.frames()
            assertEquals(listOf("Normal mix"), f.created)
            assertEquals(1, f.dismissed)
        }
    }

    @Test
    fun creationUsesTheSameAppDialogShadeAndDisablingEffectsUsesTheNormalPath() {
        for (advanced in listOf(true, false)) for (dark in listOf(true, false)) {
            Fixture(advanced = advanced, dark = dark, createOnly = true).use { f ->
                f.frames()
                f.assertContainerPixels()
                assertEquals(if (advanced) 1f else 0f, f.origins.backgroundProgress(null))
                f.click("Cancel")
                f.frames()
                assertEquals(1, f.dismissed)
            }
        }
    }

    private companion object {
        fun playlist(id: String, title: String) = PlaylistsResult("Author", id, "Playlist", "1", "Playlist", emptyList(), title)
    }
}
