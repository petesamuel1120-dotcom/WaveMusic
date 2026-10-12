package com.petedev.wavemusic.ui.screen.home

import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.back_action
import com.petedev.wavemusic.resources.Res

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.petedev.wavemusic.ui.component.CenterLoadingBox
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.MoodAndGenresContentItem
import com.petedev.wavemusic.ui.component.FrostedHeader
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.component.NormalAppBar
import com.petedev.wavemusic.ui.component.auroraGradientBackground
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.MoodViewModel
import org.koin.compose.viewmodel.koinViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun MoodScreen(
    navController: NavController,
    viewModel: MoodViewModel = koinViewModel(),
    params: String?,
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val moodData by viewModel.moodsMomentObject.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(MaterialTheme.colorScheme.background)
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(88.dp) }
    LaunchedEffect(key1 = params) {
        if (params != null) {
            viewModel.getMood(params)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Keep the backdrop source behind both the list and the floating header. A Scaffold
        // topBar reserves its own layout slot, leaving the glass with a black/empty source to blur.
        Box(Modifier.fillMaxSize().frostedHeaderSource(frostedBackdrop).auroraGradientBackground(isLightTheme).hazeSource(hazeState)) {
            Crossfade(targetState = loading) { isLoading ->
                if (isLoading) {
                    CenterLoadingBox(Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = headerHeight),
                    ) {
                        items(moodData?.items ?: emptyList()) { item ->
                            MoodAndGenresContentItem(data = item, navController = navController)
                        }
                        item { EndOfPage() }
                    }
                }
            }
        }
        FrostedHeader(
            hazeState = hazeState,
            backdrop = frostedBackdrop,
            shareHeader = true,
            // Keep the category header in step with the app theme. A fixed black tint made
            // light-mode mood pages (for example Chill) look like they had a dark toolbar.
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                headerHeight = with(density) { coordinates.size.height.toDp() }
            },
        ) {
            NormalAppBar(
                title = {
                    Text(
                        text = moodData?.header.orEmpty(),
                        style = typo().titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leftIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(WaveIcons.ArrowBackIosNew, contentDescription = stringResource(Res.string.back_action))
                    }
                },
            )
        }
    }
}
