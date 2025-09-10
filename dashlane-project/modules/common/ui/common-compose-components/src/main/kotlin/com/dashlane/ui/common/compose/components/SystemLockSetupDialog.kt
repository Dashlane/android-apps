package com.dashlane.ui.common.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R

@Composable
fun SystemLockSetupDialog(
    isCancellable: Boolean,
    onConfirm: () -> Unit,
    onCancel: (() -> Unit),
) {
    if (isCancellable) {
        Dialog(
            title = stringResource(id = R.string.settings_use_pincode_need_screen_lock_title),
            description = { Text(text = stringResource(id = R.string.settings_use_pincode_need_screen_lock_description)) },
            mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.settings_use_pincode_need_screen_lock_action_settings)),
            mainActionClick = onConfirm,
            additionalActionLayout = ButtonLayout.TextOnly(
                stringResource(id = R.string.settings_use_pincode_need_screen_lock_action_dontuse),
            ),
            additionalActionClick = onCancel,
            onDismissRequest = onCancel,
            isDestructive = false,
        )
    } else {
        Dialog(
            title = stringResource(id = R.string.settings_use_pincode_need_screen_lock_title),
            description = { Text(text = stringResource(id = R.string.settings_use_pincode_need_screen_lock_description)) },
            mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.settings_use_pincode_need_screen_lock_action_settings)),
            mainActionClick = onConfirm,
            onDismissRequest = { },
            isDestructive = false,
        )
    }
}

@Preview
@Composable
private fun SystemLockSetupDialogPreview() {
    DashlanePreview {
        SystemLockSetupDialog(
            isCancellable = false,
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
private fun SystemLockSetupDialogCancellablePreview() {
    DashlanePreview {
        SystemLockSetupDialog(
            isCancellable = true,
            onConfirm = {},
            onCancel = {},
        )
    }
}