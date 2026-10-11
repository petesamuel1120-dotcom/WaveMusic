package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme

@Composable
internal fun isOnyxBackground(): Boolean =
    LocalAppIsDarkTheme.current &&
        LocalAppBackgroundPreset.current == DataStoreManager.APP_BACKGROUND_AMOLED_BLACK

/** Adds a readable surface to otherwise bare icon controls on pure-black Onyx pages. */
@Composable
internal fun Modifier.onyxControlSurface(shape: Shape): Modifier =
    if (isOnyxBackground()) {
        background(flatChromeControlBackground(), shape)
            .border(1.dp, flatChromeControlOutline(), shape)
    } else {
        this
    }

/** Theme chrome leaves a subtle view of the page when Liquid Glass is disabled. */
@Composable
fun flatChromeBackground(alpha: Float = 0.9f): Color {
    val scheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val usesSolidPreset = LocalAppBackgroundPreset.current != DataStoreManager.APP_BACKGROUND_AURORA
    val background =
        if (usesSolidPreset) {
            scheme.surfaceContainerHigh
        } else if (LocalAppIsDarkTheme.current) {
            Color.Black
        } else {
            scheme.background
        }
    return background.copy(alpha = alpha)
}

/** Distinct, opaque control fills also use the app palette on immersive artwork pages. */
@Composable
fun flatChromeControlBackground(): Color {
    val scheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    return scheme.primary.copy(alpha = 0.04f)
        .compositeOver(scheme.surfaceContainerHighest.copy(alpha = 1f))
}

/** A fixed selection fill that stays distinct without sampling scrolling content. */
@Composable
fun flatChromeSelectionBackground(): Color {
    val scheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    return scheme.primary.copy(alpha = if (LocalAppIsDarkTheme.current) 0.22f else 0.14f)
        .compositeOver(flatChromeControlBackground())
}

@Composable
fun flatChromeControlOutline(): Color =
    (LocalAppColorScheme.current ?: MaterialTheme.colorScheme).outlineVariant.copy(alpha = 0.72f)

@Composable
fun flatChromeForeground(): Color =
    if (LocalAppIsDarkTheme.current) Color.White else Color.Black
