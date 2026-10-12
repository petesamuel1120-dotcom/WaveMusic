package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Resolve the description in composition so language changes also update accessibility. */
@Composable
internal fun Modifier.localizedDescription(resource: StringResource): Modifier {
    val description = stringResource(resource)
    return semantics { contentDescription = description }
}
