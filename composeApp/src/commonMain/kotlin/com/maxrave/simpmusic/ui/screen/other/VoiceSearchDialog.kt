package com.maxrave.simpmusic.ui.screen.other

import com.maxrave.simpmusic.ui.component.localizedDescription
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.voice_allow_microphone
import simpmusic.composeapp.generated.resources.voice_close
import simpmusic.composeapp.generated.resources.voice_example
import simpmusic.composeapp.generated.resources.voice_listening
import simpmusic.composeapp.generated.resources.voice_microphone_needed
import simpmusic.composeapp.generated.resources.voice_not_caught
import simpmusic.composeapp.generated.resources.voice_permission_prompt
import simpmusic.composeapp.generated.resources.voice_processing
import simpmusic.composeapp.generated.resources.voice_processing_prompt
import simpmusic.composeapp.generated.resources.voice_retry_prompt
import simpmusic.composeapp.generated.resources.voice_start
import simpmusic.composeapp.generated.resources.voice_stop
import simpmusic.composeapp.generated.resources.voice_try_saying
import simpmusic.composeapp.generated.resources.voice_type_prompt
import simpmusic.composeapp.generated.resources.voice_unavailable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.maxrave.simpmusic.expect.ui.VoiceSearchState
import com.maxrave.simpmusic.expect.ui.rememberVoiceSearchLauncher
import com.maxrave.simpmusic.ui.component.AnimatedAppDialog as Dialog
import com.maxrave.simpmusic.ui.component.DialogContentStage
import com.maxrave.simpmusic.ui.component.dialogContentMotion
import com.maxrave.simpmusic.ui.component.rememberDialogCloseAction
import com.maxrave.simpmusic.ui.component.auroraGradientBackground
import com.maxrave.simpmusic.ui.icon.Mic
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme

@Composable
internal fun VoiceSearchDialog(
    onDismissRequest: () -> Unit,
    onResult: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
        fullScreen = true,
    ) {
        val closeDialog = rememberDialogCloseAction()
        var state by remember { mutableStateOf(VoiceSearchState.IDLE) }
        var partialResult by remember { mutableStateOf("") }
        val voiceSearch =
            rememberVoiceSearchLauncher(
                onResult = { query ->
                    closeDialog {
                        onDismissRequest()
                        onResult(query)
                    }
                },
                onPartialResult = { partialResult = it },
                onStateChange = { state = it },
            )

        LaunchedEffect(voiceSearch) { voiceSearch.launch() }
        DisposableEffect(voiceSearch) { onDispose { voiceSearch.cancel() } }

        VoiceSearchContent(
            state = state,
            partialResult = partialResult,
            onDismiss = {
                voiceSearch.cancel()
                closeDialog(onDismissRequest)
            },
            onMicClick = {
                if (state == VoiceSearchState.LISTENING) voiceSearch.stop() else voiceSearch.launch()
            },
        )
    }
}

@Composable
internal fun VoiceSearchContent(
    state: VoiceSearchState,
    partialResult: String,
    onDismiss: () -> Unit,
    onMicClick: () -> Unit,
) {
    val colors = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val isLightTheme = !LocalAppIsDarkTheme.current

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .auroraGradientBackground(isLightTheme)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val titleTop = (maxHeight * 0.18f).coerceAtMost(144.dp)
        val micBottom = (maxHeight * 0.085f).coerceAtMost(72.dp)
        val micBottomPadding = (micBottom - 46.dp).coerceAtLeast(0.dp)
        val landscape = maxWidth > maxHeight
        val textWidth = if (landscape) maxWidth * 0.55f else maxWidth

        VoiceSearchHandle(
            onDismiss = onDismiss,
            color = colors.onBackground,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                .dialogContentMotion(DialogContentStage.Title),
        )

        Text(
            text =
                when (state) {
                    VoiceSearchState.IDLE, VoiceSearchState.LISTENING -> stringResource(Res.string.voice_listening)
                    VoiceSearchState.REQUESTING_PERMISSION -> stringResource(Res.string.voice_allow_microphone)
                    VoiceSearchState.PROCESSING -> stringResource(Res.string.voice_processing)
                    VoiceSearchState.PERMISSION_DENIED -> stringResource(Res.string.voice_microphone_needed)
                    VoiceSearchState.UNAVAILABLE -> stringResource(Res.string.voice_unavailable)
                    VoiceSearchState.ERROR -> stringResource(Res.string.voice_not_caught)
                },
            color = colors.onBackground,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopStart).width(textWidth)
                .padding(start = 28.dp, top = titleTop, end = 28.dp)
                .dialogContentMotion(DialogContentStage.Title),
        )

        VoiceSearchPrompt(
            state = state,
            partialResult = partialResult,
            color = colors.onSurfaceVariant,
            modifier = (if (landscape) {
                Modifier.align(Alignment.TopStart).width(textWidth).padding(top = maxHeight * 0.48f)
            } else {
                // Anchor the actual text height just above the widest ring instead of reserving
                // extra space for an estimated prompt height. This also follows larger text sizes.
                Modifier.align(Alignment.BottomCenter).width(textWidth)
                    .padding(bottom = micBottomPadding + 224.dp + 16.dp)
            }).dialogContentMotion(DialogContentStage.Body),
        )

        VoiceSearchMicrophone(
            state = state,
            isLightTheme = isLightTheme,
            ringColor = colors.onBackground,
            onClick = onMicClick,
            modifier = (if (landscape) {
                Modifier.align(Alignment.CenterEnd).padding(end = maxWidth * 0.08f)
            } else {
                // Keep the button at its existing center while leaving room for the expanding rings.
                Modifier.align(Alignment.BottomCenter).padding(bottom = micBottomPadding)
            }).dialogContentMotion(DialogContentStage.Actions),
        )
    }
}

@Composable
private fun VoiceSearchHandle(
    onDismiss: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    Box(
        modifier.width(96.dp).height(48.dp).clip(RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                val threshold = 40.dp.toPx()
                var distance = 0f
                detectVerticalDragGestures(
                    onDragStart = { distance = 0f },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        distance = (distance + amount).coerceAtLeast(0f)
                    },
                    onDragEnd = { if (distance >= threshold) latestOnDismiss() },
                    onDragCancel = { distance = 0f },
                )
            }
            .localizedDescription(Res.string.voice_close)
            .clickable(onClickLabel = stringResource(Res.string.voice_close), role = Role.Button, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(44.dp).height(5.dp).clip(CircleShape).background(color.copy(alpha = 0.9f)))
    }
}

@Composable
private fun VoiceSearchMicrophone(
    state: VoiceSearchState,
    isLightTheme: Boolean,
    ringColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isListening = state == VoiceSearchState.LISTENING
    val buttonColor by animateColorAsState(
        targetValue = if (isListening) Color(0xFFFF003B) else {
            if (isLightTheme) Color(0xFFE3E3E8) else Color(0xFF1C1C20)
        },
        animationSpec = tween(180),
        label = "voice-search-microphone-color",
    )
    Box(modifier.size(224.dp), contentAlignment = Alignment.Center) {
        if (isListening) {
            // Read the phase only while drawing: the rings never remeasure the button or text.
            // Removing this branch stops the infinite animation as soon as recognition stops.
            val pulse = rememberInfiniteTransition(label = "voice-search-listening")
            val phase = pulse.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
                label = "voice-search-rings",
            )
            Canvas(Modifier.fillMaxSize()) {
                val progress = phase.value
                drawCircle(
                    color = ringColor.copy(alpha = 0.07f * (1f - progress)),
                    radius = (56f + 22f * progress).dp.toPx(),
                )
                drawCircle(
                    color = ringColor.copy(alpha = 0.28f * (1f - progress)),
                    radius = (76f + 34f * progress).dp.toPx(),
                    style = Stroke(1.dp.toPx()),
                )
            }
        }
        IconButton(
            onClick = onClick,
            enabled = state != VoiceSearchState.PROCESSING && state != VoiceSearchState.REQUESTING_PERMISSION,
            modifier = Modifier.size(96.dp).clip(CircleShape).background(buttonColor),
        ) {
            Icon(
                imageVector = SimpIcons.Mic,
                contentDescription = if (isListening) stringResource(Res.string.voice_stop) else stringResource(Res.string.voice_start),
                tint = if (isListening || !isLightTheme) Color.White else Color(0xFF45454B),
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun VoiceSearchPrompt(
    state: VoiceSearchState,
    partialResult: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val processingCaption = stringResource(Res.string.voice_processing_prompt)
    val isLiveTranscript = state == VoiceSearchState.LISTENING && partialResult.isNotBlank()
    val caption =
        when {
            isLiveTranscript -> partialResult
            state == VoiceSearchState.IDLE || state == VoiceSearchState.LISTENING -> stringResource(Res.string.voice_try_saying)
            state == VoiceSearchState.REQUESTING_PERMISSION -> stringResource(Res.string.voice_permission_prompt)
            state == VoiceSearchState.PROCESSING -> partialResult.ifBlank { processingCaption }
            state == VoiceSearchState.PERMISSION_DENIED -> stringResource(Res.string.voice_retry_prompt)
            state == VoiceSearchState.UNAVAILABLE -> stringResource(Res.string.voice_type_prompt)
            state == VoiceSearchState.ERROR -> stringResource(Res.string.voice_retry_prompt)
            else -> stringResource(Res.string.voice_try_saying)
        }

    Column(modifier.padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = caption,
            color = color,
            fontSize = if (isLiveTranscript) 28.sp else 21.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        if (!isLiveTranscript && (state == VoiceSearchState.IDLE || state == VoiceSearchState.LISTENING)) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.voice_example),
                color = color,
                fontSize = 21.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            )
        }
    }
}
