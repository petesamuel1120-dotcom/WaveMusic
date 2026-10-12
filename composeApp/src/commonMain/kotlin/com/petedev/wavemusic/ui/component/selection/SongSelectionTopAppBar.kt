package com.petedev.wavemusic.ui.component.selection

import com.petedev.wavemusic.ui.component.CompactHeaderContentHeight
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.petedev.wavemusic.ui.component.PageTopAppBar
import com.petedev.wavemusic.ui.component.liquidGlass
import com.petedev.wavemusic.expect.ui.BackHandler
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.ui.component.SharedHeaderContentHeight
import com.petedev.wavemusic.ui.component.LocalSharedHeaderHosted
import com.petedev.wavemusic.ui.icon.Close
import com.petedev.wavemusic.ui.icon.MoreVert
import com.petedev.wavemusic.ui.icon.SelectAll
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.LocalAppColorScheme
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalForceDarkText
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.typo
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.exit_selection
import com.petedev.wavemusic.resources.n_selected
import com.petedev.wavemusic.resources.select_all

/**
 * Replaces a screen's own top bar while [state] is active.
 *
 * [onSelectAll] hands over the ids the screen is currently showing, in display order —
 * [SongSelectionState.toggleSelectAll] selects downward from the selected song, up to
 * [MAX_SONG_SELECTION] songs including that starting row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongSelectionTopAppBar(
    state: SongSelectionState,
    onSelectAll: () -> Unit,
    onOpenActions: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: PlatformBackdrop? = null,
    // Reserve system insets outside the toolbar's painted surface. Hosted headers and callers
    // that already reserve the status bar use WindowInsets(0).
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    // The selection toolbar owns a completed system Back while selection mode is active.
    // Exiting clears every selected ID and disables this handler for the next Back.
    BackHandler(enabled = state.isActive) { state.exit() }

    val appColorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val forceDarkText = LocalForceDarkText.current
    val isDarkChrome = LocalAppIsDarkTheme.current || forceDarkText
    val resolvedContainerColor =
        when {
            LocalAppIsDarkTheme.current -> Color.Black
            forceDarkText -> Color.Black.copy(alpha = 0.82f)
            else -> appColorScheme.surface
        }
    val barShape = RoundedCornerShape(28.dp)
    val borderColor =
        if (forceDarkText) Color.White.copy(alpha = 0.14f)
        else appColorScheme.outlineVariant.copy(alpha = 0.6f)
    val resolvedContentColor = if (isDarkChrome) Color.White else appColorScheme.onSurface
    val glassBackdrop = backdrop?.takeIf { LocalLiquidGlassEnabled.current }
    // Keep even caller-supplied blur/background modifiers below the status bar. Passing these
    // insets to TopAppBar itself would paint its container behind the system icons.
    Box(
        Modifier.windowInsetsPadding(if (LocalSharedHeaderHosted.current) WindowInsets(0) else windowInsets),
    ) {
        PageTopAppBar(
            controlContainersEnabled = false,
            modifier =
                modifier
                    .padding(horizontal = 12.dp)
                    // Selection mode replaces the playlist header. Keep the replacement on the
                    // same page backdrop so entering selection cannot turn the glass chrome into
                    // a flat Material surface.
                    .then(
                        glassBackdrop?.let {
                            Modifier.liquidGlass(
                                backdrop = it,
                                shape = barShape,
                                interactive = false,
                            )
                        } ?: Modifier,
                    )
                    .clip(barShape)
                    .border(BorderStroke(1.dp, borderColor), barShape),
            windowInsets = WindowInsets(0),
            contentHeight = if (LocalSharedHeaderHosted.current) SharedHeaderContentHeight else CompactHeaderContentHeight,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = if (glassBackdrop != null) Color.Transparent else resolvedContainerColor,
                    titleContentColor = resolvedContentColor,
                    navigationIconContentColor = resolvedContentColor,
                    actionIconContentColor = resolvedContentColor,
                ),
            navigationIcon = {
                IconButton(onClick = { state.exit() }) {
                    Icon(
                        imageVector = WaveIcons.Close,
                        contentDescription = stringResource(Res.string.exit_selection),
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(Res.string.n_selected, state.count),
                    style = typo().titleMedium,
                    color = resolvedContentColor,
                )
            },
            actions = {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = WaveIcons.SelectAll,
                        contentDescription = stringResource(Res.string.select_all),
                    )
                }
                // Select-all doubles as deselect-all, so the count can be 0 while still selecting.
                // There is nothing to act on then, and an empty sheet is worse than no sheet.
                if (state.count > 0) {
                    IconButton(onClick = onOpenActions) {
                        Icon(
                            imageVector = WaveIcons.MoreVert,
                            contentDescription = null,
                        )
                    }
                }
            },
        )
        }
}
