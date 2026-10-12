package com.petedev.wavemusic.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.DialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.window.DialogProperties
import com.petedev.wavemusic.ui.theme.seed
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.*

@Composable
@ExperimentalMaterial3Api
fun ReviewDialog(
    onDismissRequest: () -> Unit,
    onDoneReview: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AmoledAlertDialog(
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        confirmButton = {
            TextButton(onClick = {
                onDoneReview.invoke()
                uriHandler.openUri("https://github.com/petesamuel1120-dotcom/WaveMusic")
            }) {
                Text(
                    stringResource(Res.string.give_a_star),
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
                stringResource(Res.string.enjoying_wavemusic),
                style = typo().labelSmall,
            )
        },
        text = {
            Text(
                buildAnnotatedString {
                    append(stringResource(Res.string.if_you_enjoy_using_wavemusic_star_wavemusic_on_github_or_leave_a_review_on))
                    withLink(
                        LinkAnnotation.Url(
                            "https://github.com/petesamuel1120-dotcom/WaveMusic/issues",
                            TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = seed)),
                        ) {
                            onDoneReview.invoke()
                            onDismissRequest.invoke()
                            uriHandler.openUri("https://github.com/petesamuel1120-dotcom/WaveMusic/issues")
                        },
                    ) {
                        append(" GitHub")
                    }
                    append("\n")
                    append(stringResource(Res.string.if_you_love_my_work_consider))
                    withLink(
                        LinkAnnotation.Url(
                            "https://github.com/petesamuel1120-dotcom/WaveMusic",
                            TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = seed)),
                        ) {
                            onDoneReview.invoke()
                            onDismissRequest.invoke()
                            uriHandler.openUri("https://github.com/petesamuel1120-dotcom/WaveMusic")
                        },
                    ) {
                        append(stringResource(Res.string.buying_me_a_coffee))
                    }
                },
                textAlign = TextAlign.Center,
                style = typo().bodySmall,
            )
        },
    )
}
