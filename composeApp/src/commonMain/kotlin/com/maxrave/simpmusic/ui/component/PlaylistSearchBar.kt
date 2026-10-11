package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.search

/** One field for the portrait overlay and the center slot of the landscape toolbar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSearchBar(
    backdrop: PlatformBackdrop,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    // The shared header can attach after its destination. Request focus from the
    // field itself, once its focus target exists, rather than from the page owner.
    LaunchedEffect(expanded) {
        if (expanded) {
            withFrameNanos { }
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
    val landscape = isCompactLandscapeChrome()
    val headerHost = LocalSharedHeaderState.current
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry
    val fieldToken = remember { Any() }
    if (!landscape && headerHost != null && entry != null) {
        // Register presence, not focus/expanded state: the field is still visible while exiting.
        // Every playlist overlay now reserves the same paint boundary without extra list padding.
        DisposableEffect(headerHost, entry.id, fieldToken) {
            headerHost.registerPortraitSearch(entry.id, fieldToken)
            onDispose { headerHost.unregisterPortraitSearch(entry.id, fieldToken) }
        }
    }
    val colors = appSearchBarColors(transparentContainer = LocalLiquidGlassEnabled.current)
    val fieldModifier = Modifier
        .fillMaxWidth()
        .height(InPageSearchBarHeight)
        .padding(horizontal = if (landscape) 0.dp else 12.dp)
        .then(
            if (LocalLiquidGlassEnabled.current) Modifier.searchFieldGlass(backdrop)
            else Modifier,
        )
    AppSearchBarTheme(compact = true) {
        val input: @Composable () -> Unit = {
            CompositionLocalProvider(LocalTextStyle provides inPageSearchBarTextStyle(typo().bodyLarge)) {
                InPageSearchInputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
                    placeholder = {
                        Text(stringResource(Res.string.search), style = inPageSearchBarTextStyle(typo().bodyLarge))
                    },
                    leadingIcon = { Icon(SimpIcons.Search, contentDescription = null) },
                )
            }
        }
        if (landscape) {
            // Skip Material's extra top inset as well as its tall input padding.
            Box(fieldModifier.background(colors.containerColor, CircleShape)) { input() }
        } else {
            SearchBar(
                modifier = fieldModifier,
                colors = colors,
                shape = CircleShape,
                inputField = input,
                expanded = false,
                onExpandedChange = {},
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {}
        }
    }
}
