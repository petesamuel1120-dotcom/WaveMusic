package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.flow.collect

/** Dismiss search on a user's list drag, without reacting to automatic result scrolling. */
@Composable
fun SearchBarScrollDismissEffect(
    state: LazyListState,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    val dismiss by rememberUpdatedState(onDismiss)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(state, visible, focusManager, keyboardController) {
        if (!visible) return@LaunchedEffect
        state.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                dismiss()
            }
        }
    }
}
