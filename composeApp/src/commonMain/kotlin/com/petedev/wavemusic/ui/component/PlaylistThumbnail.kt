package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.ui.icon.QueueMusic
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import org.jetbrains.compose.resources.painterResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.monochrome
import kotlin.math.abs

@Composable
fun PlaylistThumbnail(
    title: String,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalAppIsDarkTheme.current
    val gradientColors = playlistThumbnailColors(title, isDark)
    val titleColor = if (isDark) Color.White else Color(0xFF20242A)
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
    ) {

        // Title in bottom right
        Text(
            text = title,
            color = titleColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}

/** Empty local-playlist art tile: a theme-matched surface with the familiar queue icon. */
@Composable
fun LocalPlaylistArtworkPlaceholder(
    modifier: Modifier = Modifier,
) {
    val isDark = LocalAppIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isDark) colors.surfaceContainerHighest else colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.55f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = WaveIcons.QueueMusic,
            contentDescription = null,
            tint = if (isDark) colors.onSurfaceVariant.copy(alpha = 0.55f) else colors.onSurfaceVariant,
            modifier = Modifier.size(36.dp),
        )
    }
}

/** Large empty playlist header art uses a neutral theme surface and a layout-sized queue icon. */
@Composable
fun LocalPlaylistHeroArtwork(
    modifier: Modifier = Modifier,
) {
    val isDark = LocalAppIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(
        modifier = modifier.background(colors.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = WaveIcons.QueueMusic,
            contentDescription = null,
            tint = colors.onSurfaceVariant.copy(alpha = if (isDark) 0.55f else 0.7f),
            modifier = Modifier.size(minOf(maxWidth, maxHeight) * 0.3f),
        )
    }
}

// Generate deterministic gradient colors from title
private fun generateGradientFromTitle(title: String): List<Color> {
    val hash = title.hashCode()

    // Extract RGB components from hash
    val hue1 = ((hash and 0xFF) / 255f) * 360f
    val hue2 = (((hash shr 8) and 0xFF) / 255f) * 360f

    // Convert HSV to RGB for vibrant colors
    val color1 = hsvToColor(hue1, 0.7f, 0.9f)
    val color2 = hsvToColor(hue2, 0.7f, 0.85f)

    return listOf(color1, color2)
}

/**
 * Deterministic placeholder gradient colors for a playlist title — matches the
 * [PlaylistThumbnail] / [painterPlaylistThumbnail] placeholder, so a screen can tint
 * its background to match a local playlist that has no real artwork.
 */
fun playlistTitleGradient(title: String): List<Color> = generateGradientFromTitle(title)

private fun playlistThumbnailColors(title: String, isDark: Boolean): List<Color> {
    val colors = generateGradientFromTitle(title)
    return if (isDark) colors else colors.map { lerp(it, Color.White, 0.72f) }
}

// HSV to RGB conversion
private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val h = hue / 60f
    val c = value * saturation
    val x = c * (1 - abs((h % 2) - 1))
    val m = value - c

    val (r, g, b) = when (h.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m),
        green = (g + m),
        blue = (b + m),
        alpha = 1f
    )
}

private class PlaylistThumbnailPainter(
    private val size: Size,
    private val title: String,
    private val textLayoutResult: TextLayoutResult,
    private val iconPainter: Painter,
    private val isDark: Boolean,
): Painter() {
    override val intrinsicSize: Size
        get() = size

    override fun DrawScope.onDraw() {
        val colors = playlistThumbnailColors(title, isDark)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = colors
            ),
            size = size,
            cornerRadius = CornerRadius(16f, 16f),
        )
        drawText(textLayoutResult, topLeft = calculateTopLeft(
            alignment = Alignment.BottomStart,
            textSize = IntSize(
                width = textLayoutResult.size.width,
                height = textLayoutResult.size.height,
            ),
            containerSize = IntSize(
                size.width.toInt(),
                size.height.toInt(),
            ),
        ))
        Logger.d("PlaylistThumbnailPainter", "Drawing icon $title")
        Logger.d("PlaylistThumbnailPainter", "Colors: ${colors.map { it.toArgb() }}")
        val centerX = size.width * 0.9f
        val centerY = size.height * 0.1f
        val circleRadius = size.width * 0.1f / 2

        drawCircle(
            center = Offset(centerX, centerY),
            color = if (isDark) Color.White else Color(0xFF20242A),
            radius = circleRadius
        )

        translate(
            left = centerX - size.width * 0.15f / 2,
            top = centerY - size.height * 0.15f / 2,
        ) {
            with(iconPainter) {
                draw(size * 0.15f, alpha = 0.2f)
            }
        }
    }
}

private fun calculateTopLeft(
    alignment: Alignment,
    textSize: IntSize,
    containerSize: IntSize,
): Offset {
    val alignmentOffset =
        alignment.align(
            size = textSize.let { (width, height) ->
                IntSize(
                    width = width,
                    height = height,
                )
            },
            space = containerSize,
            layoutDirection = LayoutDirection.Ltr,
        )

    return Offset(
        x = alignmentOffset.x.toFloat() + 16f,
        y = alignmentOffset.y.toFloat() - 16f,
    )
}

@Composable
fun painterPlaylistThumbnail(
    title: String,
    style: TextStyle,
    sizeDp: Pair<Dp, Dp>,
): Painter {
    val isDark = LocalAppIsDarkTheme.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult =
        remember(title, style, sizeDp, isDark) {
            textMeasurer.measure(
                title,
                style.copy(
                    color = if (isDark) Color.White else Color(0xFF20242A),
                    textAlign = TextAlign.Start
                ),
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                layoutDirection = LayoutDirection.Ltr,
                constraints =
                    Constraints(
                        maxWidth = with(density) { (sizeDp.first - 32.dp).toPx().toInt() },
                        maxHeight = with(density) { (sizeDp.second - 32.dp).toPx().toInt() },
                    ),
            )
        }
    val painterRes = painterResource(Res.drawable.monochrome)
    return PlaylistThumbnailPainter(
        size = Size(
            width = with(density) { sizeDp.first.toPx() },
            height = with(density) { sizeDp.second.toPx() },
        ),
        title = title,
        textLayoutResult = textLayoutResult,
        iconPainter = painterRes,
        isDark = isDark,
    )
}
