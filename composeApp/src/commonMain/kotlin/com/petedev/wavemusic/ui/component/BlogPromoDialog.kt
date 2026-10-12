package com.petedev.wavemusic.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.DialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.blog_promo_message
import com.petedev.wavemusic.resources.blog_promo_title
import com.petedev.wavemusic.resources.later
import com.petedev.wavemusic.resources.mono
import com.petedev.wavemusic.resources.visit_blog

@Composable
@ExperimentalMaterial3Api
fun BlogPromoDialog(
    onDismissRequest: () -> Unit,
    onVisitBlog: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AmoledAlertDialog(
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        confirmButton = {
            TextButton(onClick = {
                onVisitBlog.invoke()
                uriHandler.openUri("https://github.com/petesamuel1120-dotcom/WaveMusic")
            }) {
                Text(
                    stringResource(Res.string.visit_blog),
                    style = typo().bodySmall,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest.invoke()
            }) {
                Text(
                    stringResource(Res.string.later),
                    style = typo().bodySmall,
                )
            }
        },
        icon = {
            Icon(painterResource(Res.drawable.mono), "App Icon")
        },
        title = {
            Text(
                stringResource(Res.string.blog_promo_title),
                style = typo().labelSmall,
            )
        },
        text = {
            Text(
                stringResource(Res.string.blog_promo_message),
                textAlign = TextAlign.Center,
                style = typo().bodySmall,
            )
        },
    )
}
