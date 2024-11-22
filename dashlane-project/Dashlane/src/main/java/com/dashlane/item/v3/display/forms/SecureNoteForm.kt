package com.dashlane.item.v3.display.forms

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.fields.DeleteItemField
import com.dashlane.item.v3.display.sections.AttachmentsSection
import com.dashlane.item.v3.display.sections.NoteDetailSection
import com.dashlane.item.v3.display.sections.OrganisationSection
import com.dashlane.item.v3.display.sections.SecureNoteSettingsSection
import com.dashlane.item.v3.display.sharedAccessSectionItem
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.Datas
import com.dashlane.item.v3.viewmodels.FormState
import com.dashlane.item.v3.viewmodels.SecureNoteItemEditViewModel
import com.dashlane.teamspaces.model.TeamSpace

@Suppress("LongMethod")
fun LazyListScope.secureNoteForm(
    viewModel: SecureNoteItemEditViewModel,
    uiState: FormState<SecureNoteFormData>
) {
    secureNoteFormContent(
        uiState = uiState,
        onDeleteAction = {
            viewModel.actionDelete()
        },
        organisationActions = OrganisationActions( 
            onValueChanged = { value ->
                viewModel.updateCommonDataFromView(value)
            },
            onCollectionOpen = {
                viewModel.actionOpenCollection()
            },
            onCollectionRemove = {
                viewModel.removeCollection(it)
            },
            onSecureNoteTypeChanged = {
                viewModel.updateSecureNoteType(it)
            }
        ),
        noteActions = NoteActions(
            onValueChanged = {
                viewModel.updateCurrentDataFromView(it)
            },
            onSecureNoteContentChanged = {
                viewModel.onSecureNoteContentChanged(it)
            },
            onSecuredChanged = {
                viewModel.onSecuredChanged(it)
            }
        ),
        onViewAttachments = viewModel::onViewAttachments,
        onSharedClick = {
            viewModel.actionOpenShared()
        }
    )
}

private fun LazyListScope.secureNoteFormContent(
    uiState: FormState<SecureNoteFormData>,
    organisationActions: OrganisationActions,
    onDeleteAction: () -> Unit,
    noteActions: NoteActions,
    onViewAttachments: () -> Unit,
    onSharedClick: () -> Unit,
) {
    item(key = "NoteDetailSection") {
        NoteDetailSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            noteActions = noteActions
        )
    }
    item(key = "OrganisationSection") {
        OrganisationSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            organisationActions = organisationActions
        )
    }
    item(key = "AttachmentsSection") {
        AttachmentsSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            onViewAttachments = onViewAttachments
        )
    }
    sharedAccessSectionItem(uiState, onSharedClick)
    item(key = "SecureNoteSettingsSection") {
        SecureNoteSettingsSection(data = uiState.datas.current, editMode = uiState.isEditMode) { secured ->
            noteActions.onSecuredChanged(secured)
        }
    }
    item(key = "DeleteLoginField") {
        DeleteItemField(
            canDelete = uiState.datas.current.commonData.canDelete,
            editMode = uiState.isEditMode,
            label = stringResource(id = R.string.vault_delete_secure_note),
            onDeleteAction = onDeleteAction
        )
    }
}

data class NoteActions(
    val onValueChanged: (Data<SecureNoteFormData>) -> Unit = {},
    val onSecuredChanged: (Boolean) -> Unit = {},
    val onSecureNoteContentChanged: (String) -> Unit = {}
)

@Composable
@Preview
private fun SecureNoteFormContentPreview() {
    DashlanePreview {
        LazyColumn(
            content = {
                secureNoteFormContent(
                    uiState = secureNoteFormDemoState(false),
                    organisationActions = OrganisationActions(),
                    onDeleteAction = {},
                    noteActions = NoteActions(),
                    onViewAttachments = {},
                    onSharedClick = {}
                )
            }
        )
    }
}

@Composable
@Preview
private fun SecureNoteFormContentPreviewEdit() {
    DashlanePreview {
        LazyColumn(
            content = {
                secureNoteFormContent(
                    uiState = secureNoteFormDemoState(true),
                    organisationActions = OrganisationActions(),
                    onDeleteAction = {},
                    noteActions = NoteActions(),
                    onViewAttachments = {},
                    onSharedClick = {}
                )
            }
        )
    }
}

private fun secureNoteFormDemoState(isEditMode: Boolean) = FormState(
    datas = Datas(
        current = secureNoteFormDemoData(),
        initial = secureNoteFormDemoData()
    ),
    isNew = false,
    isEditMode = isEditMode
)

private fun secureNoteFormDemoData() = Data(
    commonData = CommonData(
        name = "Name",
        space = TeamSpace.Personal
    ),
    formData = SecureNoteFormData(
        content = "Lorem ipsum dolor sit amet.",
    )
)
