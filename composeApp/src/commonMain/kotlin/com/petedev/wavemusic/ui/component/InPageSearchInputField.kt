package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/** Center short search pills without Material's fixed vertical input padding. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InPageSearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    centeredContent: Boolean = false,
) {
    val compactLandscape = isCompactLandscapeChrome()
    if (!compactLandscape && !centeredContent) {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = modifier,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
        return
    }
    val contentColor = appSearchBarContentColor()
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = contentColor),
        singleLine = true,
        cursorBrush = SolidColor(contentColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        decorationBox = { innerTextField ->
            Row(
                Modifier.fillMaxSize().padding(horizontal = if (compactLandscape) 14.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Box(Modifier.size(if (compactLandscape) 20.dp else 24.dp), contentAlignment = Alignment.Center) { leadingIcon() }
                    Spacer(Modifier.width(12.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) placeholder?.invoke()
                    innerTextField()
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) { trailingIcon() }
                }
            }
        },
    )
}
