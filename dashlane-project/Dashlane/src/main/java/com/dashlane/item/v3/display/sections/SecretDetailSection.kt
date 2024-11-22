package com.dashlane.item.v3.display.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.viewmodels.Data

@SuppressWarnings("LongMethod")
@Composable
fun SecretDetailSection(
    data: Data<SecretFormData>,
    editMode: Boolean,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onCopyContent: () -> Unit,
) {
    if (!editMode) {
        InfoboxMedium(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            title = stringResource(R.string.sharing_secret_on_web)
        )
    }

    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_secret_detail_section_title), editMode = editMode)
        GenericField(
            label = stringResource(id = R.string.secret_hint_title),
            data = data.commonData.name,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            onValueChanged = onTitleChanged,
        )
        GenericField(
            label = stringResource(id = R.string.secret_hint_content),
            data = data.formData.content,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            multiLine = editMode,
            onValueChanged = onContentChanged,
            onValueCopy = onCopyContent,
        )
    }
}
