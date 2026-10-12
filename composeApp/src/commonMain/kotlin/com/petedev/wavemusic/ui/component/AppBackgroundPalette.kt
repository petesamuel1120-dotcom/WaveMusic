package com.petedev.wavemusic.ui.component

import androidx.compose.ui.graphics.Color
import com.petedev.wavemusic.domain.manager.DataStoreManager

data class AppBackgroundPresetColors(
    val id: String,
    val dark: Color,
    val light: Color,
)

/** Slightly desaturate and dim dark solid presets while keeping their color identity. */
private fun tonedDownDarkPreset(color: Color): Color {
    val neutral = color.red * 0.2126f + color.green * 0.7152f + color.blue * 0.0722f
    fun channel(value: Float): Float = (value * 0.84f + neutral * 0.16f) * 0.92f
    return Color(channel(color.red), channel(color.green), channel(color.blue), color.alpha)
}

/** Solid alternatives for the app-wide aurora background, paired for dark and light themes. */
object AppBackgroundPalette {
    val presets = listOf(
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_AMOLED_BLACK, Color(0xFF000000), Color(0xFFF7F8FA)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_MIDNIGHT, tonedDownDarkPreset(Color(0xFF080A12)), Color(0xFFF1F2FA)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_NAVY, tonedDownDarkPreset(Color(0xFF09121D)), Color(0xFFEEF4FA)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_SLATE, tonedDownDarkPreset(Color(0xFF111519)), Color(0xFFF0F3F5)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_FOREST, tonedDownDarkPreset(Color(0xFF09130F)), Color(0xFFEEF6F0)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_PLUM, tonedDownDarkPreset(Color(0xFF140D17)), Color(0xFFF6F0F8)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_BURGUNDY, tonedDownDarkPreset(Color(0xFF180D12)), Color(0xFFF8EFF1)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_TEAL, tonedDownDarkPreset(Color(0xFF071314)), Color(0xFFEBF6F6)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_OCEAN, tonedDownDarkPreset(Color(0xFF07121B)), Color(0xFFEDF4F8)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_INDIGO, tonedDownDarkPreset(Color(0xFF0E0E1E)), Color(0xFFF0EFF9)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_ESPRESSO, tonedDownDarkPreset(Color(0xFF160F0C)), Color(0xFFF7F1EC)),
        AppBackgroundPresetColors(DataStoreManager.APP_BACKGROUND_GRAPHITE, tonedDownDarkPreset(Color(0xFF131618)), Color(0xFFF0F2F3)),
    )

    fun colorFor(id: String, isLightTheme: Boolean): Color? =
        presets.firstOrNull { it.id == id }?.let { if (isLightTheme) it.light else it.dark }
}
