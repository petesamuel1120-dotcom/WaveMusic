package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.ui.icon.Add
import com.petedev.wavemusic.ui.icon.Remove
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.decrease_lyrics_offset
import com.petedev.wavemusic.resources.increase_lyrics_offset
import com.petedev.wavemusic.resources.lyrics_offset
import com.petedev.wavemusic.resources.lyrics_offset_seconds_value
import com.petedev.wavemusic.resources.lyrics_offset_step_hint
import com.petedev.wavemusic.resources.lyrics_offset_default
import com.petedev.wavemusic.resources.lyrics_offset_direction_hint
import kotlin.math.abs

internal const val LYRICS_OFFSET_STEP_MS = 500

/** Preserve offsets previously entered in milliseconds without rounding them to a half-second. */
internal fun lyricsOffsetSecondsValue(offsetMs: Int): String {
    val magnitude = abs(offsetMs.toLong())
    val sign = when {
        offsetMs > 0 -> "+"
        offsetMs < 0 -> "-"
        else -> ""
    }
    val fraction = (magnitude % 1000).toString().padStart(3, '0').trimEnd('0').ifEmpty { "0" }
    return "$sign${magnitude / 1000}.$fraction"
}

/** Shared, live half-second adjustment for the player menu and Lyrics settings. */
@Composable
internal fun LyricsOffsetControl(
    offsetMs: Int?,
    onAdjustOffset: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showHint: Boolean = false,
    onDefault: (() -> Unit)? = null,
    scopeHint: String? = null,
) {
    val colors = rememberSurfaceDarkColors()
    val offsetText = offsetMs?.let { stringResource(Res.string.lyrics_offset_seconds_value, lyricsOffsetSecondsValue(it)) } ?: "—"
    val canDecrease = offsetMs != null && offsetMs.toLong() - LYRICS_OFFSET_STEP_MS >= Int.MIN_VALUE
    val canIncrease = offsetMs != null && offsetMs.toLong() + LYRICS_OFFSET_STEP_MS <= Int.MAX_VALUE
    Column(
        modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.lyrics_offset), modifier = Modifier.weight(1f), style = typo().bodyMedium, color = colors.content)
            if (onDefault != null) {
                TextButton(onClick = onDefault, enabled = offsetMs != null) {
                    Text(stringResource(Res.string.lyrics_offset_default), color = if (offsetMs == null) colors.disabled else colors.content)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(colors.content.copy(alpha = 0.06f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onAdjustOffset(-LYRICS_OFFSET_STEP_MS) },
                enabled = canDecrease,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(WaveIcons.Remove, stringResource(Res.string.decrease_lyrics_offset), tint = if (canDecrease) colors.content else colors.disabled)
            }
            Text(
                offsetText,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                style = typo().bodyLarge.copy(textDirection = TextDirection.Ltr),
                color = colors.content,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            IconButton(
                onClick = { onAdjustOffset(LYRICS_OFFSET_STEP_MS) },
                enabled = canIncrease,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(WaveIcons.Add, stringResource(Res.string.increase_lyrics_offset), tint = if (canIncrease) colors.content else colors.disabled)
            }
        }
        if (scopeHint != null) {
            Text(scopeHint, style = typo().bodySmall, color = colors.subtitle)
            Text(stringResource(Res.string.lyrics_offset_direction_hint), style = typo().bodySmall, color = colors.subtitle)
        }
        if (showHint) {
            Text(stringResource(Res.string.lyrics_offset_step_hint), style = typo().bodySmall, color = colors.subtitle)
        }
    }
}
