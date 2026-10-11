package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
// Keep wildcard for Compose Material versions whose exposed-dropdown APIs differ by target.
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.component.AnimatedAppDialog as Dialog
import com.maxrave.simpmusic.ui.theme.typo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownButton(
    defaultSelected: String = "",
    items: List<String>,
    dialogTitle: String = defaultSelected,
    onItemSelected: (String) -> Unit,
) {
    val buttonContainerColor = settingsButtonContainerColor()
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    var selected by rememberSaveable {
        mutableStateOf(defaultSelected)
    }
    val dialogAnchor = rememberDialogMotionAnchor(24.dp)
    val focusManager = LocalFocusManager.current
    val buttonExpanded = expanded && !dialogAnchor.isClosing
    LaunchedEffect(dialogAnchor.isClosing) {
        if (dialogAnchor.isClosing) focusManager.clearFocus()
    }
    Box(Modifier.wrapContentWidth(align = Alignment.CenterHorizontally)) {
        ExposedDropdownMenuBox(expanded = buttonExpanded, onExpandedChange = { new ->
            if (new) dialogAnchor.record()
            expanded = new
        }) {
            OutlinedTextField(
                modifier =
                    Modifier
                        .then(dialogAnchor.modifier)
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .widthIn(1.dp, Dp.Infinity),
                textStyle = typo().bodyMedium,
                readOnly = true,
                value = selected,
                onValueChange = {},
                maxLines = 1,
                // Update the captured control while it is hidden, before it returns from the
                // dialog. Otherwise the arrow flips a second time after the handoff completes.
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buttonExpanded) },
                shape = RoundedCornerShape(40),
                colors =
                    ExposedDropdownMenuDefaults.textFieldColors(
                        focusedIndicatorColor = Gray,
                        unfocusedIndicatorColor = Gray,
                        unfocusedContainerColor = buttonContainerColor,
                        focusedContainerColor = buttonContainerColor,
                    ),
            )
        }
    }
    if (expanded) {
        AppDialogTheme {
            val palette = rememberAppDialogPalette()
            Dialog(onDismissRequest = { expanded = false }) {
                val closeDialog = rememberDialogCloseAction()
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .heightIn(min = 180.dp, max = 560.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(palette.container)
                            .border(1.dp, palette.border, RoundedCornerShape(24.dp)),
                ) {
                    Text(
                        text = dialogTitle,
                        style = typo().titleSmall,
                        color = palette.content,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                            .dialogContentMotion(DialogContentStage.Title),
                    )
                    LazyColumn(modifier = Modifier.weight(1f).dialogContentMotion(DialogContentStage.Body)) {
                        itemsIndexed(items) { index, item ->
                            val isSelected = item == selected
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .dialogContentMotion(DialogContentStage.Option, index)
                                        .clickable {
                                            closeDialog {
                                                selected = item
                                                expanded = false
                                                onItemSelected(item)
                                            }
                                        }
                                        .padding(horizontal = 20.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item,
                                    style = typo().bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else palette.content,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(vertical = 13.dp),
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors =
                                        RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = palette.secondaryContent,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
