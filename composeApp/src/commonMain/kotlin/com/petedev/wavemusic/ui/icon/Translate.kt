package com.petedev.wavemusic.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val WaveIcons.Translate: ImageVector
    get() {
        if (_Translate != null) return _Translate!!
        _Translate =
            ImageVector.Builder(
                name = "Translate",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    pathFillType = PathFillType.NonZero,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                ) {
                    moveTo(12.87f, 15.07f)
                    lineToRelative(-2.54f, -2.51f)
                    lineToRelative(0.03f, -0.03f)
                    curveToRelative(1.74f, -1.94f, 2.98f, -4.16f, 3.71f, -6.53f)
                    horizontalLineTo(17f)
                    verticalLineTo(4f)
                    horizontalLineToRelative(-7f)
                    verticalLineTo(2f)
                    horizontalLineTo(8f)
                    verticalLineToRelative(2f)
                    horizontalLineTo(1f)
                    verticalLineToRelative(1.99f)
                    horizontalLineToRelative(11.17f)
                    curveToRelative(-0.67f, 1.92f, -1.69f, 3.71f, -3.05f, 5.26f)
                    curveTo(8.24f, 9.75f, 7.53f, 8.08f, 7.16f, 6.5f)
                    horizontalLineTo(5.14f)
                    curveToRelative(0.44f, 2.09f, 1.36f, 4.07f, 2.69f, 5.78f)
                    lineTo(2.17f, 17.9f)
                    lineTo(3.6f, 19.33f)
                    lineToRelative(5.5f, -5.5f)
                    lineToRelative(3.42f, 3.42f)
                    lineToRelative(0.35f, -2.18f)
                    close()
                    moveTo(18.5f, 10f)
                    horizontalLineToRelative(-2f)
                    lineTo(12f, 22f)
                    horizontalLineToRelative(2f)
                    lineToRelative(1.12f, -3f)
                    horizontalLineToRelative(4.75f)
                    lineTo(21f, 22f)
                    horizontalLineToRelative(2f)
                    lineToRelative(-4.5f, -12f)
                    close()
                    moveTo(15.88f, 17f)
                    lineTo(17.5f, 12.67f)
                    lineTo(19.12f, 17f)
                    horizontalLineToRelative(-3.24f)
                    close()
                }
            }.build()
        return _Translate!!
    }

private var _Translate: ImageVector? = null
