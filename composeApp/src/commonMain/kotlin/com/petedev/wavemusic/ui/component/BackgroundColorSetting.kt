package com.petedev.wavemusic.ui.component

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.ui.icon.Check
import com.petedev.wavemusic.ui.icon.KeyboardArrowDown
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.background_color_amoled_black
import com.petedev.wavemusic.resources.background_color_burgundy
import com.petedev.wavemusic.resources.background_color_aurora
import com.petedev.wavemusic.resources.background_color_espresso
import com.petedev.wavemusic.resources.background_color_forest
import com.petedev.wavemusic.resources.background_color_graphite
import com.petedev.wavemusic.resources.background_color_indigo
import com.petedev.wavemusic.resources.background_color_midnight
import com.petedev.wavemusic.resources.background_color_navy
import com.petedev.wavemusic.resources.background_color_ocean
import com.petedev.wavemusic.resources.background_color_plum
import com.petedev.wavemusic.resources.background_color_slate
import com.petedev.wavemusic.resources.background_color_teal
import com.petedev.wavemusic.resources.background_color_title
import com.petedev.wavemusic.resources.hide
import com.petedev.wavemusic.resources.show

private data class BackgroundChoice(val id: String, val label: String, val swatch: Color)

@Composable
fun BackgroundColorSetting(
    selectedPreset: String,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val isLightTheme = colors.background.luminance() > 0.5f
    val choices = listOf(
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_AURORA, stringResource(Res.string.background_color_aurora), Color(0xFF35305A)),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_AMOLED_BLACK, stringResource(Res.string.background_color_amoled_black), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_AMOLED_BLACK, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_MIDNIGHT, stringResource(Res.string.background_color_midnight), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_MIDNIGHT, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_NAVY, stringResource(Res.string.background_color_navy), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_NAVY, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_SLATE, stringResource(Res.string.background_color_slate), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_SLATE, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_FOREST, stringResource(Res.string.background_color_forest), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_FOREST, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_PLUM, stringResource(Res.string.background_color_plum), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_PLUM, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_BURGUNDY, stringResource(Res.string.background_color_burgundy), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_BURGUNDY, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_TEAL, stringResource(Res.string.background_color_teal), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_TEAL, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_OCEAN, stringResource(Res.string.background_color_ocean), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_OCEAN, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_INDIGO, stringResource(Res.string.background_color_indigo), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_INDIGO, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_ESPRESSO, stringResource(Res.string.background_color_espresso), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_ESPRESSO, isLightTheme)!!),
        BackgroundChoice(DataStoreManager.APP_BACKGROUND_GRAPHITE, stringResource(Res.string.background_color_graphite), AppBackgroundPalette.colorFor(DataStoreManager.APP_BACKGROUND_GRAPHITE, isLightTheme)!!),
    )
    val selected = choices.firstOrNull { it.id == selectedPreset } ?: choices.first()
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, tween(220), label = "backgroundColorChevron")
    val toggleLabel = stringResource(if (expanded) Res.string.hide else Res.string.show)

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 72.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(role = Role.Button, onClickLabel = toggleLabel) { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackgroundSwatch(selected, selected = false, modifier = Modifier.size(36.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.background_color_title), style = typo().labelMedium, color = colors.onSurface)
                Text(selected.label, style = typo().bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(WaveIcons.KeyboardArrowDown, null, tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp).rotate(chevronRotation))
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(220)) + fadeOut(tween(150)),
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                BoxWithConstraints(Modifier.fillMaxWidth().selectableGroup()) {
                    val density = LocalDensity.current
                    val fontScale = density.fontScale.coerceAtLeast(1f)
                    val textMeasurer = rememberTextMeasurer()
                    val widestWord = with(density) {
                        choices.flatMap { it.label.split(' ') }.maxOf { word ->
                            textMeasurer.measure(word, style = typo().bodySmall, maxLines = 1).size.width.toDp()
                        }
                    }
                    val minimumTileWidth = maxOf(68.dp * fontScale, widestWord + 16.dp)
                    val columns = ((maxWidth + 8.dp) / (minimumTileWidth + 8.dp)).toInt().coerceIn(2, 4)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        choices.chunked(columns).forEach { row ->
                            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { choice ->
                                    BackgroundChoiceTile(
                                        choice = choice,
                                        selected = choice.id == selected.id,
                                        onClick = { onSelectPreset(choice.id) },
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
private fun BackgroundChoiceTile(
    choice: BackgroundChoice,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
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
        BackgroundSwatch(choice, selected, Modifier.size(32.dp))
        Spacer(Modifier.height(6.dp))
        Text(choice.label, style = typo().bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurface,
            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BackgroundSwatch(choice: BackgroundChoice, selected: Boolean, modifier: Modifier) {
    val shape = CircleShape
    val fill = if (choice.id == DataStoreManager.APP_BACKGROUND_AURORA) {
        Brush.linearGradient(listOf(Color(0xFF963DE0), Color(0xFF2856E8), Color(0xFF00A99D)))
    } else {
        Brush.linearGradient(listOf(choice.swatch, choice.swatch))
    }
    Box(modifier.clip(shape).background(fill).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f), shape),
        contentAlignment = Alignment.Center) {
        if (selected) {
            Icon(WaveIcons.Check, null,
                tint = if (choice.swatch.luminance() > 0.45f) Color.Black else Color.White,
                modifier = Modifier.size(22.dp))
        }
    }
}
