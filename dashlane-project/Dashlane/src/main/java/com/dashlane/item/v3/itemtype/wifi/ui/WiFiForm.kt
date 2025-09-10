package com.dashlane.item.v3.itemtype.wifi.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.display.fields.DeleteItemField
import com.dashlane.item.v3.display.sections.OrganisationSection
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiItemEditAction
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.Datas
import com.dashlane.item.v3.viewmodels.FormState
import com.dashlane.teamspaces.model.TeamSpace

fun LazyListScope.wiFiForm(
    uiState: FormState<WiFiFormData>,
    handleAction: (WiFiItemEditAction) -> Unit,
) {
    val data = uiState.datas.current

    if (data.formData.isAlreadyConnectedInfoBoxVisible) {
        item {
            InfoboxMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                title = stringResource(R.string.wifi_connect_to_network_already_connected),
                mood = Mood.Brand,
            )
        }
    }

    item(key = "WiFiDetailSection") {
        WiFiDetailSection(
            data = data,
            editMode = uiState.isEditMode,
            hiddenFields = uiState.hiddenFields,
            revealedFields = uiState.revealedFields,
            handleAction = handleAction,
        )
    }
    
    if (uiState.isEditMode || (data.commonData.space != null && data.commonData.availableSpaces.size > 1)) {
        item(key = "OrganisationSection") {
            OrganisationSection(
                data = uiState.datas.current,
                editMode = uiState.isEditMode,
                onItemNameChanged = {
                    handleAction(WiFiItemEditAction.NameChanged(it))
                },
                onSpaceChanged = {
                    handleAction(WiFiItemEditAction.SpaceChanged(it))
                },
            )
        }
    }
    item(key = "DeleteSection") {
        DeleteItemField(
            canDelete = data.commonData.canDelete,
            editMode = uiState.isEditMode,
            label = stringResource(id = R.string.vault_delete_wifi),
            onDeleteAction = { handleAction(WiFiItemEditAction.Delete) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WiFiFormEditModePreview() = DashlaneTheme {
    val data = Data(
        commonData = CommonData(
            isEditable = true,
            canDelete = true,
            space = TeamSpace.Personal,
            availableSpaces = listOf(TeamSpace.Personal),
        ),
        formData = WiFiFormData(),
    )

    LazyColumn(Modifier.fillMaxSize()) {
        wiFiForm(
            uiState = FormState(
                datas = Datas(
                    initial = data,
                    current = data,
                ),
                isNew = false,
                isEditMode = true,
            ),
            handleAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WiFiFormViewModePreview() = DashlaneTheme {
    val data = Data(
        commonData = CommonData(),
        formData = WiFiFormData(
            isAlreadyConnectedInfoBoxVisible = true,
        ),
    )

    LazyColumn(Modifier.fillMaxSize()) {
        wiFiForm(
            uiState = FormState(
                datas = Datas(
                    initial = data,
                    current = data,
                ),
                isNew = false,
                isEditMode = false,
            ),
            handleAction = {},
        )
    }
}