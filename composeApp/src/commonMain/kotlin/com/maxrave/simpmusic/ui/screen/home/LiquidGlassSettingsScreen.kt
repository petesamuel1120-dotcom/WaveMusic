package com.maxrave.simpmusic.ui.screen.home

import com.maxrave.simpmusic.ui.component.pageContentHorizontalInset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxrave.domain.manager.LiquidGlassIntensity
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.FrostedHeader
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderState
import com.maxrave.simpmusic.ui.component.rememberSettingsHeaderScrollOffset
import com.maxrave.simpmusic.ui.component.LiquidGlassContainer
import com.maxrave.simpmusic.ui.component.LocalPageAuroraBackdrop
import com.maxrave.simpmusic.ui.component.PageTopAppBar
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.SettingsTheme
import com.maxrave.simpmusic.ui.component.auroraGradientBackground
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.component.pageHeaderContentHeight
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.kyant.backdrop.drawPlainBackdrop
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.liquid_glass_enable_to_apply
import simpmusic.composeapp.generated.resources.liquid_glass_frosted
import simpmusic.composeapp.generated.resources.liquid_glass_intensity
import simpmusic.composeapp.generated.resources.liquid_glass_intensity_description
import simpmusic.composeapp.generated.resources.liquid_glass_liquid
import simpmusic.composeapp.generated.resources.liquid_glass_reset
import simpmusic.composeapp.generated.resources.preview

/** The illustration and controls sample background-only layers, never their own glass. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassSettingsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: SharedViewModel = koinInject(),
) {
    val intensity by viewModel.liquidGlassIntensity.collectAsStateWithLifecycle()
    val glassEnabled = LocalLiquidGlassEnabled.current
    val isDark = LocalAppIsDarkTheme.current
    val headerBackdrop = rememberBackdrop(MaterialTheme.colorScheme.background)
    val pageAurora = LocalPageAuroraBackdrop.current
    val headerHaze = rememberHazeState()
    val density = LocalDensity.current
    val topInset = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val headerHeight = pageHeaderContentHeight()
    val scrollState = rememberScrollState()
    val headerHost = LocalSharedHeaderState.current
    val headerOffset = rememberSettingsHeaderScrollOffset(headerHeight = topInset + headerHeight) {
        0 to scrollState.value
    }
    val title = stringResource(Res.string.liquid_glass_intensity)
    var pendingChange by remember { mutableStateOf(false) }
    val currentSave by rememberUpdatedState({ viewModel.saveLiquidGlassIntensity() })
    DisposableEffect(viewModel) {
        onDispose { if (pendingChange) currentSave() }
    }

    SettingsTheme {
        val colors = MaterialTheme.colorScheme
        Box(Modifier.fillMaxSize().background(colors.background)) {
            Box(Modifier.fillMaxSize().frostedHeaderSource(headerBackdrop)) {
                Box(
                    Modifier.matchParentSize().then(
                        if (pageAurora != null) {
                            Modifier.drawPlainBackdrop(pageAurora, shape = { RectangleShape }, effects = {})
                        } else {
                            Modifier.background(colors.background).auroraGradientBackground(!isDark)
                        },
                    ),
                )
                Column(
                    Modifier.fillMaxSize().verticalScroll(scrollState).padding(innerPadding)
                        .padding(horizontal = pageContentHorizontalInset(16.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(headerHeight + 16.dp))
                    Column(
                        Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Text(
                            stringResource(Res.string.liquid_glass_intensity_description),
                            style = typo().bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        // Keep a directly opened page inactive while the effect is off.
                        Column(
                            Modifier.fillMaxWidth().alpha(if (glassEnabled) 1f else 0.5f),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            GlassIllustration(isDark)
                            Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                    .background(colors.surfaceContainer).padding(20.dp),
                            ) {
                                GlassIntensitySlider(
                                    value = intensity,
                                    label = title,
                                    enabled = glassEnabled,
                                    onValueChange = {
                                        pendingChange = true
                                        viewModel.previewLiquidGlassIntensity(it)
                                    },
                                    onValueChangeFinished = {
                                        pendingChange = false
                                        viewModel.saveLiquidGlassIntensity()
                                    },
                                )
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    GlassEndpointIcon(filled = !isDark, label = stringResource(Res.string.liquid_glass_frosted))
                                    GlassEndpointIcon(filled = isDark, label = stringResource(Res.string.liquid_glass_liquid))
                                }
                            }
                        }
                        if (!glassEnabled) {
                            Text(
                                stringResource(Res.string.liquid_glass_enable_to_apply),
                                style = typo().bodyMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                        TextButton(
                            enabled = glassEnabled,
                            onClick = {
                                pendingChange = false
                                viewModel.previewLiquidGlassIntensity(LiquidGlassIntensity.Default)
                                viewModel.saveLiquidGlassIntensity()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(stringResource(Res.string.liquid_glass_reset))
                        }
                        EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
                    }
                }
            }
            FrostedHeader(
                hazeState = headerHaze,
                backdrop = headerBackdrop,
                shareHeader = true,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(topInset + headerHeight)
                    .graphicsLayer { translationY = if (headerHost == null) headerOffset.value else 0f },
                tint = colors.background,
            ) {
                PageTopAppBar(
                    title = { Text(title, style = typo().titleMedium) },
                    navigationIcon = {
                        RippleIconButton(SimpIcons.ArrowBackIosNew, tint = colors.onSurface) { navController.navigateUp() }
                    },
                    windowInsets = WindowInsets(0),
                    modifier = Modifier.align(Alignment.BottomCenter),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

/** Overlapping panes mirror the reference while retaining spoken endpoint labels. */
@Composable
private fun GlassEndpointIcon(filled: Boolean, label: String) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val background = MaterialTheme.colorScheme.surfaceContainer
    Canvas(Modifier.size(width = 32.dp, height = 26.dp).semantics { contentDescription = label }) {
        val backPosition = Offset(2.dp.toPx(), 2.dp.toPx())
        val frontPosition = Offset(9.dp.toPx(), 8.dp.toPx())
        val paneSize = Size(20.dp.toPx(), 16.dp.toPx())
        val backRadius = CornerRadius(4.dp.toPx())
        val frontRadius = CornerRadius(7.dp.toPx())
        val outline = Stroke(1.8.dp.toPx())
        if (filled) {
            drawRoundRect(color.copy(alpha = 0.65f), backPosition, paneSize, backRadius)
        } else {
            drawRoundRect(color, backPosition, paneSize, backRadius, style = outline)
        }
        // Separate the two panes cleanly where they overlap.
        drawRoundRect(background, frontPosition, paneSize, frontRadius)
        if (filled) {
            drawRoundRect(background, frontPosition, paneSize, frontRadius, style = Stroke(3.dp.toPx()))
            drawRoundRect(color, frontPosition, paneSize, frontRadius)
        } else {
            drawRoundRect(color, frontPosition, paneSize, frontRadius, style = outline)
        }
    }
}

@Composable
private fun GlassIllustration(isDark: Boolean) {
    val backdrop = rememberBackdrop(MaterialTheme.colorScheme.background)
    Box(
        Modifier.fillMaxWidth().height(248.dp).clip(RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center,
    ) {
        // Colored bands and fine lines make the difference between frost and refraction visible.
        Canvas(Modifier.matchParentSize().layerBackdrop(backdrop)) {
            drawRect(
                Brush.linearGradient(
                    if (isDark) listOf(Color(0xFF072D3A), Color(0xFF443B83), Color(0xFF963A65))
                    else listOf(Color(0xFFAFE5E2), Color(0xFFD7C9F2), Color(0xFFF7C1B4)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFF74E5D8).copy(alpha = 0.85f), Color.Transparent), center = Offset(size.width * 0.15f, size.height * 0.3f), radius = size.width * 0.65f),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.15f, size.height * 0.3f),
            )
            for (i in -3..10) {
                val x = size.width * i / 8f
                drawLine(Color.White.copy(alpha = 0.22f), Offset(x, 0f), Offset(x + size.height * 0.6f, size.height), strokeWidth = 1.dp.toPx())
            }
            drawLine(Color(0xFFFFDEB3).copy(alpha = 0.6f), Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.1f), strokeWidth = 20.dp.toPx())
        }
        LiquidGlassContainer(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(0.64f).height(76.dp),
            shape = RoundedCornerShape(percent = 50),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(SimpIcons.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(stringResource(Res.string.preview), style = typo().titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassIntensitySlider(
    value: Float,
    label: String,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val dragging by interaction.collectIsDraggedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val appearance = LocalLiquidGlassAppearance.current
    val scale by animateFloatAsState(
        targetValue = if (dragging || pressed) 1f + 0.12f * appearance.motionMultiplier else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 350f),
        label = "glass_slider_thumb",
    )
    val colors = MaterialTheme.colorScheme
    val backdrop = rememberBackdrop(colors.surfaceContainer)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
        // Capture the track and its background separately, so the thumb can refract the colored line.
        Canvas(Modifier.matchParentSize().layerBackdrop(backdrop)) {
            drawRect(colors.surfaceContainer)
            val inset = 26.dp.toPx()
            val start = Offset(if (isRtl) size.width - inset else inset, center.y)
            val end = Offset(if (isRtl) inset else size.width - inset, center.y)
            drawLine(colors.onSurfaceVariant.copy(alpha = 0.18f), start, end, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            val fraction = LiquidGlassIntensity.normalize(value)
            if (fraction > 0f) {
                drawLine(colors.primary, start, Offset(start.x + (end.x - start.x) * fraction, center.y), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Slider(
            value = value,
            enabled = enabled,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..1f,
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().height(64.dp).semantics { contentDescription = label },
            thumb = {
                Box(
                    Modifier.size(width = 52.dp, height = 32.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .liquidGlass(backdrop, shape = RoundedCornerShape(percent = 50), interactive = false),
                )
            },
            track = { Box(Modifier.fillMaxWidth().height(6.dp)) },
        )
    }
}
