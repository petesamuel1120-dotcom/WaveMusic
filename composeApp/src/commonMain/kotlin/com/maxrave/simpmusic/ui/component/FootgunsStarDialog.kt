package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.footguns_promo_message
import simpmusic.composeapp.generated.resources.footguns_promo_title
import simpmusic.composeapp.generated.resources.give_a_star
import simpmusic.composeapp.generated.resources.later

private const val FOOTGUNS_REPO_URL = "https://github.com/maxrave-dev/kotlin-footguns"

// GitHub renders this on demand for any public repo - repo name, description, avatar and the LIVE
// star count, so the banner sells itself and stays current without shipping an asset.
private const val FOOTGUNS_SOCIAL_IMAGE_URL = "https://opengraph.githubassets.com/1/maxrave-dev/kotlin-footguns"

// The rendered image is 1200x600.
private const val SOCIAL_IMAGE_ASPECT_RATIO = 2f

@Composable
@ExperimentalMaterial3Api
fun FootgunsStarDialog(
    onDismissRequest: () -> Unit,
    onDoneStar: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    // Offline, or GitHub unreachable: drop the banner rather than leave a blank block above the text.
    var bannerFailed by remember { mutableStateOf(false) }
    AlertDialog(
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
                onDoneStar.invoke()
                uriHandler.openUri(FOOTGUNS_REPO_URL)
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
        title = {
            Text(
                stringResource(Res.string.footguns_promo_title),
                style = typo().labelSmall,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!bannerFailed) {
                    AsyncImage(
                        model = FOOTGUNS_SOCIAL_IMAGE_URL,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        onState = { state ->
                            if (state is AsyncImagePainter.State.Error) bannerFailed = true
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(SOCIAL_IMAGE_ASPECT_RATIO)
                                .clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.height(14.dp))
                }
                Text(
                    stringResource(Res.string.footguns_promo_message),
                    textAlign = TextAlign.Center,
                    style = typo().bodySmall,
                )
            }
        },
    )
}
