package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(
    loading: Boolean,
    message: String,
) {
    if (loading) {
        val dialogPalette = rememberAppDialogPalette()
        Box(modifier = Modifier.fillMaxSize()) {
            BasicAlertDialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                modifier = Modifier.wrapContentSize(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, dialogPalette.border, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = dialogPalette.container,
                    contentColor = dialogPalette.content,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        Modifier.padding(
                            horizontal = 20.dp,
                            vertical = 20.dp,
                        ),
                    ) {
                        Text(
                            stringResource(Res.string.loading),
                            style = typo().headlineMedium,
                        )
                        Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.size(15.dp))
                            Text(text = message)
                        }
                    }
                }
            }
        }
    }
}
