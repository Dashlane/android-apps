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
fun RevokeToDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        mainActionClick = onConfirm,
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.collections_revoke_to_delete_dialog_positive_button)),
        additionalActionClick = onDismiss,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.collections_revoke_to_delete_dialog_negative_button)),
        title = stringResource(id = R.string.collections_revoke_to_delete_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.collections_revoke_to_delete_dialog_text))
        }
    )
}

@Preview
@Composable
private fun RevokeToDeleteDialogPreview() {
    DashlanePreview {
        RevokeToDeleteDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}