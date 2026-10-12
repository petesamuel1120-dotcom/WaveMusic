package com.petedev.wavemusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ComponentOverrideApi
import androidx.compose.material3.LocalBasicAlertDialogOverride
import androidx.compose.material3.DefaultBasicAlertDialogOverride
import com.petedev.wavemusic.ui.component.AnimatedAppAlertDialogOverride
import com.petedev.wavemusic.ui.component.DialogOriginTracker
import com.petedev.wavemusic.ui.component.LocalDialogOrigins
import com.petedev.wavemusic.ui.component.AppBackgroundPalette
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import com.petedev.wavemusic.domain.manager.LiquidGlassIntensity
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.expect.ui.SystemBarAppearanceEffect

/**
 * Semantic colors that sit outside the Material 3 ColorScheme.
 * Read them via [LocalAppColors] inside composables.
 */
@Immutable
data class AppColors(
    val favorite: Color,
    val lyricActive: Color,
    val shimmerBackground: Color,
    val shimmerLine: Color,
    val overlay: Color,
    val overlayHeavy: Color,
)

private val DarkAppColors =
    AppColors(
        favorite = favoriteColor,
        lyricActive = lyricActiveColor,
        shimmerBackground = shimmerBackground,
        shimmerLine = shimmerLine,
        overlay = overlay,
        overlayHeavy = blackMoreOverlay,
    )

// Overlays stay dark in both themes: they cover artwork, where content is always light.
private val LightAppColors =
    DarkAppColors.copy(
        shimmerBackground = shimmerBackgroundLight,
        shimmerLine = shimmerLineLight,
    )

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/** True in dark theme. Provided by [AppTheme] so platform glass (blur/tint) can adapt reliably. */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/** App-level theme values remain stable inside immersive dark player/detail subtrees. */
val LocalAppIsDarkTheme = staticCompositionLocalOf { true }
val LocalAppColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

/**
 * Whether liquid-glass surfaces may actually draw glass.
 *
 * Provided by [AppTheme] from the user's setting; the default is true so previews and anything
 * composed outside [AppTheme] keep today's look. The setting row is Android-only, so Desktop always
 * provides true — its capsule player and detail-screen buttons are glass by design, with no switch.
 */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }

/** Whether built-in dialog menus use the app's expanding motion and backdrop blur. */
val LocalAdvancedDialogAnimations = staticCompositionLocalOf { false }

/** Whether visual effects such as glass, player ambience, and page motion are reduced. */
val LocalReduceVisualEffects = staticCompositionLocalOf { false }

/** Dynamic local: live slider changes invalidate only consumers of the glass material. */
val LocalLiquidGlassAppearance = compositionLocalOf {
    LiquidGlassAppearance.fromIntensity(LiquidGlassIntensity.Default)
}

/**
 * The dark scheme to use for immersive screens while the app itself is on the light theme.
 * Provided by [AppTheme], consumed by [ForceDarkContent]; null only outside of [AppTheme].
 */
val LocalForcedDarkColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

/** Parses "RRGGBB" or "AARRGGBB" (optionally "#"-prefixed) into a [Color]; null if malformed. */
fun parseThemeColorHex(hex: String): Color? {
    val clean = hex.trim().removePrefix("#")
    val argb =
        when (clean.length) {
            6 -> "FF$clean"
            8 -> clean
            else -> return null
        }
    return argb.toLongOrNull(16)?.let { Color(it) }
}

/**
 * Neutral surfaces for the light theme, on a pure neutral-grey ramp (R=G=B, no seed tint);
 * primary/secondary/tertiary stay seed-derived. The page background is #FAFAFA (neutral tone 98,
 * the Material 3 stance) rather than pure white: a full-bleed #FFFFFF expanse glares on a large
 * desktop window, and even Apple — who pins systemBackground to white — puts #F2F2F7 behind
 * list pages. Pure white is kept for surfaceBright/surfaceContainerLowest so cards and sheets
 * still have a brighter-than-page tier to lift onto. Dark stays pure-black AMOLED: black does
 * not glare and saves OLED.
 */
private fun ColorScheme.withNeutralLightSurfaces(): ColorScheme =
    copy(
        background = Color(0xFFFAFAFA),
        onBackground = Color.Black,
        surface = Color(0xFFFAFAFA),
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFE2E2E2),
        onSurfaceVariant = Color(0xFF333333),
        surfaceTint = primary,
        surfaceBright = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFDADADA),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F7F7),
        surfaceContainer = Color(0xFFF1F1F1),
        surfaceContainerHigh = Color(0xFFECECEC),
        surfaceContainerHighest = Color(0xFFE6E6E6),
        outline = Color(0xFF777777),
        outlineVariant = Color(0xFFC7C7C7),
        inverseSurface = Color(0xFF303030),
        inverseOnSurface = Color(0xFFF1F1F1),
    )

/** Keep foregrounds legible on every light palette. */
private fun ColorScheme.withReadableLightForegrounds(): ColorScheme =
    copy(
        onBackground = Color.Black,
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFF333333),
    )

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ComponentOverrideApi::class,
)
@Composable
fun AppTheme(
    themeMode: String = DataStoreManager.THEME_MODE_SYSTEM,
    themeColorSource: String = DataStoreManager.THEME_COLOR_DEFAULT,
    customThemeColor: Color? = null,
    appBackgroundPreset: String = DataStoreManager.APP_BACKGROUND_AURORA,
    liquidGlassEnabled: Boolean = true,
    liquidGlassIntensity: Float = LiquidGlassIntensity.Default,
    reduceVisualEffectsEnabled: Boolean = false,
    advancedDialogAnimationsEnabled: Boolean = false,
    forceWhiteStatusBarIcons: Boolean = false,
    transparentStatusBar: Boolean = false,
    content:
        @Composable()
        () -> Unit,
) {
    val isDark = isDarkTheme(themeMode)
    val dialogOrigins = remember { DialogOriginTracker() }
    val effectiveLiquidGlassEnabled = liquidGlassEnabled && !reduceVisualEffectsEnabled
    val effectiveAdvancedDialogAnimationsEnabled = advancedDialogAnimationsEnabled && !reduceVisualEffectsEnabled
    val glassAppearance = remember(liquidGlassIntensity) {
        LiquidGlassAppearance.fromIntensity(liquidGlassIntensity)
    }
    val selectedSeedColor =
        if (themeColorSource == DataStoreManager.THEME_COLOR_CUSTOM) {
            customThemeColor ?: seed
        } else {
            seed
        }
    val seedColor = if (isDark) selectedSeedColor.mutedForDarkTheme() else selectedSeedColor
    // Symmetric base: dark pins background/surface to pure black via isAmoled; light pins them to
    // pure white with a neutral-grey ramp (the seed otherwise tints the light neutrals warm/cream).
    val generatedColorScheme =
        rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = isDark,
            isAmoled = isDark,
            style = PaletteStyle.TonalSpot,
            modifyColorScheme = { cs -> if (isDark) cs else cs.withNeutralLightSurfaces() },
        ).let { scheme -> if (isDark) scheme else scheme.withReadableLightForegrounds() }
    // Keep the background picker coordinated with Material surfaces. Aurora retains the existing
    // scheme; each solid preset gets a matching elevation ramp so buttons and cards remain visible
    // on both the near-black Onyx option and the tinted dark palettes.
    val colorScheme =
        AppBackgroundPalette.colorFor(appBackgroundPreset, isLightTheme = !isDark)?.let { background ->
            generatedColorScheme.withBackgroundPresetSurfaces(
                background = background,
                isDark = isDark,
                isOnyx = appBackgroundPreset == DataStoreManager.APP_BACKGROUND_AMOLED_BLACK,
            )
        } ?: generatedColorScheme
    // ForceDarkContent subtrees stay dark even at light theme. Resolve their scheme once here
    // instead of letting every such subtree build a palette of its own.
    val forcedDarkScheme =
        if (isDark) {
            colorScheme
        } else {
            rememberDynamicColorScheme(
                seedColor = seedColor.mutedForDarkTheme(),
                isDark = true,
                isAmoled = true,
                style = PaletteStyle.TonalSpot,
            )
        }
    SystemBarAppearanceEffect(
        isDark = isDark,
        statusBarColor = if (forceWhiteStatusBarIcons || (transparentStatusBar && effectiveLiquidGlassEnabled)) {
            Color.Transparent
        } else {
            colorScheme.background
        },
        forceWhiteStatusBarIcons = forceWhiteStatusBarIcons,
    )
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides SoftRippleConfiguration,
                LocalDialogOrigins provides dialogOrigins,
                LocalBasicAlertDialogOverride provides if (effectiveAdvancedDialogAnimationsEnabled) {
                    AnimatedAppAlertDialogOverride
                } else {
                    DefaultBasicAlertDialogOverride
                },
                LocalAdvancedDialogAnimations provides effectiveAdvancedDialogAnimationsEnabled,
                LocalReduceVisualEffects provides reduceVisualEffectsEnabled,
                LocalContentColor provides colorScheme.onSurfaceVariant,
                LocalAppColors provides if (isDark) DarkAppColors else LightAppColors,
                LocalIsDarkTheme provides isDark,
                LocalAppIsDarkTheme provides isDark,
                LocalAppColorScheme provides colorScheme,
                LocalForcedDarkColorScheme provides forcedDarkScheme,
                LocalLiquidGlassEnabled provides effectiveLiquidGlassEnabled,
                LocalLiquidGlassAppearance provides glassAppearance,
                content = content,
            )
        },
        typography = typo(colorScheme),
    )
}

private fun ColorScheme.withBackgroundPresetSurfaces(
    background: Color,
    isDark: Boolean,
    isOnyx: Boolean,
): ColorScheme {
    val endpoint = if (isDark) Color.White else Color.Black
    fun tone(amount: Float): Color = androidx.compose.ui.graphics.lerp(background, endpoint, amount)
    fun darkTone(amount: Float): Float = if (isOnyx) amount + 0.025f else amount
    val primaryText = if (isDark) Color.White else Color(0xFF111111)
    val secondaryText = if (isDark) Color(0xFFD0D0D0) else Color(0xFF454545)
    return copy(
        background = background,
        onBackground = primaryText,
        surface = background,
        onSurface = primaryText,
        surfaceVariant = tone(if (isDark) 0.06f else 0.055f),
        onSurfaceVariant = secondaryText,
        surfaceBright = tone(if (isDark) darkTone(0.18f) else 0f),
        surfaceDim = tone(if (isDark) 0f else 0.12f),
        surfaceContainerLowest = tone(if (isDark) darkTone(0.01f) else 0f),
        surfaceContainerLow = tone(if (isDark) darkTone(0.035f) else 0.025f),
        surfaceContainer = tone(if (isDark) darkTone(0.065f) else 0.05f),
        surfaceContainerHigh = tone(if (isDark) darkTone(0.095f) else 0.075f),
        surfaceContainerHighest = tone(if (isDark) darkTone(0.12f) else 0.10f),
        outline = tone(if (isDark) darkTone(0.24f) else 0.36f),
        outlineVariant = tone(if (isDark) darkTone(0.16f) else 0.22f),
        inverseSurface = if (isDark) Color(0xFFE6E6E6) else Color(0xFF303030),
        inverseOnSurface = if (isDark) Color(0xFF303030) else Color(0xFFF5F5F5),
    )
}

/**
 * A controlled ripple applied app-wide. The press state is intentionally stronger so dialog and
 * menu rows remain visibly interactive on both black and light surfaces.
 *
 * Keep hover/focus restrained, but let a direct press read clearly. The unspecified color keeps
 * the indication in contrast with whichever theme or artwork-backed surface is underneath.
 *
 * The colour is left unspecified on purpose, so the ripple keeps deriving from LocalContentColor
 * and stays correct inside [ForceDarkContent] subtrees, which run a different scheme.
 *
 * This reaches bare `Modifier.clickable` too, not just Material components: MaterialTheme
 * already provides `material3.ripple()` as LocalIndication, and that is the one indication that
 * reads this configuration. Nothing extra has to be provided for it.
 *
 * The (color, rippleAlpha) constructor is deprecated in favour of one that also takes a `focus`,
 * but that three-argument version is internal to material3, and the two public replacements —
 * (color) and (focus, color) — drop rippleAlpha entirely. Adjusting ripple alpha therefore has
 * no non-deprecated path in this version, so the warning is suppressed rather than designed
 * around. Revisit once material3 opens up the full constructor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
private val SoftRippleConfiguration =
    RippleConfiguration(
        // Colour is deliberately left alone. Ripple derives it from LocalContentColor, which is
        // light on the dark theme and dark on the light one — always contrasting with what it
        // covers. Pinning a dark grey here instead made the ripple paint *darker* than the black
        // surface underneath, so a tap read as a sooty smudge rather than a highlight.
        // Alpha is the only lever that softens without changing which way the contrast runs.
        rippleAlpha =
            RippleAlpha(
                draggedAlpha = 0.06f,
                focusedAlpha = 0.04f,
                hoveredAlpha = 0.03f,
                // A stronger press state makes full-width dialog/menu rows register clearly,
                // especially over bright artwork and AMOLED black.
                pressedAlpha = 0.22f,
            ),
    )

/**
 * Wraps immersive screens — album, playlist, local playlist, podcast and the players — which
 * always draw over dark artwork and must therefore render dark even on the light theme.
 *
 * [LocalForceDarkText] alone is not enough: it only recolors text through [typo]. Icons, buttons and
 * every other Material default read [LocalContentColor] and [MaterialTheme.colorScheme], which at
 * light theme resolve to dark-on-light and turn grey and unreadable over the artwork. Providing the
 * dark scheme here fixes the whole subtree in one place.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForceDarkContent(content: @Composable () -> Unit) {
    val darkScheme = LocalForcedDarkColorScheme.current ?: MaterialTheme.colorScheme
    MaterialExpressiveTheme(
        colorScheme = darkScheme,
        content = {
            CompositionLocalProvider(
                LocalForceDarkText provides true,
                LocalIsDarkTheme provides true,
                LocalContentColor provides darkScheme.onSurfaceVariant,
                LocalAppColors provides DarkAppColors,
                content = content,
            )
        },
        typography = typo(darkScheme, forceDark = true),
    )
}

/**
 * Resolves the stored theme mode to a plain boolean.
 *
 * Pulled out of [AppTheme] so chrome living *outside* it can ask the same question — the desktop
 * title bar is drawn before AppTheme is entered, so it has no MaterialTheme to read and would
 * otherwise need its own copy of this `when`.
 */
@Composable
fun isDarkTheme(themeMode: String): Boolean =
    when (themeMode) {
        DataStoreManager.THEME_MODE_LIGHT -> false
        DataStoreManager.THEME_MODE_SYSTEM -> isSystemInDarkTheme()
        else -> true
    }
