package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.DropdownField
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownItemContent
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun ItemEditDropdownField(
    modifier: Modifier = Modifier,
    label: String,
    selectedId: String?,
    entries: List<ItemEditDropdownEntry>,
    editMode: Boolean,
    isEditable: Boolean,
    onValueChanged: (String) -> Unit,
) {
    val value = remember(selectedId, entries) {
        entries.find { it.id == selectedId }?.label ?: ""
    }
    if (editMode) {
        ItemEditDropdownField(
            modifier = modifier.fillMaxWidth(),
            label = label,
            value = value,
            entries = entries,
            isEditable = isEditable,
            onValueChanged = onValueChanged,
        )
    } else {
        DisplayField(
            modifier = modifier.fillMaxWidth(),
            label = label,
            value = value,
        )
    }
}

data class ItemEditDropdownEntry(
    val id: String,
    val label: String,
)

@Composable
private fun ItemEditDropdownField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    entries: List<ItemEditDropdownEntry>,
    isEditable: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownField(
        modifier = modifier.fillMaxWidth(),
        label = label,
        value = value,
        readOnly = !isEditable,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        entries.forEach { entry ->
            DropdownItem(
                content = {
                    DropdownItemContent(
                        leadingIcon = null,
                        text = entry.label,
                    )
                },
                onClick = {
                    onValueChanged(entry.id)
                    expanded = false
                },
            )
        }
    }
}

@Preview
@Composable
private fun ItemEditDropdownFieldViewModePreview() = DashlanePreview {
    ItemEditDropdownField(
        label = "Dropdown Field",
        entries = listOf(
            ItemEditDropdownEntry("1", "Entry 1"),
            ItemEditDropdownEntry("2", "Entry 2"),
            ItemEditDropdownEntry("3", "Entry 3"),
        ),
        selectedId = "1",
        editMode = false,
        isEditable = true,
        onValueChanged = {},
    )
}

@Preview
@Composable
private fun ItemEditDropdownFieldEditModePreview() = DashlanePreview {
    ItemEditDropdownField(
        label = "Dropdown Field",
        entries = listOf(
            ItemEditDropdownEntry("4", "Entry 4"),
            ItemEditDropdownEntry("5", "Entry 5"),
            ItemEditDropdownEntry("6", "Entry 6"),
        ),
        selectedId = "4",
        editMode = true,
        isEditable = true,
        onValueChanged = {},
    )
}