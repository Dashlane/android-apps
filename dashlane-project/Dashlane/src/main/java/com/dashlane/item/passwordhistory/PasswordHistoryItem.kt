package com.dashlane.item.passwordhistory

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.PasswordFieldFeedback
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun PasswordHistoryItem(
    modifier: Modifier = Modifier,
    password: String,
    dateString: String,
    onRevertClick: () -> Boolean,
    onCopyClick: () -> Boolean,
) {
    var obfuscated by rememberSaveable { mutableStateOf(true) }
    PasswordField(
        modifier = modifier.fillMaxWidth(),
        value = password,
        onValueChange = {},
        label = dateString,
        labelPersists = false,
        obfuscated = obfuscated,
        feedback = PasswordFieldFeedback.Text(stringResource(R.string.password_history_date_feedback, dateString)),
        actions = TextFieldActions.PasswordFreestyle(
            hideRevealAction = FieldAction.HideReveal(
                contentDescriptionToHide = stringResource(id = R.string.and_accessibility_text_edit_hide),
                contentDescriptionToReveal = stringResource(id = R.string.and_accessibility_text_edit_reveal),
                onClick = {
                    obfuscated = !obfuscated
                    true
                },
            ),
            genericAction1 = FieldAction.Generic(
                iconLayout = ButtonLayout.IconOnly(
                    IconTokens.actionCopyOutlined,
                    stringResource(id = R.string.quick_action_copy_password)
                ),
                onClick = onCopyClick,
            ),
            genericAction2 = FieldAction.Generic(
                iconLayout = ButtonLayout.IconOnly(
                    IconTokens.actionUndoOutlined,
                    stringResource(id = R.string.infobox_restore_password_button)
                ),
                onClick = onRevertClick,
            ),
        ),
        readOnly = true,
    )
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordHistoryItemPreview() {
    DashlanePreview {
        PasswordHistoryItem(
            password = "Azerty12",
            dateString = "3 days ago",
            onRevertClick = { true },
            onCopyClick = { true }
        )
    }
}