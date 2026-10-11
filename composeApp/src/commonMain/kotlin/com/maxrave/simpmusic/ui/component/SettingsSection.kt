package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.LocalSettingsTypography

/** Keeps preference rows, text fields, buttons, and dialogs on the same type scale. */
@Composable
fun SettingsTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSettingsTypography provides true) {
        MaterialTheme(typography = typo(), content = content)
    }
}

/** Shared fill for settings rows and buttons that use the same surface. */
@Composable
internal fun settingsButtonContainerColor(): Color {
    val colors = MaterialTheme.colorScheme
    if (isOnyxBackground()) {
        return colors.surfaceContainerHighest
    }
    val light = colors.background.luminance() > 0.5f
    val container =
        if (light) colors.surfaceContainer else lerp(colors.surfaceContainer, colors.background, 0.55f)
    return container.copy(alpha = if (light) 0.78f else 0.72f)
}

/** Cards and shortcuts use visible theme surfaces in Aurora and Onyx. */
@Composable
internal fun adaptiveCardContainerColor(defaultColor: Color, accent: Boolean = false): Color {
    return when {
        isOnyxBackground() ->
            if (accent) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else MaterialTheme.colorScheme.surfaceContainerHighest
        LocalAppIsDarkTheme.current && LocalAppBackgroundPreset.current == DataStoreManager.APP_BACKGROUND_AURORA ->
            if (accent) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else settingsButtonContainerColor()
        else -> defaultColor
    }
}

/** Shared settings hierarchy. Translucent surfaces let the existing aurora show through. */
@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val light = colors.background.luminance() > 0.5f
    Column(modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (icon != null) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(14.dp))
                        .background(colors.primary.copy(alpha = if (light) 0.10f else 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = typo().titleSmall.copy(fontSize = 18.sp, lineHeight = 25.sp),
                color = colors.onSurface,
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = settingsButtonContainerColor(),
            contentColor = colors.onSurface,
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = if (light) 0.45f else 0.32f)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                content = content,
            )
        }
    }
}

/** Dependent fields stay visibly inside the option that reveals them. */
@Composable
fun SettingsNested(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary.copy(alpha = 0.04f))
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp),
        content = content,
    )
}

@Composable
fun SettingsDivider() {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    )
}
