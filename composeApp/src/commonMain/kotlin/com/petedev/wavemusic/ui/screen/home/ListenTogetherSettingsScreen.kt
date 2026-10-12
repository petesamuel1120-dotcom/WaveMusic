package com.petedev.wavemusic.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import com.petedev.wavemusic.ui.component.SettingsTheme
import com.petedev.wavemusic.ui.component.SettingsSection
import com.petedev.wavemusic.ui.component.SettingsNested
import com.petedev.wavemusic.ui.component.SettingItem
import com.petedev.wavemusic.ui.component.auroraGradientBackground
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.ui.icon.Sync
import com.petedev.wavemusic.ui.icon.Groups
import com.petedev.wavemusic.ui.icon.PeopleAlt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.LiquidGlassIconButton
import com.petedev.wavemusic.ui.component.rememberSettingsHeaderScrollOffset
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.Check
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.ListenTogetherSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.listen_together
import com.petedev.wavemusic.resources.lt_as_host
import com.petedev.wavemusic.resources.lt_auto_approve_joins
import com.petedev.wavemusic.resources.lt_auto_approve_joins_desc
import com.petedev.wavemusic.resources.lt_blocked
import com.petedev.wavemusic.resources.lt_blocked_empty
import com.petedev.wavemusic.resources.lt_custom_server
import com.petedev.wavemusic.resources.lt_custom_server_desc
import com.petedev.wavemusic.resources.lt_default_server_location
import com.petedev.wavemusic.resources.lt_default_server_name
import com.petedev.wavemusic.resources.lt_save_server
import com.petedev.wavemusic.resources.lt_server
import com.petedev.wavemusic.resources.lt_unblock

/** Uses the same comfortable content width as the other settings pages. */
private const val CONTENT_MAX_WIDTH_DP = 720

/**
 * Listen Together settings, grouped into server, hosting, and blocked listeners.
 *
 * The blocklist is **client-side and by name**. The protocol has `kick_user` but no ban, and the
 * server hands out a fresh `user_<nanotime>_<rand>` id on every connection, so there is no stable
 * identity to block on — a name is the only thing that persists across reconnects, and a determined
 * person can change it. It is a convenience, not a security control.
 */
@Composable
fun ListenTogetherSettingsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: ListenTogetherSettingsViewModel = koinViewModel(),
) {
    val usingCustom by viewModel.usingCustomServer.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val autoJoins by viewModel.autoApproveJoins.collectAsStateWithLifecycle()
    val blocked by viewModel.blockedNames.collectAsStateWithLifecycle()

    var draftUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    val backdrop = rememberBackdrop(MaterialTheme.colorScheme.background)
    val scrollState = rememberScrollState()
    val topInset = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
    val headerOffset = rememberSettingsHeaderScrollOffset(
        headerHeight = topInset + 52.dp,
        sharedHeader = false,
    ) { 0 to scrollState.value }

    // See ListenTogetherScreen: measure the space actually given, not the window.
    SettingsTheme {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val contentWidth = minOf(maxWidth.value, CONTENT_MAX_WIDTH_DP.toFloat()).dp

        val bg = MaterialTheme.colorScheme.background
        // The background is captured separately from the floating back button.
        Box(
            Modifier.matchParentSize().frostedHeaderSource(backdrop)
                .background(bg).auroraGradientBackground(bg.luminance() > 0.5f),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Full-width header — see ListenTogetherScreen; only the content below is capped.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(48.dp)
                        .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // The back button floats outside the content so it can follow the same hide
                // and reveal behavior as other settings controls. These spacers center the title.
                Spacer(Modifier.width(48.dp))
                Text(
                    text = stringResource(Res.string.listen_together),
                    style = typo().titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(48.dp))
            }

            // Only the content is capped; the header above stays full width.
            Column(
                modifier = Modifier.width(contentWidth).padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                SettingsSection(
                    title = stringResource(Res.string.lt_server),
                    icon = WaveIcons.Sync,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ServerOption(
                            title = stringResource(Res.string.lt_default_server_name),
                            subtitle = stringResource(Res.string.lt_default_server_location),
                            selected = !usingCustom,
                            onClick = { viewModel.useDefaultServer() },
                        )
                        ServerOption(
                            title = stringResource(Res.string.lt_custom_server),
                            subtitle = stringResource(Res.string.lt_custom_server_desc),
                            selected = usingCustom,
                            onClick = { if (!usingCustom) viewModel.setServerUrl(draftUrl.ifBlank { "wss://" }) },
                        )
                    }
                    AnimatedVisibility(visible = usingCustom) {
                        SettingsNested {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                BasicTextField(
                                    value = draftUrl,
                                    onValueChange = { draftUrl = it },
                                    singleLine = true,
                                    textStyle = typo().bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f), RoundedCornerShape(14.dp)),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                                            contentAlignment = Alignment.CenterStart,
                                        ) {
                                            if (draftUrl.isEmpty()) {
                                                Text("wss://…", style = typo().bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            inner()
                                        }
                                    },
                                )
                                // Committing on focus loss would silently store a half-typed address, so the value
                                // is only written when the user asks for it.
                                if (draftUrl != serverUrl) {
                                    SmallAction(text = stringResource(Res.string.lt_save_server)) { viewModel.setServerUrl(draftUrl) }
                                }
                            }
                        }
                    }
                }

                SettingsSection(
                    title = stringResource(Res.string.lt_as_host),
                    icon = WaveIcons.Groups,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.lt_auto_approve_joins),
                        subtitle = stringResource(Res.string.lt_auto_approve_joins_desc),
                        switch = autoJoins to { viewModel.setAutoApproveJoins(it) },
                    )
                }

                SettingsSection(
                    title = "${stringResource(Res.string.lt_blocked)} · ${blocked.size}",
                    icon = WaveIcons.PeopleAlt,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (blocked.isEmpty()) {
                            Text(
                                stringResource(Res.string.lt_blocked_empty),
                                style = typo().bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            blocked.forEach { name ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(
                                                    40.dp,
                                                ).clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            name.trim().firstOrNull()?.uppercase() ?: "?",
                                            style = typo().titleSmall,
                                            color = MaterialTheme.colorScheme.surface,
                                        )
                                    }
                                    Text(
                                        name,
                                        style = typo().bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    SmallAction(text = stringResource(Res.string.lt_unblock)) { viewModel.unblock(name) }
                                }
                            }
                        }
                    }
                }

                EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
            }
        }

        // Floats over the strip the header row reserves — a sibling of both the scroll column and
        // the backdrop source, exactly the arrangement the main Listen Together page uses.
        LiquidGlassIconButton(
            backdrop = backdrop,
            imageVector = WaveIcons.ArrowBackIosNew,
            tint = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(24.dp),
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer { translationY = headerOffset.value }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 12.dp, top = 4.dp)
                    .size(48.dp),
        ) {
            navController.navigateUp()
        }
    }
    }
}

@Composable
private fun ServerOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)) else Modifier)
                .border(
                    1.dp,
                    if (selected) {
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.40f,
                        )
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f)
                    },
                    RoundedCornerShape(16.dp),
                ).clickable { onClick() }
                .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) {
                            Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.26f), CircleShape)
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(WaveIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = typo().titleSmall,
                color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(3.dp))
            Text(subtitle, style = typo().bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SmallAction(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f), RoundedCornerShape(15.dp))
                .clickable { onClick() }
                .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = typo().labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
