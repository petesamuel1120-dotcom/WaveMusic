package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.*

/** Use the shared dialog theme and motion for both Library and player playlist creation. */
@Composable
fun CreatePlaylistDialog(onDismissRequest: () -> Unit, onCreate: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var invalidName by rememberSaveable { mutableStateOf(false) }
    AmoledAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.create_playlist), style = typo().titleMedium) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; invalidName = false },
                label = { Text(stringResource(Res.string.playlist_name)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = invalidName,
                supportingText = {
                    if (invalidName) Text(stringResource(Res.string.playlist_name_cannot_be_empty))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            DialogTextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.cancel)) }
        },
        confirmButton = {
            val close = rememberDialogCloseAction()
            TextButton(
                enabled = LocalDialogMotionOwner.current?.closing != true,
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        invalidName = true
                    } else {
                        close { onCreate(trimmedName); onDismissRequest() }
                    }
                },
            ) { Text(stringResource(Res.string.create_playlist)) }
        },
    )
}
