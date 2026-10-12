package com.petedev.wavemusic.ui.theme

import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.n_track
import com.petedev.wavemusic.resources.settings_personalize
import com.petedev.wavemusic.resources.play_action
import com.petedev.wavemusic.resources.voice_try_saying
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class LanguageResourcesTest {
    @Test
    fun languagePickerRecognizesSystemTagsRegionsAndLegacyAliases() {
        val supported = com.petedev.wavemusic.common.SUPPORTED_LANGUAGE
        for (tag in listOf("ar", "ar-SA", "ar-EG")) {
            assertEquals("اللغة العربية", supported.getLanguageFromCode(tag))
        }
        for (tag in listOf("de", "de-DE", "de_AT")) {
            assertEquals("Deutsch", supported.getLanguageFromCode(tag))
        }
        assertEquals(supported.getLanguageFromCode("iw-IL"), supported.getLanguageFromCode("he"))
        assertEquals(supported.getLanguageFromCode("id-ID"), supported.getLanguageFromCode("in-ID"))
        assertEquals(supported.getLanguageFromCode("zh-Hant-TW"), supported.getLanguageFromCode("zh-TW"))
        assertEquals(supported.getLanguageFromCode("zh-CN"), supported.getLanguageFromCode("zh-Hans-TW"))
    }

    @Test
    fun arabicAndGermanCoverEveryDefaultResourceAndPreserveFormatArguments() {
        val root = listOf(Path.of("src/commonMain/composeResources"),
            Path.of("composeApp/src/commonMain/composeResources")).first { Files.isDirectory(it) }
        fun resources(locale: String): Map<String, Pair<String, String>> {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(root.resolve("$locale/strings.xml").toFile())
            val nodes = document.documentElement.childNodes
            return (0 until nodes.length).mapNotNull { index ->
                val element = nodes.item(index) as? org.w3c.dom.Element ?: return@mapNotNull null
                element.getAttribute("name") to (element.tagName to element.textContent)
            }.toMap()
        }
        val defaults = resources("values")
        val formats = Regex("%(?:\\d+\\$)?[dsf]")
        for (locale in listOf("values-ar", "values-de")) {
            val translated = resources(locale)
            assertTrue((defaults.keys - translated.keys).isEmpty(),
                "Missing translations in $locale: ${defaults.keys - translated.keys}")
            for ((key, value) in defaults) {
                val localized = translated.getValue(key)
                assertEquals(value.first, localized.first, "Resource kind changed for $locale/$key")
                if (value.first == "string") {
                    assertEquals(formats.findAll(value.second).map { it.value }.sorted().toList(),
                        formats.findAll(localized.second).map { it.value }.sorted().toList(),
                        "Format arguments changed for $locale/$key")
                    if (value.second.isNotBlank()) {
                        assertTrue(localized.second.isNotBlank(), "Empty translation for $locale/$key")
                    }
                }
            }
        }
    }

    @Test
    fun switchingLanguagesReloadsSettingsPlaybackAndVoiceSearchStrings() = runBlocking {
        val original = Locale.getDefault()
        try {
            for ((tag, expected) in listOf(
                "en-US" to listOf("Personalize", "Play", "Try saying"),
                "ar-SA" to listOf("التخصيص", "تشغيل", "جرّب أن تقول"),
                "de-DE" to listOf("Personalisierung", "Wiedergeben", "Sag zum Beispiel"),
                "en-US" to listOf("Personalize", "Play", "Try saying"),
            )) {
                Locale.setDefault(Locale.forLanguageTag(tag))
                assertEquals(expected, listOf(getString(Res.string.settings_personalize),
                    getString(Res.string.play_action), getString(Res.string.voice_try_saying)))
            }
            Locale.setDefault(Locale.forLanguageTag("ar-SA"))
            assertEquals("مقطعان", getPluralString(Res.plurals.n_track, 2, 2))
            assertEquals("3 مقاطع", getPluralString(Res.plurals.n_track, 3, 3))
            Locale.setDefault(Locale.forLanguageTag("de-DE"))
            assertEquals("2 Titel", getPluralString(Res.plurals.n_track, 2, 2))
        } finally {
            Locale.setDefault(original)
        }
    }
}
