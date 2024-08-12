package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.display.fields.CollectionsField
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.display.fields.SpaceField
import com.dashlane.item.v3.display.forms.OrganisationActions
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.runIfNull

@Composable
fun OrganisationSection(
    data: CredentialFormData,
    editMode: Boolean,
    organisationActions: OrganisationActions,
) {
    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_organisation), editMode = editMode)
        if (editMode) {
            GenericField(
                label = stringResource(id = R.string.authentifiant_hint_name),
                data = data.name.runIfNull { data.url },
                editMode = true,
                isEditable = data.isEditable,
                onValueChanged = { value ->
                    organisationActions.onValueChanged(data.copy(name = value))
                }
            )
        }
        SpaceField(
            data = data,
            editMode = editMode,
            isEditable = data.isEditable && !data.isForcedSpace,
            onSpaceSelected = { value ->
                organisationActions.onValueChanged(data.copy(space = value))
            }
        )
        CollectionsField(data = data, editMode = editMode, organisationActions = organisationActions)
        val isShared = data.sharingCount.userCount > 0 || data.sharingCount.groupCount > 0
        if (!editMode && isShared) {
            GenericField(
                label = stringResource(id = R.string.vault_shared_with),
                data = getSharingCount(sharingCount = data.sharingCount),
                editMode = false,
                onValueChanged = {}
            )
            LinkButton(
                text = stringResource(id = R.string.vault_view_all_shared_users),
                destinationType = LinkButtonDestinationType.INTERNAL
            ) {
                organisationActions.onSharedClick()
            }
        }
    }
}

@Composable
private fun getSharingCount(sharingCount: FormData.SharingCount): String {
    val userCount = pluralStringResource(
        R.plurals.sharing_shared_counter_users,
        sharingCount.userCount,
        sharingCount.userCount
    )
    val groupCount = pluralStringResource(
        R.plurals.sharing_shared_counter_groups,
        sharingCount.groupCount,
        sharingCount.groupCount
    )
    return if (sharingCount.userCount != 0 && sharingCount.groupCount != 0) {
        stringResource(R.string.sharing_shared_shared_with_users_and_groups, userCount, groupCount)
    } else if (sharingCount.userCount != 0) {
        stringResource(R.string.sharing_shared_shared_with, userCount)
    } else {
        stringResource(R.string.sharing_shared_shared_with, groupCount)
    }
}

@Preview
@Composable
private fun OrganisationSectionPreview() {
    DashlanePreview {
        OrganisationSection(
            data = CredentialFormData(
                name = "My item",
                space = TeamSpace.Personal,
                collections = listOf(
                    CollectionData("", "Collection 1", false),
                    CollectionData("", "Collection 2", false),
                    CollectionData("", "Collection 3", true),
                ),
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
            data = CredentialFormData(
                name = "My item",
                space = TeamSpace.Personal,
                collections = listOf(
                    CollectionData("", "Collection 1", false),
                    CollectionData("", "Collection 2", false),
                    CollectionData("", "Collection 3", true),
                ),
            ),
            editMode = true,
            organisationActions = OrganisationActions()
        )
    }
}