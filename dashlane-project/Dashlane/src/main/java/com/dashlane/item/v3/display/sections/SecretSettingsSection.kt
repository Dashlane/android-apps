package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.dashlane.R
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.ui.widgets.compose.SettingField

@Composable
fun SecretSettingsSection(
    data: Data<SecretFormData>,
    editMode: Boolean,
    onSecuredChange: (Boolean) -> Unit,
) {
    if (!editMode || !data.formData.secureSettingAvailable) return
    SectionContent(editMode = editMode) {
        SectionTitle(
            title = stringResource(id = R.string.vault_preferences),
            editMode = editMode
        )
        SettingField(
            title = stringResource(id = R.string.vault_secret_setting_secured_title),
            description = stringResource(id = R.string.vault_secret_setting_secured_description),
            checked = data.formData.secured,
            onCheckedChange = onSecuredChange
        )
    }
}

@Preview
@Composable
private fun SecretSettingsSectionPreview(@PreviewParameter(BooleanProvider::class) secured: Boolean) {
    DashlanePreview {
        SecretSettingsSection(
            data = Data(
                commonData = CommonData(),
                formData = SecretFormData(secured = secured)
            ),
            editMode = true,
            onSecuredChange = {}
        )
    }
}