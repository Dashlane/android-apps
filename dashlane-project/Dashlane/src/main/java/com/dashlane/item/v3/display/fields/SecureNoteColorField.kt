package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dashlane.R
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.DropdownField
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownItemContent
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getLabelId
import com.dashlane.xml.domain.SyncObject

@Composable
fun SecureNoteColorField(
    secureNoteType: SyncObject.SecureNoteType,
    editMode: Boolean,
    isEditable: Boolean,
    onSecureNoteTypeChanged: (SyncObject.SecureNoteType) -> Unit
) {
    if (editMode) {
        SecureNoteColorPicker(secureNoteType, isEditable, onSecureNoteTypeChanged)
    } else {
        SecureNoteColorDisplay(secureNoteType)
    }
}

@Composable
fun SecureNoteColorPicker(
    secureNoteType: SyncObject.SecureNoteType,
    isEditable: Boolean,
    onSecureNoteTypeChanged: (SyncObject.SecureNoteType) -> Unit
) {
    val expandedState = remember { mutableStateOf(false) }
    DropdownField(
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(id = R.string.toolbar_menu_title_secure_note_color),
        value = stringResource(secureNoteType.getLabelId()),
        readOnly = !isEditable,
        expandedState = expandedState,
    ) {
        SyncObject.SecureNoteType.entries.filter { it != SyncObject.SecureNoteType.NO_TYPE }.forEach { type ->
            DropdownItem(
                content = {
                    DropdownItemContent(
                        leadingIcon = {
                            Thumbnail(
                                type = ThumbnailType.VaultItem.LegacyOtherIcon(
                                    token = IconTokens.itemColorOutlined,
                                    color = colorResource(id = type.getColorId())
                                ),
                                size = ThumbnailSize.Small
                            )
                        },
                        text = stringResource(id = type.getLabelId())
                    )
                },
                onClick = {
                    onSecureNoteTypeChanged(type)
                    expandedState.value = !expandedState.value
                }
            )
        }
    }
}

@Composable
fun SecureNoteColorDisplay(secureNoteType: SyncObject.SecureNoteType) {
    DisplayField(
        label = stringResource(id = R.string.toolbar_menu_title_secure_note_color),
        value = stringResource(id = secureNoteType.getLabelId())
    )
}

class SecureNoteTypeProvider : PreviewParameterProvider<SyncObject.SecureNoteType> {
    override val values: Sequence<SyncObject.SecureNoteType>
        get() = SyncObject.SecureNoteType.entries.asSequence()
}

@Preview
@Composable
private fun SecureNoteColorFieldPreview(@PreviewParameter(SecureNoteTypeProvider::class) secureNoteType: SyncObject.SecureNoteType) {
    DashlanePreview {
        SecureNoteColorField(
            secureNoteType = secureNoteType,
            editMode = false,
            isEditable = true,
            onSecureNoteTypeChanged = {}
        )
    }
}