package com.dashlane.collections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        mainActionClick = onConfirm,
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.collections_list_delete_popup_button_confirm)),
        additionalActionClick = onDismiss,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.collections_list_delete_popup_button_cancel)),
        title = stringResource(id = R.string.collections_list_delete_popup_title),
        description = {
            Text(text = stringResource(id = R.string.collections_list_delete_popup_text))
        },
        isDestructive = true
    )
}

@Preview
@Composable
private fun DeleteConfirmationDialogPreview() {
    DashlanePreview {
        DeleteConfirmationDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}