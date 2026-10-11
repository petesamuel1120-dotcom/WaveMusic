package com.maxrave.simpmusic.ui.component

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import java.util.function.Consumer
import kotlin.math.roundToInt

@Composable
internal actual fun DialogWindowScrim(opacity: () -> Float, fullScreen: Boolean) {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    val motion = LocalDialogMotionOwner.current
    val returnComplete = motion?.returnComplete == true
    val originalWindowAlpha = remember(window) { window?.attributes?.alpha ?: 1f }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && window != null && motion != null) {
        NativeDialogBackdropBlur(window, motion)
    }
    val systemBars = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
    val statusBarColor = MaterialTheme.colorScheme.background
    val lightStatusBar = statusBarColor.luminance() > 0.5f
    DisposableEffect(window, fullScreen) {
        if (window == null || systemBars == null) return@DisposableEffect onDispose {}
        val originalStatusBarColor = window.statusBarColor
        val originalLightStatusBars = systemBars.isAppearanceLightStatusBars
        val originalLightNavigationBars = systemBars.isAppearanceLightNavigationBars
        window.setWindowAnimations(0)
        // Compose draws the dim scrim; keep native dim disabled throughout the spring.
        window.setDimAmount(0f)
        onDispose {
            // This window is discarded after the Compose return. Keep native animation and
            // dim disabled so teardown cannot add another transition to the returned control.
            window.setWindowAnimations(0)
            window.setDimAmount(0f)
            // Keep the outgoing native surface transparent even if an OEM retains its last
            // buffer during removal. This window is discarded, so its alpha is not restored.
            window.setDialogWindowAlpha(0f)
            window.statusBarColor = originalStatusBarColor
            systemBars.isAppearanceLightStatusBars = originalLightStatusBars
            if (fullScreen) {
                systemBars.isAppearanceLightNavigationBars = originalLightNavigationBars
            }
        }
    }
    SideEffect {
        // Commit the transparent terminal frame before Dialog.dismiss removes the window.
        // The live settings row is already visible when returnComplete becomes true.
        window?.setDialogWindowAlpha(if (returnComplete) 0f else originalWindowAlpha)
        // Alert dialogs use their own Android window, so tint its status bar too. Full-screen
        // dialogs keep their content-controlled system bars.
        if (window != null && !fullScreen) {
            window.statusBarColor = statusBarColor.toArgb()
        }
        if (systemBars != null) {
            systemBars.isAppearanceLightStatusBars = !fullScreen && lightStatusBar
            if (fullScreen) {
                systemBars.isAppearanceLightNavigationBars = false
            }
        }
    }
    val tint = MaterialTheme.colorScheme.surfaceTint
    Box(
        Modifier.fillMaxSize().drawBehind {
            val reveal = opacity().coerceIn(0f, 1f)
            // A deep neutral veil with a trace of theme color reads as frosted material instead
            // of a flat black overlay on top of the system's cross-window blur.
            drawRect(Color.Black.copy(alpha = 0.18f * reveal))
            drawRect(tint.copy(alpha = 0.035f * reveal))
        },
    )
}

private fun Window.setDialogWindowAlpha(alpha: Float) {
    val current = attributes
    if (current.alpha != alpha) {
        current.alpha = alpha
        attributes = current
    }
}

/** Blur the actual windows behind the dialog, including the activity and a parent sheet/dialog. */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun NativeDialogBackdropBlur(window: Window, motion: DialogMotionState) {
    val manager = window.context.getSystemService(WindowManager::class.java)
    val density = LocalDensity.current.density
    DisposableEffect(window, manager, motion) {
        val originalRadius = window.attributes.blurBehindRadius
        val originalBlurFlag = window.attributes.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        motion.nativeBackdropBlur = manager.isCrossWindowBlurEnabled
        val listener = Consumer<Boolean> { motion.nativeBackdropBlur = it }
        manager.addCrossWindowBlurEnabledListener(listener)
        onDispose {
            manager.removeCrossWindowBlurEnabledListener(listener)
            motion.nativeBackdropBlur = false
            val attributes = window.attributes
            attributes.blurBehindRadius = originalRadius
            attributes.flags = (attributes.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()) or originalBlurFlag
            window.attributes = attributes
        }
    }
    LaunchedEffect(window, motion, density) {
        // Update only the integer radius when it changes. Entry, interrupted/reverse motion and
        // runtime blur availability all share the same progress; no independent blur animator.
        snapshotFlow {
            if (motion.nativeBackdropBlur) dialogBackdropBlurRadius(motion.progress.value, density).roundToInt() else 0
        }.collect { radius ->
            val attributes = window.attributes
            attributes.blurBehindRadius = radius
            attributes.flags = if (radius > 0) {
                attributes.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            } else {
                attributes.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
            }
            window.attributes = attributes
        }
    }
}

internal actual fun animatedDialogProperties(original: DialogProperties, fullScreen: Boolean) = DialogProperties(
    dismissOnBackPress = original.dismissOnBackPress,
    dismissOnClickOutside = false, // The full-window stage handles outside taps.
    usePlatformDefaultWidth = false,
    securePolicy = original.securePolicy,
    decorFitsSystemWindows = if (fullScreen) false else original.decorFitsSystemWindows,
    windowTitle = original.windowTitle,
    windowType = original.windowType,
    windowToken = original.windowToken,
)
