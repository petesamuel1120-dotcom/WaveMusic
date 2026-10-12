package com.petedev.wavemusic.domain.manager

/** The header material is independent of whether its buttons stay pinned. */
enum class FrostedGlassHeaderStyle(val preferenceValue: String) {
    Off("off"),
    Moderate("moderate"),
    Balanced("balanced"),
    Fuller("fuller");

    val isEnabled: Boolean get() = this != Off

    companion object {
        val Default = Moderate

        fun fromPreference(value: String?, default: FrostedGlassHeaderStyle = Default): FrostedGlassHeaderStyle =
            entries.firstOrNull { it.preferenceValue == value } ?: default
    }
}
