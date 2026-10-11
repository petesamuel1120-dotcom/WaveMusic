package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalForceDarkText
import com.maxrave.simpmusic.ui.theme.typo

@Immutable
data class AppDialogPalette(
    val container: Color,
    val content: Color,
    val secondaryContent: Color,
    val border: Color,
)

/** Dialog colors follow the app's selected background and retain readable contrast. */
@Composable
fun rememberAppDialogPalette(): AppDialogPalette {
    val scheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val isDark = LocalAppIsDarkTheme.current
    return remember(isDark, scheme) {
        if (isDark) {
            AppDialogPalette(
                container = scheme.surface,
                content = scheme.onSurface,
                secondaryContent = scheme.onSurfaceVariant,
                border = scheme.outlineVariant.copy(alpha = 0.5f),
            )
        } else {
            AppDialogPalette(
                container = scheme.surfaceContainerHigh,
                content = scheme.onSurface,
                secondaryContent = scheme.onSurfaceVariant,
                border = scheme.outline.copy(alpha = 0.22f),
            )
        }
    }
}

/** Runs dialog content against the app theme, even when opened from a forced-dark player screen. */
@Composable
fun AppDialogTheme(content: @Composable () -> Unit) {
    val appScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val appIsDark = LocalAppIsDarkTheme.current
    MaterialTheme(
        colorScheme = appScheme,
        typography = typo(appScheme, forceDark = false),
    ) {
        CompositionLocalProvider(
            LocalIsDarkTheme provides appIsDark,
            LocalForceDarkText provides false,
            LocalContentColor provides appScheme.onSurfaceVariant,
            content = content,
        )
    }
}

/** Shared compact alert styling: AMOLED in dark mode, a soft theme surface in light mode. */
@Composable
fun AmoledAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    containerColorOverride: Color? = null,
    raisedDarkContainer: Boolean = false,
) {
    AppDialogTheme {
        val palette = rememberAppDialogPalette()
        val containerColor = when {
            raisedDarkContainer && LocalAppIsDarkTheme.current -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> containerColorOverride ?: palette.container
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = { DialogMotionContent(DialogContentStage.Actions, confirmButton) },
            modifier = modifier.border(1.dp, palette.border, RoundedCornerShape(24.dp)),
            dismissButton = dismissButton?.let { { DialogMotionContent(DialogContentStage.Actions, it) } },
            icon = icon?.let { { DialogMotionContent(DialogContentStage.Title, it) } },
            title = title?.let { { DialogMotionContent(DialogContentStage.Title, it) } },
            text = text?.let { { DialogMotionContent(DialogContentStage.Body, it) } },
            shape = RoundedCornerShape(24.dp),
            containerColor = containerColor,
            titleContentColor = palette.content,
            textContentColor = palette.secondaryContent,
            iconContentColor = palette.content,
            tonalElevation = 0.dp,
            properties = properties,
        )
    }
}
