package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.viewmodels.Data

@Composable
fun AttachmentsSection(data: Data<out FormData>, editMode: Boolean, onViewAttachments: () -> Unit) {
    if (editMode || data.commonData.attachmentCount == 0) {
        return
    }
    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_attachments), editMode = editMode)
        GenericField(
            label = stringResource(id = R.string.vault_attached_files_label),
            editMode = editMode,
            data = pluralStringResource(
                id = R.plurals.vault_attached_files,
                count = data.commonData.attachmentCount,
                data.commonData.attachmentCount
            ),
            onValueChanged = {}
        )
        LinkButton(
            text = stringResource(id = R.string.vault_attached_files_view_button),
            destinationType = LinkButtonDestinationType.INTERNAL,
            onClick = {
                onViewAttachments()
            },
        )
    }
}

@Preview
@Composable
private fun AttachmentSectionPreview() {
    DashlanePreview {
        AttachmentsSection(
            data = Data(
                commonData = CommonData(attachmentCount = 2),
                formData = SecureNoteFormData()
            ),
            editMode = false
        ) {}
    }
}