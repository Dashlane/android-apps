package com.dashlane.item.v3.display

import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog.Companion.DIALOG_PASSWORD_GENERATOR_TAG
import com.dashlane.util.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressWarnings("LongMethod")
@Composable
fun PerformAction(
    data: FormData,
    itemAction: ItemAction,
    viewModel: ItemEditViewModel,
    navigator: Navigator,
    resultLauncherAuthenticator: ActivityResultLauncher<AuthenticatorSuggestionsUiState.HasLogins.CredentialItem>,
    activity: AppCompatActivity,
    snackbarState: SnackbarHostState,
    snackScope: CoroutineScope
) {
    when (itemAction) {
        ItemAction.ConfirmSaveChanges -> {
            SaveConfirmationDialog(
                onActionPerformed = { viewModel.actionHandled() },
                onSave = { viewModel.saveData() },
                onDiscard = { viewModel.onCloseClicked(true) }
            )
        }
        ItemAction.ConfirmDelete -> {
            navigator.goToDeleteVaultItem(data.id, data.isShared)
            viewModel.actionHandled()
        }
        is ItemAction.GoToSetup2FA -> {
            resultLauncherAuthenticator.launch(
                AuthenticatorSuggestionsUiState.HasLogins.CredentialItem(
                    id = itemAction.credentialId,
                    title = itemAction.credentialName,
                    domain = itemAction.topDomain,
                    username = null,
                    packageName = itemAction.packageName,
                    professional = itemAction.proSpace,
                )
            )
            viewModel.actionHandled()
        }
        is ItemAction.OpenWebsite -> {
            LoginOpener(activity).show(itemAction.url, itemAction.packageNames, itemAction.listener)
            viewModel.actionHandled()
        }
        ItemAction.OpenNoRights -> {
            LimitedRightDialog {
                viewModel.actionHandled()
            }
        }
        ItemAction.OpenShared -> {
            navigator.goToShareUsersForItems(data.id)
            viewModel.actionHandled()
        }
        ItemAction.OpenPasswordHistory -> {
            navigator.goToItemHistory(data.id)
            viewModel.actionHandled()
        }
        ItemAction.ConfirmRemove2FA -> {
            ConfirmRemove2FADialog(
                data.name,
                onPositive = {
                    (viewModel as CredentialItemEditViewModel).actionRemoveTwoFactorConfirmed()
                    viewModel.actionHandled()
                },
                onNegative = { viewModel.actionHandled() }
            )
        }
        is ItemAction.OpenLinkedServices -> {
            navigator.goToLinkedWebsites(
                itemId = data.id,
                fromViewOnly = itemAction.fromViewOnly,
                addNew = itemAction.addNew,
                temporaryWebsites = itemAction.temporaryWebsites,
                temporaryApps = itemAction.temporaryApps,
                urlDomain = itemAction.url
            )
            viewModel.actionHandled()
        }
        is ItemAction.OpenCollection -> {
            navigator.goToCollectionSelectorFromItemEdit(
                fromViewOnly = false,
                temporaryPrivateCollectionsName = itemAction.temporaryPrivateCollectionsName,
                temporarySharedCollectionsId = itemAction.temporarySharedCollectionsId,
                spaceId = itemAction.spaceId,
                isLimited = itemAction.isLimited
            )
            viewModel.actionHandled()
        }
        is ItemAction.OpenPasswordGenerator -> {
            if (data !is CredentialFormData) {
                viewModel.actionHandled()
                return
            }
            DeviceUtils.hideKeyboard(activity)
            if (activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR_TAG) != null) return

            
            activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR_TAG) as? PasswordGeneratorDialog
                ?: PasswordGeneratorDialog.newInstance(activity, itemAction.origin, data.url ?: "")
                    .show(activity.supportFragmentManager, DIALOG_PASSWORD_GENERATOR_TAG)
            viewModel.actionHandled()
        }
        is ItemAction.GuidedPasswordChange -> {
            navigator.goToGuidedPasswordChangeFromCredential(
                data.id,
                itemAction.website,
                itemAction.userName
            )
            viewModel.actionHandled()
        }
        is ItemAction.PasswordRestoreResult -> {
            if (itemAction.success) {
                DisplaySnackbarMessage(
                    snackbarState,
                    snackScope,
                    stringResource(id = R.string.feedback_password_restored)
                )
            } else {
                DisplaySnackbarMessage(snackbarState, snackScope, stringResource(id = R.string.generic_error_message))
            }
            viewModel.actionHandled()
        }
        ItemAction.Saved -> {
            DisplaySnackbarMessage(snackbarState, snackScope, stringResource(id = R.string.vault_saved))
            DeviceUtils.hideKeyboard(activity)
            viewModel.actionHandled()
        }
        ItemAction.Close -> {
            DeviceUtils.hideKeyboard(activity)
            navigator.popBackStack()
        }
    }
}

@Composable
private fun DisplaySnackbarMessage(
    snackbarState: SnackbarHostState,
    snackScope: CoroutineScope,
    message: String
) {
    LaunchedEffect(Unit) {
        snackScope.launch {
            snackbarState.showSnackbar(message)
        }
    }
}

@Composable
private fun SaveConfirmationDialog(
    onActionPerformed: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            onActionPerformed()
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.dialog_save_item_save_button)),
        mainActionClick = {
            onActionPerformed()
            onSave()
        },
        additionalActionClick = {
            onActionPerformed()
            onDiscard()
        },
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.discart)),
        title = stringResource(id = R.string.save_item_),
        description = {
            Text(text = stringResource(id = R.string.would_you_like_to_save_the_item_))
        }
    )
}

@Composable
private fun LimitedRightDialog(onDone: () -> Unit) {
    Dialog(
        onDismissRequest = {
            onDone()
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = {
            onDone()
        },
        title = stringResource(id = R.string.reveal_password_permission_title),
        description = {
            Text(text = stringResource(id = R.string.reveal_password_permission_body))
        }
    )
}

@Composable
private fun ConfirmRemove2FADialog(name: String, onPositive: () -> Unit, onNegative: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.authenticator_item_edit_remove_popup_title),
        description = {
            Text(text = stringResource(id = R.string.authenticator_item_edit_remove_popup_body, name))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.authenticator_item_edit_remove_popup_positive_button)),
        mainActionClick = {
            onPositive()
        },
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.authenticator_item_edit_remove_popup_negative_button)),
        additionalActionClick = {
            onNegative()
        },
        onDismissRequest = {
            onNegative()
        },
    )
}