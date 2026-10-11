package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalForceDarkText
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import com.maxrave.simpmusic.ui.theme.typo

/** Runs search controls against the app theme, including when hosted on an immersive dark page. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppSearchBarTheme(compact: Boolean = false, content: @Composable () -> Unit) {
    val appScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val appIsDark = LocalAppIsDarkTheme.current
    val appTypography = typo(appScheme, forceDark = false)
    MaterialExpressiveTheme(
        colorScheme = appScheme,
        typography = appTypography.copy(
            bodyLarge = if (compact) inPageSearchBarTextStyle(appTypography.bodyLarge)
            else appSearchBarTextStyle(appTypography.bodyLarge),
        ),
    ) {
        CompositionLocalProvider(
            LocalForceDarkText provides false,
            LocalIsDarkTheme provides appIsDark,
            LocalContentColor provides appScheme.onSurfaceVariant,
            content = content,
        )
    }
}

val InPageSearchBarHeight: Dp
    @Composable get() = if (isCompactLandscapeChrome()) 36.dp else 52.dp

@Composable
fun inPageSearchBarTextStyle(style: TextStyle): TextStyle =
    appSearchBarTextStyle(style.copy(fontSize = if (isCompactLandscapeChrome()) 14.sp else 16.sp))

/** Gives single-line fields room for Arabic marks and lifts glyphs slightly within the input. */
fun appSearchBarTextStyle(style: TextStyle): TextStyle =
    style.copy(
        lineHeight = style.fontSize * 1.5f,
        baselineShift = BaselineShift(0.16f),
    )

/** Shared search surface: transparent for glass, otherwise AMOLED black or pure white by theme. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appSearchBarColors(transparentContainer: Boolean = false): SearchBarColors {
    val isDark = LocalAppIsDarkTheme.current
    val container =
        when {
            transparentContainer -> Color.Transparent
            isDark -> Color.Black
            else -> Color.White
        }
    return SearchBarDefaults.colors().copy(containerColor = container)
}

@Composable
fun appSearchBarContentColor(): Color =
    (LocalAppColorScheme.current ?: MaterialTheme.colorScheme).onSurface
