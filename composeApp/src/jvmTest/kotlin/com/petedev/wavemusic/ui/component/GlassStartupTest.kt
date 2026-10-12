@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.theme.LiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlassStartupTest {
    @Test
    fun attachmentAndCollapsedBoundsCannotCreateNativeEffects() {
        for (size in listOf(Size.Unspecified, Size.Zero, Size(0f, 60f), Size(60f, 0f),
            Size(-1f, 60f), Size(60f, -1f), Size(Float.NaN, 60f), Size(60f, Float.NaN),
            Size(Float.POSITIVE_INFINITY, 60f), Size(60f, Float.NEGATIVE_INFINITY))) {
            assertFalse(size.hasGlassEffectBounds(), "Unsafe native effect bounds: $size")
        }
        assertTrue(Size(1f, 1f).hasGlassEffectBounds())
        assertTrue(Size(300f, 60f).hasGlassEffectBounds())
    }

    @Test
    fun glassAttachesCollapsedThenRendersAfterLayoutAtEveryMaterialIntensity() {
        for (intensity in listOf(0f, 0.25f, 0.5f, 1f)) {
            val controlSize = mutableStateOf(0.dp)
            val scene = ImageComposeScene(160, 100) {
                CompositionLocalProvider(
                    LocalLiquidGlassEnabled provides true,
                    LocalLiquidGlassAppearance provides LiquidGlassAppearance.fromIntensity(intensity),
                ) {
                    val backdrop = rememberBackdrop(Color.Green)
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.matchParentSize().layerBackdrop(backdrop).background(Color.Green))
                        Box(Modifier.align(Alignment.Center).size(controlSize.value).drawInteractiveGlass(
                            isDark = true,
                            backdrop = backdrop,
                            luminanceAnimation = 0.5f,
                            shape = RoundedCornerShape(24.dp),
                            interaction = null,
                        ))
                    }
                }
            }
            var time = 0L
            fun renderCenter(): Int {
                val image = scene.render(time.also { time += 16_666_667L })
                try {
                    val bitmap = Bitmap.makeFromImage(image)
                    try {
                        return bitmap.getColor(80, 50)
                    } finally {
                        bitmap.close()
                    }
                } finally {
                    image.close()
                }
            }
            try {
                repeat(3) { renderCenter() }
                controlSize.value = 64.dp
                repeat(3) { renderCenter() }
                val pixel = renderCenter()
                val red = (pixel ushr 16) and 255
                val green = (pixel ushr 8) and 255
                val blue = pixel and 255
                assertTrue(green > red && green > blue && green < 250,
                    "Measured glass must render its tinted backdrop at intensity $intensity")
                controlSize.value = 0.dp
                repeat(3) { renderCenter() }
            } finally {
                scene.close()
            }
        }
    }
}
