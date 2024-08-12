package com.dashlane.item.delete

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text

@Composable
fun DeleteVaultItemScreen(
    state: DeleteVaultItemViewModel.UiState,
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit
) {
    when (state.mode) {
        DeleteVaultItemViewModel.WarningMode.NORMAL -> {
            WarningConfirmDialog(
                onConfirmed = onConfirmed,
                onCancelled = onCancelled,
                isDeleting = state.deleting
            )
        }
        DeleteVaultItemViewModel.WarningMode.SHARING -> {
            WarningSharingDialog(
                onConfirmed = onConfirmed,
                onCancelled = onCancelled,
                isDeleting = state.deleting
            )
        }
        else -> Unit
    }
}

@Composable
private fun WarningConfirmDialog(
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit,
    isDeleting: Boolean
) {
    DeleteDialog(
        title = R.string.delete_item,
        description = R.string.please_confirm_you_would_like_to_delete_the_item,
        primaryCta = R.string.delete,
        secondaryCta = R.string.cancel,
        onConfirmed = onConfirmed,
        onCancelled = onCancelled,
        isDeleting = isDeleting
    )
}

@Composable
private fun WarningSharingDialog(
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit,
    isDeleting: Boolean
) {
    DeleteDialog(
        title = R.string.sharing_confirmation_popup_title_delete_from_service,
        description = R.string.sharing_confirmation_popup_description_delete_from_service,
        primaryCta = R.string.sharing_confirmation_popup_btn_confirm_delete_from_service,
        secondaryCta = R.string.sharing_confirmation_popup_btn_cancel_delete_from_service,
        onConfirmed = onConfirmed,
        onCancelled = onCancelled,
        isDeleting = isDeleting
    )
}

@Composable
private fun DeleteDialog(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes primaryCta: Int,
    @StringRes secondaryCta: Int,
    isDeleting: Boolean,
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit
) {
    if (isDeleting) {
        Dialog(
            title = stringResource(id = title),
            description = {
                Text(text = stringResource(id = description))
            },
            mainActionLayout = ButtonLayout.IndeterminateProgress,
            mainActionClick = { },
            onDismissRequest = { },
        )
    } else {
        Dialog(
            title = stringResource(id = title),
            description = {
                Text(text = stringResource(id = description))
            },
            mainActionLayout = ButtonLayout.TextOnly(stringResource(id = primaryCta)),
            mainActionClick = onConfirmed,
            additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = secondaryCta)),
            additionalActionClick = onCancelled,
            onDismissRequest = onCancelled,
            isDestructive = false,
        )
    }
}
