package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.forms.OrganisationActions
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.ui.widgets.view.CategoryChip
import com.dashlane.ui.widgets.view.CategoryChipList

@Composable
fun ColumnScope.CollectionsField(
    data: Data<out FormData>,
    editMode: Boolean,
    organisationActions: OrganisationActions
) {
    if (data.commonData.attachmentCount > 0) {
        if (data.formData is SecureNoteFormData) {
            Text(
                text = stringResource(id = R.string.vault_secure_note_attachment_restrict_collections),
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textNeutralQuiet
            )
        } 
        return
    }
    if (editMode) {
        CollectionFieldEdit(
            data,
            organisationActions.onCollectionOpen,
            organisationActions.onCollectionRemove
        )
    } else {
        CollectionsFieldDisplay(data, organisationActions.onCollectionOpen)
    }
}

@Composable
private fun ColumnScope.CollectionFieldEdit(
    data: Data<out FormData>,
    onCollectionOpen: () -> Unit,
    onCollectionRemove: (CollectionData) -> Unit
) {
    val collections = data.commonData.collections ?: return
    if (collections.isNotEmpty()) {
        CategoryChipList {
            collections.forEach { collection ->
                CategoryChip(
                    label = collection.name,
                    editMode = true,
                    shared = collection.shared,
                    onClick = {
                        onCollectionRemove(collection)
                    }
                )
            }
        }
    }
    LinkButton(
        modifier = Modifier
            .align(Alignment.End),
        text = if (collections.isEmpty()) {
            stringResource(id = R.string.vault_add_collection)
        } else {
            stringResource(id = R.string.vault_manage_collections)
        },
        destinationType = LinkButtonDestinationType.INTERNAL,
        onClick = {
            onCollectionOpen()
        },
    )
}

@SuppressWarnings("LongMethod")
@Composable
private fun CollectionsFieldDisplay(data: Data<out FormData>, onCollectionOpen: () -> Unit) {
    val collections = data.commonData.collections ?: return
    if (collections.isEmpty()) {
        DisplayField(
            label = stringResource(id = R.string.vault_collection_title),
            placeholder = stringResource(id = R.string.vault_no_collection),
            value = null,
            omitActionsOnEmptyState = false,
            actions = DisplayFieldActions.newInstance(
                FieldAction.Generic(
                    iconLayout = ButtonLayout.IconOnly(
                        iconToken = IconTokens.actionAddOutlined,
                        contentDescription = stringResource(id = R.string.vault_add_collection)
                    ),
                    onClick = {
                        onCollectionOpen()
                        true
                    }
                )
            ),
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    text = stringResource(id = R.string.vault_collection_title),
                    style = DashlaneTheme.typography.bodyHelperRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                CategoryChipList {
                    collections.forEach { collection ->
                        CategoryChip(
                            label = collection.name,
                            editMode = false,
                            shared = collection.shared,
                            onClick = {
                            }
                        )
                    }
                }
            }
        }
        LinkButton(
            text = stringResource(id = R.string.vault_manage_collections),
            destinationType = LinkButtonDestinationType.INTERNAL,
            onClick = {
                onCollectionOpen()
            },
        )
    }
}

@Preview
@Composable
private fun CollectionFieldPreview() {
    DashlanePreview {
        Column {
            CollectionsField(
                data = Data(
                    formData = CredentialFormData(),
                    commonData = CommonData(
                        collections = listOf(
                            CollectionData("", "Collection 1", false),
                            CollectionData("", "Collection 2", true)
                        )
                    )
                ),
                editMode = false,
                organisationActions = OrganisationActions()
            )
        }
    }
}

@Preview
@Composable
private fun RestrictedCollectionFieldPreview() {
    DashlanePreview {
        Column {
            CollectionsField(
                data = Data(
                    formData = SecureNoteFormData(),
                    commonData = CommonData(
                        attachmentCount = 2
                    )
                ),
                editMode = false,
                organisationActions = OrganisationActions()
            )
        }
    }
}