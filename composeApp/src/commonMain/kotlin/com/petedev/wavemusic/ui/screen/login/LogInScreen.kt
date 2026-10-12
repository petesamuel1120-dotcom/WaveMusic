package com.petedev.wavemusic.ui.screen.login

import com.petedev.wavemusic.ui.component.CompactHeaderContentHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.expect.ui.PlatformWebView
import com.petedev.wavemusic.expect.ui.createWebViewCookieManager
import com.petedev.wavemusic.expect.ui.rememberWebViewState
import com.petedev.wavemusic.ui.component.DevLogInBottomSheet
import com.petedev.wavemusic.ui.component.DevLogInType
import com.petedev.wavemusic.ui.component.RippleIconButton
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.LogoDev
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.LogInViewModel
import com.petedev.wavemusic.viewModel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.log_in
import com.petedev.wavemusic.resources.login_failed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: LogInViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    hideBottomNavigation: () -> Unit,
    showBottomNavigation: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var devLoginSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var isCompletingYouTubeLogin by rememberSaveable { mutableStateOf(false) }

    val state = rememberWebViewState()

    LaunchedEffect(state) {
        snapshotFlow { state.value }.collect {
            Logger.d(
                "LogInScreen",
                "WebViewState: ${
                    when (it) {
                        is com.petedev.wavemusic.expect.ui.WebViewState.Finished -> "Finished"
                        is com.petedev.wavemusic.expect.ui.WebViewState.Loading -> "Loading ${it.progress}%"
                    }
                }",
            )
        }
    }

    // Hide bottom navigation when entering this screen
    LaunchedEffect(Unit) {
        hideBottomNavigation()
        createWebViewCookieManager().removeAllCookies()
    }

    // Show bottom navigation when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            showBottomNavigation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(
                Modifier
                    .size(
                        innerPadding.calculateTopPadding() + CompactHeaderContentHeight,
                    ),
            )
            // WebView for YouTube Music login
            PlatformWebView(
                state,
                Config.LOG_IN_URL,
                aboveContent = {
                    if (devLoginSheet) {
                        DevLogInBottomSheet(
                            onDismiss = {
                                devLoginSheet = false
                            },
                            onDone = { cookie ->
                                coroutineScope.launch {
                                    val success = settingsViewModel.addAccount(cookie)
                                    if (success) {
                                        navController.navigateUp()
                                    } else {
                                        viewModel.makeToast(getString(Res.string.login_failed))
                                    }
                                }
                            },
                            type = DevLogInType.YouTube,
                        )
                    }
                }
            ) { url ->
                Logger.d("LogInScreen", "Current URL: $url")
                if (!isCompletingYouTubeLogin &&
                    url.substringBefore('?').trimEnd('/') == Config.YOUTUBE_MUSIC_MAIN_URL.trimEnd('/')
                ) {
                    isCompletingYouTubeLogin = true
                    coroutineScope.launch {
                        val success =
                            createWebViewCookieManager()
                                .getCookie(url)
                                .takeIf {
                                    it.isNotEmpty()
                                }?.let {
                                    settingsViewModel.addAccount(it)
                                } ?: false

                        createWebViewCookieManager().removeAllCookies()

                        if (success) {
                            keyboardController?.hide()
                            navController.navigateUp()
                        } else {
                            viewModel.makeToast(getString(Res.string.login_failed))
                            isCompletingYouTubeLogin = false
                        }
                    }
                }
            }
        }

        // Keep the login WebView outside a live blur capture. Some Android WebView/Vulkan
        // combinations crash while drawing a WebViewFunctor into an offscreen blur layer.
        TopAppBar(
            expandedHeight = CompactHeaderContentHeight,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f)),
            title = {
                Text(
                    text = stringResource(Res.string.log_in),
                    style = typo().titleMedium,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        WaveIcons.ArrowBackIosNew,
                        Modifier.size(32.dp),
                        true,
                    ) {
                        navController.navigateUp()
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        devLoginSheet = true
                    },
                ) {
                    Icon(
                        WaveIcons.LogoDev,
                        "Developer Mode",
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
        )
    }
}
