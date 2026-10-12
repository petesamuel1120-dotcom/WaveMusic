package com.petedev.wavemusic.expect.ui

import android.os.Build
import android.view.RoundedCorner
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun rememberTopDisplayCornerRadii(): Pair<Dp, Dp> {
    val context = LocalContext.current
    val density = LocalDensity.current
    val orientation = LocalConfiguration.current.orientation
    return remember(context, density, orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val windowManager = context.getSystemService(WindowManager::class.java)
            val insets = windowManager?.currentWindowMetrics?.windowInsets
            with(density) {
                (insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0).toDp() to
                    (insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)?.radius ?: 0).toDp()
            }
        } else {
            0.dp to 0.dp
        }
    }
}
