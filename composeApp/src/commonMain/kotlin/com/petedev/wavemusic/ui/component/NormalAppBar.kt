package com.petedev.wavemusic.ui.component

import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.back_action


import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.MoreVert
import com.petedev.wavemusic.ui.icon.WaveIcons
import org.jetbrains.compose.resources.painterResource
import com.petedev.wavemusic.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalAppBar(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    leftIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (RowScope.() -> Unit)? = null,
) {
    PageTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = leftIcon,
        actions = rightIcon,
        // Every other screen in the app builds its TopAppBar with a transparent container and
        // onBackground content. Leaving `colors` unset here fell back to the Material default
        // (surface container + onSurface content), which is why MoodScreen — the only caller —
        // showed a light bar with black text on top of the app's dark background.
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
    )
}

@Preview
@Composable
fun NormalAppBarPreview() {
    NormalAppBar(
        title = {
            Text(text = "Title")
        },
        leftIcon = {
            IconButton(onClick = { }) {
                Icon(
                    WaveIcons.ArrowBackIosNew,
                    contentDescription = stringResource(Res.string.back_action),
                )
            }
        },
        rightIcon = {
            IconButton(onClick = { }) {
                Icon(
                    WaveIcons.MoreVert,
                    contentDescription = stringResource(Res.string.back_action),
                )
            }
        },
    )
}
