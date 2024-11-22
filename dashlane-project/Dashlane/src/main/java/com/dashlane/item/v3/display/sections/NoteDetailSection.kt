package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.item.v3.data.SecureNoteContentFeedback
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.display.forms.NoteActions
import com.dashlane.item.v3.viewmodels.Data
import java.text.DecimalFormat

@SuppressWarnings("LongMethod")
@Composable
fun NoteDetailSection(data: Data<SecureNoteFormData>, editMode: Boolean, noteActions: NoteActions) {
    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_note_detail_title), editMode = editMode)
        GenericField(
            label = stringResource(id = R.string.secure_note_hint_title),
            data = data.commonData.name,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            onValueChanged = { value ->
                noteActions.onValueChanged(data.copyCommonData { it.copy(name = value) })
            }
        )
        GenericField(
            label = stringResource(id = R.string.secure_note_hint_content),
            data = data.formData.content,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            multiLine = true,
            onValueChanged = { value ->
                noteActions.onSecureNoteContentChanged(value)
            },
            feedbackText = data.formData.contentFeedback?.getText(),
            isError = data.formData.contentFeedback?.error ?: false
        )
    }
}

@Composable
fun SecureNoteContentFeedback.getText(): String =
    when (this) {
        is SecureNoteContentFeedback.UnderLimitFeedback -> stringResource(
            id = R.string.vault_secure_note_character_count,
            length.formatDecimal(),
            limit.formatDecimal()
        )
        is SecureNoteContentFeedback.AtLimitFeedback -> stringResource(
            id = R.string.vault_secure_note_character_count_limit,
            limit.formatDecimal(),
            limit.formatDecimal()
        )
        is SecureNoteContentFeedback.AboveLimitFeedback -> stringResource(
            id = R.string.vault_secure_note_character_count_above_limit,
            length.formatDecimal(),
            limit.formatDecimal()
        )
}

private fun Int.formatDecimal(): String = DecimalFormat.getInstance().format(this)
