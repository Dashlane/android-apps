package com.dashlane.ui.common.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.ui.commoncomposecomponents.R

@Composable
fun ReminderMasterPasswordDialog(
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.change_master_password_pop_success_title),
        description = { Text(text = stringResource(id = R.string.change_master_password_popup_success_message)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onDismiss,
        onDismissRequest = onDismiss,
        isDestructive = false,
    )
}