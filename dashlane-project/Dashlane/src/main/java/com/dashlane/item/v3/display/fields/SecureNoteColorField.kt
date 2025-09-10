package com.dashlane.item.v3.display.fields

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dashlane.R
import com.dashlane.design.component.DisplayField
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getLabelId
import com.dashlane.xml.domain.SyncObject

@Composable
fun SecureNoteColorField(
    secureNoteType: SyncObject.SecureNoteType,
    editMode: Boolean,
    isEditable: Boolean,
    onSecureNoteTypeChanged: (SyncObject.SecureNoteType) -> Unit,
) {
    val resources = LocalContext.current.resources
    if (editMode) {
        ItemEditColorDropdown(
            label = stringResource(id = R.string.toolbar_menu_title_secure_note_color),
            selectedId = secureNoteType.value,
            entries = remember(resources) {
                SyncObject.SecureNoteType.entries
                    .filter { it != SyncObject.SecureNoteType.NO_TYPE }
                    .map {
                        ItemEditColorDropdownEntry(
                            id = it.value,
                            label = resources.getString(it.getLabelId()),
                            color = it.getColorId(),
                        )
                    }
                    .sortedBy { it.label }
            },
            isEditable = isEditable,
            onSelectColorId = { selected ->
                onSecureNoteTypeChanged(SyncObject.SecureNoteType.entries.first { it.value == selected })
            },
        )
    } else {
        SecureNoteColorDisplay(secureNoteType)
    }
}

@Composable
private fun SecureNoteColorDisplay(secureNoteType: SyncObject.SecureNoteType) {
    DisplayField(
        label = stringResource(id = R.string.toolbar_menu_title_secure_note_color),
        value = stringResource(id = secureNoteType.getLabelId()),
    )
}

private class SecureNoteTypeProvider : PreviewParameterProvider<SyncObject.SecureNoteType> {
    override val values: Sequence<SyncObject.SecureNoteType>
        get() = SyncObject.SecureNoteType.entries.asSequence()
}

@Preview
@Composable
private fun SecureNoteColorFieldPreview(
    @PreviewParameter(SecureNoteTypeProvider::class) secureNoteType: SyncObject.SecureNoteType,
) {
    DashlanePreview {
        SecureNoteColorField(
            secureNoteType = secureNoteType,
            editMode = false,
            isEditable = true,
            onSecureNoteTypeChanged = {},
        )
    }
}