package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.DropdownField
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownItemContent
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.xml.domain.SyncObject

@Composable
fun ItemEditColorDropdown(
    label: String,
    selectedId: String,
    entries: List<ItemEditColorDropdownEntry>,
    isEditable: Boolean,
    onSelectColorId: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedEntry = remember(entries, selectedId) { entries.find { it.id == selectedId } }

    DropdownField(
        modifier = Modifier.fillMaxWidth(),
        label = label,
        value = selectedEntry?.label ?: "",
        readOnly = !isEditable,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        SyncObject.SecureNoteType.entries.filter { it != SyncObject.SecureNoteType.NO_TYPE }
        entries.forEach { entry ->
            DropdownItem(
                content = {
                    DropdownItemContent(
                        leadingIcon = {
                            Thumbnail(
                                type = ThumbnailType.VaultItem.LegacyOtherIcon(
                                    token = IconTokens.itemColorOutlined,
                                    color = colorResource(id = entry.color),
                                ),
                                size = ThumbnailSize.Small,
                            )
                        },
                        text = entry.label,
                    )
                },
                onClick = {
                    onSelectColorId(entry.id)
                    expanded = !expanded
                },
            )
        }
    }
}

data class ItemEditColorDropdownEntry(
    val id: String,
    val label: String,
    val color: Int,
)

@Preview
@Composable
private fun ItemEditColorDropdownPreview() = DashlanePreview {
    ItemEditColorDropdown(
        label = "Color",
        selectedId = "1",
        entries = listOf(
            ItemEditColorDropdownEntry("1", "Entry 1", R.color.securenote_color_red),
            ItemEditColorDropdownEntry("2", "Entry 2", R.color.securenote_color_blue),
            ItemEditColorDropdownEntry("3", "Entry 3", R.color.securenote_color_pink),
        ),
        isEditable = true,
        onSelectColorId = {},
    )
}