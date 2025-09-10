package com.dashlane.item.v3.display.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.fields.CollectionsField
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.ItemEditColorDropdown
import com.dashlane.item.v3.display.fields.ItemEditColorDropdownEntry
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.display.fields.SecureNoteColorField
import com.dashlane.item.v3.display.fields.SpaceField
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.getColorName
import com.dashlane.vault.model.getColorResource
import com.dashlane.xml.domain.SyncObject

@Composable
fun OrganisationSection(
    data: Data<out FormData>,
    editMode: Boolean,
    onItemNameChanged: (String) -> Unit = {},
    onSpaceChanged: (TeamSpace) -> Unit = {},
    onCollectionOpen: () -> Unit = {},
    onSecureNoteTypeChanged: (SyncObject.SecureNoteType) -> Unit = {},
    onCreditCardColorChanged: (String) -> Unit = {},
) {
    SectionContent(showCard = !editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_organisation), editMode = editMode)
        OrganisationSectionContent(
            editMode,
            data,
            onItemNameChanged,
            onSecureNoteTypeChanged,
            onCreditCardColorChanged,
            onSpaceChanged,
            onCollectionOpen,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun ColumnScope.OrganisationSectionContent(
    editMode: Boolean,
    data: Data<out FormData>,
    onItemNameChanged: (String) -> Unit,
    onSecureNoteTypeChanged: (SyncObject.SecureNoteType) -> Unit,
    onCreditCardColorChanged: (String) -> Unit,
    onSpaceChanged: (TeamSpace) -> Unit,
    onCollectionOpen: () -> Unit,
) {
    if (editMode && data.formData is CredentialFormData) {
        GenericField(
            label = stringResource(id = R.string.authentifiant_hint_name),
            data = data.commonData.name,
            editMode = true,
            isEditable = data.commonData.isEditable,
            onValueChanged = onItemNameChanged,
        )
    }
    
    if (data.formData is SecureNoteFormData) {
        SecureNoteColorField(
            secureNoteType = data.formData.secureNoteType,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            onSecureNoteTypeChanged = { onSecureNoteTypeChanged(it) },
        )
    }
    if (data.formData is CreditCardFormData) {
        GenericField(
            label = stringResource(id = R.string.credit_card_hint_name),
            data = data.commonData.name,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            onValueChanged = onItemNameChanged,
            imeAction = ImeAction.Next,
        )

        if (editMode) {
            val resources = LocalContext.current.resources
            val entries = remember {
                (SyncObject.PaymentCreditCard.Color.entries - SyncObject.PaymentCreditCard.Color.NO_TYPE)
                    .map {
                        ItemEditColorDropdownEntry(
                            id = it.value,
                            label = resources.getString(it.getColorName()),
                            color = it.getColorResource(),
                        )
                    }
                    .sortedWith(compareBy { it.label })
            }
            ItemEditColorDropdown(
                label = stringResource(R.string.creditcard_color_header),
                selectedId = data.formData.colorId,
                entries = entries,
                isEditable = data.commonData.isEditable,
                onSelectColorId = onCreditCardColorChanged,
            )
        }
    }
    if (editMode && data.formData is WiFiFormData) {
        GenericField(
            label = stringResource(id = R.string.wifi_hint_name),
            data = data.commonData.name,
            editMode = true,
            isEditable = data.commonData.isEditable,
            onValueChanged = onItemNameChanged,
            keyboardType = KeyboardType.Unspecified,
            imeAction = ImeAction.Next,
        )
    }
    if (data.commonData.space != null && data.commonData.availableSpaces.size > 1) {
        SpaceField(
            space = data.commonData.space,
            availableSpaces = data.commonData.availableSpaces,
            editMode = editMode,
            isEditable = data.commonData.isEditable && !data.commonData.isForcedSpace,
            onSpaceSelected = onSpaceChanged,
        )
    }
    if (data.commonData.collections != null) {
        CollectionsField(
            data = data,
            editMode = editMode,
            onCollectionOpen = onCollectionOpen,
        )
    }
}

private val previewCommonData = CommonData(
    name = "My item",
    space = TeamSpace.Personal,
    availableSpaces = listOf(TeamSpace.Personal),
    collections = listOf(
        CollectionData("", "Collection 1", false),
        CollectionData("", "Collection 2", false),
        CollectionData("", "Collection 3", true),
    ),
    isEditable = true,
)

@Preview
@Composable
private fun OrganisationSectionPreview() {
    DashlanePreview {
        OrganisationSection(
            data = Data(
                formData = CredentialFormData(),
                commonData = previewCommonData,
            ),
            editMode = false,
        )
    }
}

@Preview
@Composable
private fun OrganisationSectionEditPreview() {
    DashlanePreview {
        OrganisationSection(
            data = Data(
                formData = CredentialFormData(),
                commonData = previewCommonData,
            ),
            editMode = true,
        )
    }
}

@Preview
@Composable
private fun OrganisationSectionEditSecureNotePreview() {
    DashlanePreview {
        OrganisationSection(
            data = Data(
                formData = SecureNoteFormData(
                    secureNoteType = SyncObject.SecureNoteType.PURPLE,
                ),
                commonData = previewCommonData,
            ),
            editMode = true,
        )
    }
}