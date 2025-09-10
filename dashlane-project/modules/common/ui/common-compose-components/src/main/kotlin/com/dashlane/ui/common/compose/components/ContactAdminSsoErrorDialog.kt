package com.dashlane.ui.common.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.ui.commoncomposecomponents.R

@Composable
fun ContactAdminSsoErrorDialog(onCancel: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.general_error),
        description = { Text(text = stringResource(id = R.string.sso_contact_administrator_message)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onCancel,
        onDismissRequest = onCancel,
    )
}