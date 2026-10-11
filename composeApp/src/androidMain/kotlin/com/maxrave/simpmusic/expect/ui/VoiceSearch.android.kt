package com.maxrave.simpmusic.expect.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale

@Composable
actual fun rememberVoiceSearchLauncher(
    onResult: (String) -> Unit,
    onPartialResult: (String) -> Unit,
    onStateChange: (VoiceSearchState) -> Unit,
): VoiceSearchLauncher {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnResult = rememberUpdatedState(onResult)
    val latestOnPartialResult = rememberUpdatedState(onPartialResult)
    val latestOnStateChange = rememberUpdatedState(onStateChange)
    val state = remember { mutableStateOf(VoiceSearchState.IDLE) }
    val isRecognitionActive = remember { mutableStateOf(false) }
    val recognizer = remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun updateState(newState: VoiceSearchState) {
        state.value = newState
        latestOnStateChange.value(newState)
    }

    lateinit var startRecognition: () -> Unit
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startRecognition() else updateState(VoiceSearchState.PERMISSION_DENIED)
        }

    val recognitionListener =
        remember(context) {
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (isRecognitionActive.value) updateState(VoiceSearchState.LISTENING)
                }

                override fun onBeginningOfSpeech() {
                    if (isRecognitionActive.value) updateState(VoiceSearchState.LISTENING)
                }

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    if (isRecognitionActive.value) updateState(VoiceSearchState.PROCESSING)
                }

                override fun onError(error: Int) {
                    if (!isRecognitionActive.value) return
                    isRecognitionActive.value = false
                    updateState(
                        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                            VoiceSearchState.PERMISSION_DENIED
                        } else {
                            VoiceSearchState.ERROR
                        },
                    )
                }

                override fun onResults(results: Bundle?) {
                    if (!isRecognitionActive.value) return
                    isRecognitionActive.value = false
                    val result =
                        results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                    if (result.isNullOrBlank()) {
                        updateState(VoiceSearchState.ERROR)
                    } else {
                        updateState(VoiceSearchState.PROCESSING)
                        latestOnResult.value(result)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    if (!isRecognitionActive.value) return
                    partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        ?.takeIf(String::isNotBlank)
                        ?.let(latestOnPartialResult.value)
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            }
        }

    startRecognition = {
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            updateState(VoiceSearchState.REQUESTING_PERMISSION)
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            updateState(VoiceSearchState.UNAVAILABLE)
        } else {
            try {
                val speechRecognizer =
                    recognizer.value
                        ?: SpeechRecognizer.createSpeechRecognizer(context).also {
                            it.setRecognitionListener(recognitionListener)
                            recognizer.value = it
                        }
                isRecognitionActive.value = true
                latestOnPartialResult.value("")
                updateState(VoiceSearchState.LISTENING)
                speechRecognizer.startListening(
                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    },
                )
            } catch (_: RuntimeException) {
                isRecognitionActive.value = false
                updateState(VoiceSearchState.UNAVAILABLE)
            }
        }
    }

    val launcher =
        remember(context, permissionLauncher, recognitionListener) {
            object : VoiceSearchLauncher {
                override fun launch() = startRecognition()

                override fun stop() {
                    if (isRecognitionActive.value) {
                        updateState(VoiceSearchState.PROCESSING)
                        recognizer.value?.stopListening()
                    }
                }

                override fun cancel() {
                    isRecognitionActive.value = false
                    recognizer.value?.cancel()
                    latestOnPartialResult.value("")
                    updateState(VoiceSearchState.IDLE)
                }
            }
        }

    DisposableEffect(launcher) {
        onDispose {
            isRecognitionActive.value = false
            recognizer.value?.cancel()
            recognizer.value?.destroy()
            recognizer.value = null
        }
    }
    DisposableEffect(lifecycleOwner, launcher) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) launcher.cancel()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return launcher
}
