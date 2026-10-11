package com.maxrave.simpmusic.ui.component

import simpmusic.composeapp.generated.resources.continue_with_google

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.domain.data.entities.GoogleAccountEntity
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.ui.icon.PeopleAlt
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.account
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.log_out_from_youtube
import simpmusic.composeapp.generated.resources.signed_in

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun YouTubeAccountDialog(
    isLoggedIn: Boolean?,
    viewModel: SettingsViewModel,
    navController: NavController,
    onDismiss: () -> Unit,
) {
    val accountsState by viewModel.googleAccounts.collectAsStateWithLifecycle()
    val activeAccount = accountsState.data?.firstOrNull { it.isUsed } ?: accountsState.data?.firstOrNull()
    val palette = rememberAppDialogPalette()
    val screenInfo = getScreenSizeInfo()
    val isLandscape = screenInfo.wDP >= screenInfo.hDP
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel) { viewModel.getAllGoogleAccount() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = palette.container,
        contentColor = palette.content,
        scrimColor = Color.Black.copy(alpha = 0.68f),
        dragHandle = {
            Surface(
                Modifier.padding(top = 10.dp).size(width = 42.dp, height = 5.dp),
                shape = CircleShape,
                color = palette.content.copy(alpha = 0.36f),
            ) {}
        },
    ) {
        Column(
            modifier = Modifier
                .dialogMotionSource(clearPreviousOrigin = true)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = if (isLandscape) 8.dp else 18.dp, bottom = if (isLandscape) 16.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isLandscape) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    listOf("♫", "✦", "♪", "✦").forEachIndexed { index, glyph ->
                        val positions = listOf(
                            Alignment.CenterStart,
                            Alignment.TopCenter,
                            Alignment.CenterEnd,
                            Alignment.BottomCenter,
                        )
                        Text(
                            text = glyph,
                            modifier = Modifier.align(positions[index]),
                            color = Color(0xFF8EC2D7).copy(alpha = 0.62f),
                            style = typo().titleMedium,
                        )
                    }
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                style = typo().headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335)),
                                    ),
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(if (isLandscape) 4.dp else 10.dp))
            Text(
                text = if (isLoggedIn == true) stringResource(Res.string.account) else "Sign in to Wave Music",
                style = typo().headlineSmall,
                color = palette.content,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (isLandscape) 6.dp else 10.dp))
            Text(
                text = if (isLoggedIn == true) {
                    "Your Google account is connected to YouTube Music."
                } else {
                    "Connect your Google account to access your YouTube Music library, playlists and likes."
                },
                style = typo().bodyLarge,
                color = palette.secondaryContent,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            if (isLoggedIn == true && activeAccount != null) {
                Spacer(Modifier.height(if (isLandscape) 12.dp else 22.dp))
                AccountSummary(activeAccount, compact = isLandscape)
            }

            Spacer(Modifier.height(if (isLandscape) 16.dp else 26.dp))
            if (isLoggedIn == null) {
                CircularProgressIndicator(Modifier.size(26.dp), color = MaterialTheme.colorScheme.primary)
            } else if (isLoggedIn) {
                AccountActionButton(
                    text = stringResource(Res.string.log_out_from_youtube),
                    container = Color(0xFF25282D),
                    content = Color.White,
                    compact = isLandscape,
                    onClick = { viewModel.logOutAllYouTube(); onDismiss() },
                )
            } else {
                AccountActionButton(
                    text = stringResource(Res.string.continue_with_google),
                    container = Color(0xFF1A73E8),
                    content = Color.White,
                    compact = isLandscape,
                    onClick = { onDismiss(); navController.navigate(LoginDestination) },
                )
            }

            Spacer(Modifier.height(if (isLandscape) 8.dp else 14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(if (isLandscape) 40.dp else 44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f).height(1.dp).background(palette.border))
                Text(
                    text = stringResource(Res.string.cancel),
                    modifier =
                        Modifier
                            .padding(horizontal = 28.dp, vertical = 8.dp)
                            .clip(CircleShape)
                            .then(rememberAnimatedDialogAction(onDismiss)),
                    style = typo().titleMedium,
                    color = Color(0xFF8AB4F8),
                )
                Spacer(Modifier.weight(1f).height(1.dp).background(palette.border))
            }
        }
    }
}

@Composable
private fun AccountActionButton(
    text: String,
    container: Color,
    content: Color,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(if (compact) 52.dp else 64.dp)
                .clip(RoundedCornerShape(20.dp))
                .then(rememberAnimatedDialogAction(onClick)),
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (text == stringResource(Res.string.continue_with_google)) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = Color.White,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "G",
                            style = typo().titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335)),
                                ),
                            ),
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
            }
            Text(text, style = typo().titleMedium, color = content, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun rememberAnimatedDialogAction(onClick: () -> Unit): Modifier {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val currentOnClick by rememberUpdatedState(onClick)
    var isAnimating by remember { mutableStateOf(false) }

    return Modifier
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .clickable(enabled = !isAnimating) {
            isAnimating = true
            scope.launch {
                scale.animateTo(0.97f, tween(140, easing = FastOutSlowInEasing))
                scale.animateTo(1f, tween(170, easing = FastOutSlowInEasing))
                isAnimating = false
                currentOnClick()
            }
        }
}

@Composable
private fun AccountSummary(account: GoogleAccountEntity, compact: Boolean = false) {
    val palette = rememberAppDialogPalette()
    Row(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(if (compact) 8.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(account.thumbnailUrl)
                .crossfade(180)
                .build(),
            placeholder = rememberVectorPainter(SimpIcons.PeopleAlt),
            error = rememberVectorPainter(SimpIcons.PeopleAlt),
            contentDescription = account.name,
            modifier = Modifier.size(if (compact) 40.dp else 52.dp).clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(account.name, style = typo().labelMedium, color = palette.content, maxLines = 1)
            val handle = account.email.let { if (it.contains('@')) it else "@$it" }
            Text(handle, style = typo().bodySmall, color = palette.secondaryContent, maxLines = 1)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(Res.string.signed_in),
            style = typo().bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
