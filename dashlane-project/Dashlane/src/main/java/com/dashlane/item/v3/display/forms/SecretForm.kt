package com.dashlane.item.v3.display.forms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.display.fields.DeleteItemField
import com.dashlane.item.v3.display.sections.AttachmentsSection
import com.dashlane.item.v3.display.sections.OrganisationSection
import com.dashlane.item.v3.display.sections.SecretDetailSection
import com.dashlane.item.v3.display.sections.SecretSettingsSection
import com.dashlane.item.v3.display.sharedAccessSectionItem
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.Datas
import com.dashlane.item.v3.viewmodels.FormState
import com.dashlane.item.v3.viewmodels.SecretItemEditViewModel
import com.dashlane.teamspaces.model.TeamSpace

@Suppress("LongMethod")
fun LazyListScope.secretForm(
    viewModel: SecretItemEditViewModel,
    uiState: FormState<SecretFormData>,
) {
    secretFormContent(
        uiState = uiState,
        onDeleteAction = viewModel::actionDelete,
        onTitleChanged = viewModel::onTitleChanged,
        onContentChanged = viewModel::onContentChanged,
        onSecuredChange = viewModel::onSecuredChanged,
        onSharedClick = viewModel::actionOpenShared,
        onViewAttachments = viewModel::onViewAttachments,
        onCopyContent = viewModel::copySecretContent,
        onItemNameChanged = viewModel::updateItemName,
        onSpaceChanged = viewModel::updateSpace,
    )
}

private fun LazyListScope.secretFormContent(
    uiState: FormState<SecretFormData>,
    onDeleteAction: () -> Unit,
    onSharedClick: () -> Unit,
    onCopyContent: () -> Unit,
    onItemNameChanged: (String) -> Unit,
    onSpaceChanged: (TeamSpace) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onSecuredChange: (Boolean) -> Unit,
    onViewAttachments: () -> Unit,
) {
    item(key = "SecretDetailSection") {
        SecretDetailSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            onTitleChanged = onTitleChanged,
            onContentChanged = onContentChanged,
            onCopyContent = onCopyContent,
        )
    }
    item(key = "OrganisationSection") {
        OrganisationSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            onItemNameChanged = onItemNameChanged,
            onSpaceChanged = onSpaceChanged,
        )
    }
    item(key = "AttachmentsSection") {
        AttachmentsSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            onViewAttachments = onViewAttachments,
        )
    }
    sharedAccessSectionItem(uiState, onSharedClick)
    item(key = "SecureNoteSettingsSection") {
        SecretSettingsSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            onSecuredChange = onSecuredChange,
        )
        Spacer(Modifier.height(8.dp))
    }
    item(key = "DeleteSecretField") {
        DeleteItemField(
            canDelete = uiState.datas.current.commonData.canDelete,
            editMode = uiState.isEditMode,
            label = stringResource(id = R.string.vault_delete_secret),
            onDeleteAction = onDeleteAction,
        )
    }
}

@Composable
@Preview
private fun SecureNoteFormContentPreview() {
    DashlanePreview {
        LazyColumn {
            secretFormContent(
                uiState = secretNoteFormDemoState(false),
                onDeleteAction = {},
                onSharedClick = {},
                onTitleChanged = {},
                onContentChanged = {},
                onSecuredChange = {},
                onCopyContent = {},
                onViewAttachments = {},
                onItemNameChanged = {},
                onSpaceChanged = {},
            )
        }
    }
}

@Composable
@Preview
private fun SecureNoteFormContentPreviewEdit() {
    DashlanePreview {
        LazyColumn(
            content = {
                secretFormContent(
                    uiState = secretNoteFormDemoState(true),
                    onDeleteAction = {},
                    onSharedClick = {},
                    onTitleChanged = {},
                    onContentChanged = {},
                    onSecuredChange = {},
                    onCopyContent = {},
                    onViewAttachments = {},
                    onItemNameChanged = {},
                    onSpaceChanged = {},
                )
            },
        )
    }
}

private fun secretNoteFormDemoState(isEditMode: Boolean) = FormState(
    datas = Datas(
        current = secretFormDemoData(),
        initial = secretFormDemoData(),
    ),
    isNew = false,
    isEditMode = isEditMode,
)

private fun secretFormDemoData() = Data(
    commonData = CommonData(
        name = "Name",
        space = TeamSpace.Personal,
        canDelete = true,
    ),
    formData = SecretFormData(
        content = "Lorem ipsum dolor sit amet.",
    ),
)