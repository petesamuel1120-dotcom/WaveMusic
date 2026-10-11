package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.maxrave.simpmusic.expect.ui.BackHandler
import kotlinx.coroutines.flow.collect

/** Closes an active playlist search when Back dismisses the keyboard or the screen. */
@Composable
fun SearchBarBackHandler(
    visible: Boolean,
    onDismiss: () -> Unit,
    onKeyboardDismissed: () -> Unit = onDismiss,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val dismiss by rememberUpdatedState(onDismiss)
    val keyboardDismissed by rememberUpdatedState(onKeyboardDismissed)

    BackHandler(enabled = visible) {
        keyboardController?.hide()
        dismiss()
    }

    // Android gives the IME the first Back event while it is visible. Observe each transition to
    // hidden so the search UI closes on that gesture, while still letting callers preserve their
    // filtered results when Enter dismisses the keyboard.
    LaunchedEffect(visible, density) {
        if (!visible) return@LaunchedEffect

        var keyboardWasShown = false
        var keyboardWasVisible = false
        snapshotFlow { imeInsets.getBottom(density) > 0 }
            .collect { keyboardVisible ->
                if (keyboardVisible) {
                    keyboardWasShown = true
                    keyboardWasVisible = true
                } else if (keyboardWasShown && keyboardWasVisible) {
                    keyboardWasVisible = false
                    keyboardDismissed()
                }
            }
    }
}
