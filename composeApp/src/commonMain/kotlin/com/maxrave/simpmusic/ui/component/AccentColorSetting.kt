package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.ui.icon.Check
import com.maxrave.simpmusic.ui.icon.KeyboardArrowDown
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.parseThemeColorHex
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import com.maxrave.simpmusic.ui.theme.mutedForDarkTheme
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.accent_color_blue
import simpmusic.composeapp.generated.resources.accent_color_cyan
import simpmusic.composeapp.generated.resources.accent_color_mint
import simpmusic.composeapp.generated.resources.accent_color_pink
import simpmusic.composeapp.generated.resources.accent_color_purple
import simpmusic.composeapp.generated.resources.accent_color_sunset
import simpmusic.composeapp.generated.resources.accent_color_turquoise
import simpmusic.composeapp.generated.resources.hide
import simpmusic.composeapp.generated.resources.show
import simpmusic.composeapp.generated.resources.theme_color
import simpmusic.composeapp.generated.resources.theme_color_default

private data class AccentPreset(val label: String, val hex: String?, val color: Color)

/** Preset accent palette; null restores Default, and an opaque ARGB value selects a preset seed. */
@Composable
fun AccentColorSetting(
    colorSource: String,
    customColorHex: String,
    onSelectColor: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val isDark = LocalIsDarkTheme.current
    val isCustom = colorSource == DataStoreManager.THEME_COLOR_CUSTOM
    val presets = listOf(
        AccentPreset(stringResource(Res.string.theme_color_default), null, seed),
        AccentPreset(stringResource(Res.string.accent_color_sunset), "FFFFD38A", Color(0xFFFFD38A)),
        AccentPreset(stringResource(Res.string.accent_color_pink), "FFFF3568", Color(0xFFFF3568)),
        AccentPreset(stringResource(Res.string.accent_color_turquoise), "FF2FCBC4", Color(0xFF2FCBC4)),
        AccentPreset(stringResource(Res.string.accent_color_cyan), "FF63D2F3", Color(0xFF63D2F3)),
        AccentPreset(stringResource(Res.string.accent_color_mint), "FF29CB59", Color(0xFF29CB59)),
        AccentPreset(stringResource(Res.string.accent_color_purple), "FFB858EC", Color(0xFFB858EC)),
        AccentPreset(stringResource(Res.string.accent_color_blue), "FF168BFA", Color(0xFF168BFA)),
    )
    val selectedPreset = presets.firstOrNull { preset ->
        if (preset.hex == null) !isCustom else isCustom && preset.hex.equals(customColorHex, ignoreCase = true)
    }
    val selectedColor = (if (isCustom) parseThemeColorHex(customColorHex) ?: seed else seed)
        .let { if (isDark) it.mutedForDarkTheme() else it }
    val selectedLabel = selectedPreset?.label ?: "#${customColorHex.takeLast(6).uppercase()}"
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, tween(220), label = "accentChevron")
    val toggleLabel = stringResource(if (expanded) Res.string.hide else Res.string.show)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 72.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(role = Role.Button, onClickLabel = toggleLabel) {
                    expanded = !expanded
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentSwatch(selectedColor, selected = false, modifier = Modifier.size(36.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.theme_color), style = typo().labelMedium, color = colors.onSurface)
                Text(selectedLabel, style = typo().bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(SimpIcons.KeyboardArrowDown, null, tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp).rotate(chevronRotation))
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(220)) + fadeOut(tween(150)),
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BoxWithConstraints(Modifier.fillMaxWidth().selectableGroup()) {
                    val density = LocalDensity.current
                    val fontScale = density.fontScale.coerceAtLeast(1f)
                    val textMeasurer = rememberTextMeasurer()
                    val labelStyle = typo().bodySmall
                    // Fit whole words before adding columns, including translated color names.
                    val widestWord = with(density) {
                        presets.flatMap { it.label.split(' ') }.maxOf { word ->
                            textMeasurer.measure(word, style = labelStyle, maxLines = 1).size.width.toDp()
                        }
                    }
                    val minimumTileWidth = maxOf(68.dp * fontScale, widestWord + 16.dp)
                    val columns = ((maxWidth + 8.dp) / (minimumTileWidth + 8.dp)).toInt().coerceIn(2, 4)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.chunked(columns).forEach { row ->
                            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { preset ->
                                    AccentPresetTile(
                                        preset = if (isDark) preset.copy(color = preset.color.mutedForDarkTheme()) else preset,
                                        selected = selectedPreset == preset,
                                        onClick = {
                                            onSelectColor(preset.hex)
                                        },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun AccentPresetTile(preset: AccentPreset, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.heightIn(min = 96.dp).clip(shape)
            .background(if (selected) colors.primary.copy(alpha = 0.12f) else colors.surfaceContainerHighest.copy(alpha = 0.65f))
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AccentSwatch(preset.color, selected, Modifier.size(32.dp))
        Spacer(Modifier.height(6.dp))
        Text(preset.label, style = typo().bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurface,
            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AccentSwatch(color: Color, selected: Boolean, modifier: Modifier) {
    Box(modifier.background(color, CircleShape).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center) {
        if (selected) {
            Icon(SimpIcons.Check, null, tint = if (color.luminance() > 0.45f) Color.Black else Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
