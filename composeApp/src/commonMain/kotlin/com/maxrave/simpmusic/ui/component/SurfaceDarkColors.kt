package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme

/**
 * Color set for overlay surfaces (bottom sheets & dialogs), following the app's selected theme.
 *
 * The app-level locals stay stable inside immersive force-dark screens. Reading them here keeps a
 * menu light in light mode even when opened from an artwork-backed page, and aligned with the
 * selected solid background in dark mode.
 *
 * Keep `Color.Transparent` and `scrimColor = Color.Black.copy(...)` as-is — the scrim is a dim over the
 * content behind the sheet and is dark in both themes.
 */
@Immutable
data class SurfaceDarkColors(
    val container: Color, // sheet/dialog background + inner cards (selected dark or light surface)
    val handle: Color, // drag handle (old #474545)
    val content: Color, // primary text + icons (old white)
    val subtitle: Color, // secondary text (old #B0B0A0 / #CCCCCC / #D0D0C0)
    val disabled: Color, // disabled text/icons + dividers (old gray / #555555 / #3D3D3D)
)

@Composable
fun rememberSurfaceDarkColors(): SurfaceDarkColors {
    val cs = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    return if (LocalAppIsDarkTheme.current) {
        SurfaceDarkColors(
            container = cs.surface,
            handle = cs.outlineVariant,
            content = cs.onSurface,
            subtitle = cs.onSurfaceVariant,
            disabled = cs.onSurfaceVariant.copy(alpha = 0.38f),
        )
    } else {
        SurfaceDarkColors(
            container = cs.surfaceContainerLow,
            handle = cs.outlineVariant,
            content = cs.onSurface,
            subtitle = cs.onSurfaceVariant,
            disabled = cs.onSurfaceVariant.copy(alpha = 0.38f),
        )
    }
}
