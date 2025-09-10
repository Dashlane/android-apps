package com.dashlane.item.passwordhistory

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun CredentialPasswordHistoryItem(
    modifier: Modifier = Modifier,
    password: String,
    dateString: String,
    onRevertClick: () -> Boolean,
    onCopyClick: () -> Boolean,
) {
    PasswordHistoryField(
        modifier = modifier,
        password = password,
        dateString = dateString,
        feedback = stringResource(R.string.password_history_date_feedback, dateString),
        onCopyClick = onCopyClick,
        additionalAction = FieldAction.Generic(
            iconLayout = ButtonLayout.IconOnly(
                IconTokens.actionUndoOutlined,
                stringResource(id = R.string.infobox_restore_password_button),
            ),
            onClick = onRevertClick,
        ),
    )
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordHistoryItemPreview() {
    DashlanePreview {
        CredentialPasswordHistoryItem(
            password = "Azerty12",
            dateString = "3 days ago",
            onRevertClick = { true },
            onCopyClick = { true },
        )
    }
}