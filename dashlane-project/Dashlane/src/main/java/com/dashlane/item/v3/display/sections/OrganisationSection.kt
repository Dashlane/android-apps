package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.display.fields.SecureNoteColorField
import com.dashlane.item.v3.display.fields.SpaceField
import com.dashlane.item.v3.display.forms.OrganisationActions
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.xml.domain.SyncObject

@Composable
fun OrganisationSection(
    data: Data<out FormData>,
    editMode: Boolean,
    organisationActions: OrganisationActions,
) {
    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_organisation), editMode = editMode)
        if (editMode && data.formData is CredentialFormData) {
            GenericField(
                label = stringResource(id = R.string.authentifiant_hint_name),
                data = data.commonData.name,
                editMode = true,
                isEditable = data.commonData.isEditable,
                onValueChanged = { value ->
                    organisationActions.onValueChanged(data.commonData.copy(name = value))
                }
            )
        }
        
        if (data.formData is SecureNoteFormData) {
            SecureNoteColorField(
                secureNoteType = data.formData.secureNoteType,
                editMode = editMode,
                isEditable = data.commonData.isEditable
            ) {
                organisationActions.onSecureNoteTypeChanged(it)
            }
        }
        SpaceField(
            commonData = data.commonData,
            editMode = editMode,
            isEditable = data.commonData.isEditable && !data.commonData.isForcedSpace,
            onSpaceSelected = { value ->
                organisationActions.onValueChanged(
                    data.commonData.copy(
                        space = value,
                        collections = data.commonData.collections?.take(0)
                    )
                )
            }
        )
        if (data.commonData.collections != null) {
            CollectionsField(data = data, editMode = editMode, organisationActions = organisationActions)
        }
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
    isEditable = true
)

@Preview
@Composable
private fun OrganisationSectionPreview() {
    DashlanePreview {
        OrganisationSection(
            data = Data(
                formData = CredentialFormData(),
                commonData = previewCommonData
            ),
            editMode = false,
            organisationActions = OrganisationActions()
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
                commonData = previewCommonData
            ),
            editMode = true,
            organisationActions = OrganisationActions()
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
                    secureNoteType = SyncObject.SecureNoteType.PURPLE
                ),
                commonData = previewCommonData
            ),
            editMode = true,
            organisationActions = OrganisationActions()
        )
    }
}