@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.skia.EncodedImageFormat
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.lyrics_offset_seconds_value
import simpmusic.composeapp.generated.resources.decrease_lyrics_offset
import simpmusic.composeapp.generated.resources.increase_lyrics_offset
import simpmusic.composeapp.generated.resources.lyrics_offset_default
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LyricsOffsetControlTest {
    private fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
    private fun nodes(scene: ImageComposeScene) = scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
    private fun buttons(scene: ImageComposeScene) = nodes(scene)
        .filter { it.config.getOrNull(SemanticsActions.OnClick) != null }
        .sortedBy { it.boundsInRoot.left }
    private fun valueText(value: String) = runBlocking { getString(Res.string.lyrics_offset_seconds_value, value) }
    private fun stepButtons(scene: ImageComposeScene): List<SemanticsNode> =
        listOf(Res.string.decrease_lyrics_offset, Res.string.increase_lyrics_offset).map { resource ->
            val label = runBlocking { getString(resource) }
            buttons(scene).first { button -> descendants(button).any { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true } }
        }

    @Test
    fun pressesAdjustByHalfASecondAndKeepExistingMillisecondPrecision() {
        valueText("0.0") // Warm the same resource table before the offscreen composition.
        val offset = mutableStateOf<Int?>(0)
        val deltas = mutableListOf<Int>()
        val scene = ImageComposeScene(360, 220) {
            MaterialTheme {
                LyricsOffsetControl(offset.value, { delta -> deltas += delta; offset.value = offset.value!! + delta }, onDefault = { offset.value = 0 })
            }
        }
        var time = 0L
        fun frames() { repeat(3) { time += 16_666_667L; scene.render(time).close() } }
        fun assertValue(expected: String) {
            assertTrue(nodes(scene).any { node -> node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == valueText(expected) } == true })
        }
        fun click(index: Int) { stepButtons(scene)[index].config[SemanticsActions.OnClick].action!!.invoke(); frames() }
        try {
            frames()
            assertEquals(3, buttons(scene).size)
            assertValue("0.0")
            click(1)
            assertValue("+0.5")
            click(1)
            assertValue("+1.0")
            repeat(3) { click(0) }
            assertValue("-0.5")
            assertEquals(listOf(500, 500, -500, -500, -500), deltas)
            offset.value = 1234
            frames()
            click(1)
            assertValue("+1.734")
            val defaultLabel = runBlocking { getString(Res.string.lyrics_offset_default) }
            buttons(scene).first { button -> descendants(button).any { it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == defaultLabel } == true } }
                .config[SemanticsActions.OnClick].action!!.invoke()
            frames()
            assertValue("0.0")

            offset.value = null
            frames()
            buttons(scene).forEach { assertNotNull(it.config.getOrNull(SemanticsProperties.Disabled)) }
        } finally { scene.close() }
    }

    @Test
    fun existingOffsetsAreDisplayedExactly() {
        assertEquals("0.0", lyricsOffsetSecondsValue(0))
        assertEquals("+0.5", lyricsOffsetSecondsValue(500))
        assertEquals("-0.5", lyricsOffsetSecondsValue(-500))
        assertEquals("+0.001", lyricsOffsetSecondsValue(1))
        assertEquals("-2147483.648", lyricsOffsetSecondsValue(Int.MIN_VALUE))
    }

    @Test
    fun controlsFitLargeTextInBothThemes() {
        valueText("-1.5")
        for (dark in listOf(true, false)) {
            val scheme = if (dark) darkColorScheme() else lightColorScheme()
            val scene = ImageComposeScene(420, 260, density = Density(1f, 1.6f)) {
                MaterialTheme(colorScheme = scheme) {
                    CompositionLocalProvider(LocalAppColorScheme provides scheme, LocalAppIsDarkTheme provides dark, LocalIsDarkTheme provides dark) {
                        Surface(Modifier.fillMaxSize()) { LyricsOffsetControl(-1500, {}, showHint = true, onDefault = {}, scopeHint = "Applies to songs without their own offset.") }
                    }
                }
            }
            var time = 0L
            try {
                repeat(3) { time += 16_666_667L; scene.render(time).close() }
                val controls = stepButtons(scene)
                assertEquals(2, controls.size)
                assertTrue(controls[0].boundsInRoot.right <= controls[1].boundsInRoot.left)
                assertTrue(controls.all { it.boundsInRoot.width >= 48f && it.boundsInRoot.height >= 48f })
                val value = nodes(scene).first { node -> node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == valueText("-1.5") } == true }
                assertTrue(controls[0].boundsInRoot.right <= value.boundsInRoot.left)
                assertTrue(value.boundsInRoot.right <= controls[1].boundsInRoot.left)
                System.getenv("WAVEMUSIC_LYRICS_OFFSET_PREVIEW")?.let { path ->
                    val folder = File(path).also { it.mkdirs() }
                    val image = scene.render(time + 16_666_667L)
                    try { File(folder, if (dark) "offset-dark.png" else "offset-light.png").writeBytes(requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).bytes) }
                    finally { image.close() }
                }
            } finally { scene.close() }
        }
    }
}
