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
val WaveIcons.Mic: ImageVector
    get() {
        if (_Mic != null) return _Mic!!
        _Mic =
            ImageVector.Builder(
                name = "Mic",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(12f, 14f)
                    curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
                    verticalLineTo(5f)
                    curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
                    reflectiveCurveTo(9f, 3.34f, 9f, 5f)
                    verticalLineToRelative(6f)
                    curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                    close()
                    moveTo(17f, 11f)
                    curveToRelative(0f, 2.76f, -2.24f, 5f, -5f, 5f)
                    reflectiveCurveToRelative(-5f, -2.24f, -5f, -5f)
                    horizontalLineTo(5f)
                    curveToRelative(0f, 3.53f, 2.61f, 6.43f, 6f, 6.92f)
                    verticalLineTo(21f)
                    horizontalLineToRelative(2f)
                    verticalLineToRelative(-3.08f)
                    curveToRelative(3.39f, -0.49f, 6f, -3.39f, 6f, -6.92f)
                    horizontalLineToRelative(-2f)
                    close()
                }
            }.build()
        return _Mic!!
    }

private var _Mic: ImageVector? = null
