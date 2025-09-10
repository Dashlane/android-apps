package com.dashlane.item.v3.display

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.provider.Settings.ACTION_WIFI_SETTINGS
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditSideEffect
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog.Companion.DIALOG_PASSWORD_GENERATOR_TAG
import com.dashlane.util.DeviceUtils
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressWarnings("LongMethod")
@Composable
fun PerformAction(
    data: Data<out FormData>,
    itemAction: ItemAction,
    viewModel: ItemEditViewModel<out FormData>,
    navigator: Navigator,
    activity: AppCompatActivity,
) {
    when (itemAction) {
        ItemAction.ConfirmSaveChanges -> {
            SaveDialog(
                title = stringResource(id = R.string.save_item_),
                description = stringResource(id = R.string.would_you_like_to_save_the_item_),
                secondaryAction = stringResource(id = R.string.discart),
                onActionPerformed = { viewModel.actionHandled() },
                onSave = { viewModel.onSave() },
                onSecondary = { viewModel.onCloseClicked(true) },
            )
        }
        ItemAction.SaveError -> {
            ErrorSaveDialog(
                onActionPerformed = { viewModel.actionHandled() },
            )
        }
        ItemAction.OpenNoRights -> {
            LimitedRightDialog {
                viewModel.actionHandled()
            }
        }
        ItemAction.ConfirmRemove2FA -> {
            ConfirmRemove2FADialog(
                data.commonData.name ?: "",
                onPositive = {
                    (viewModel as CredentialItemEditViewModel).actionRemoveTwoFactorConfirmed()
                    viewModel.actionHandled()
                },
                onNegative = { viewModel.actionHandled() },
            )
        }
        ItemAction.Close -> {
            DeviceUtils.hideKeyboard(activity)
            navigator.popBackStack()
        }
        is ItemAction.SavePendingChangesToEditCollection -> {
            SaveDialog(
                title = stringResource(id = R.string.save_item_),
                description = stringResource(id = R.string.save_item_to_proceed_description),
                secondaryAction = stringResource(id = R.string.cancel),
                onActionPerformed = { viewModel.actionHandled() },
                onSave = {
                    viewModel.onSaveToEditCollection()
                },
                onSecondary = { },
            )
        }
    }
}

@Suppress(
    "LongMethod",
    
    "kotlin:S107",
)
fun performSideEffect(
    context: Context,
    sideEffect: ItemEditSideEffect,
    navigator: Navigator,
    resultLauncherAuthenticator: ActivityResultLauncher<CredentialItem>,
    resultLauncherAttachments: ActivityResultLauncher<SummaryObject>,
    resultLauncherConnectToWiFiNetwork: ActivityResultLauncher<WifiNetworkSuggestion>,
    activity: AppCompatActivity,
    snackbarState: SnackbarHostState,
    snackScope: CoroutineScope,
    creditCardSecurityCodeFocusRequested: FocusRequester,
    creditCardNumberFocusRequested: FocusRequester,
) {
    when (sideEffect) {
        is ItemEditSideEffect.ConfirmDelete -> {
            navigator.goToDeleteVaultItem(sideEffect.id, sideEffect.isShared)
        }
        is ItemEditSideEffect.GoToSetup2FA -> {
            resultLauncherAuthenticator.launch(
                CredentialItem(
                    id = sideEffect.credentialId,
                    title = sideEffect.credentialName,
                    domain = sideEffect.topDomain,
                    username = null,
                    packageName = sideEffect.packageName,
                    professional = sideEffect.proSpace,
                ),
            )
        }
        is ItemEditSideEffect.OpenWebsite -> {
            LoginOpener(activity).show(sideEffect.url, sideEffect.packageNames, sideEffect.listener)
        }
        is ItemEditSideEffect.OpenShared -> {
            navigator.goToShareUsersForItems(sideEffect.id)
        }
        is ItemEditSideEffect.OpenPasswordHistory -> {
            navigator.goToItemHistoryFromItemEdit(sideEffect.id)
        }
        is ItemEditSideEffect.OpenLinkedServices -> {
            navigator.goToLinkedWebsites(
                itemId = sideEffect.id,
                fromViewOnly = sideEffect.fromViewOnly,
                addNew = sideEffect.addNew,
                temporaryWebsites = sideEffect.temporaryWebsites,
                temporaryApps = sideEffect.temporaryApps,
                urlDomain = sideEffect.url,
            )
        }
        is ItemEditSideEffect.OpenCollection -> {
            navigator.goToCollectionSelectorFromItemEdit(itemId = sideEffect.itemId)
        }
        is ItemEditSideEffect.OpenPasswordGenerator -> {
            DeviceUtils.hideKeyboard(activity)
            if (activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR_TAG) != null) return

            
            activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR_TAG) as? PasswordGeneratorDialog
                ?: PasswordGeneratorDialog.newInstance(
                    activity,
                    sideEffect.origin,
                    sideEffect.domainAsking,
                )
                    .show(activity.supportFragmentManager, DIALOG_PASSWORD_GENERATOR_TAG)
        }
        is ItemEditSideEffect.GuidedPasswordChange -> {
            navigator.goToGuidedPasswordChangeFromCredential(
                sideEffect.id,
                sideEffect.website,
                sideEffect.userName,
            )
        }
        is ItemEditSideEffect.PasswordRestoreResult -> {
            if (sideEffect.success) {
                displaySnackbarMessage(
                    snackbarState,
                    snackScope,
                    context.getString(R.string.feedback_password_restored),
                )
            } else {
                displaySnackbarMessage(
                    snackbarState,
                    snackScope,
                    context.getString(R.string.generic_error_message),
                )
            }
        }
        ItemEditSideEffect.Saved -> {
            displaySnackbarMessage(
                snackbarState,
                snackScope,
                context.getString(R.string.vault_saved),
            )
            DeviceUtils.hideKeyboard(activity)
        }
        ItemEditSideEffect.Close -> {
            DeviceUtils.hideKeyboard(activity)
            navigator.popBackStack()
        }
        is ItemEditSideEffect.ShowAttachments -> {
            sideEffect.summaryObject?.let {
                resultLauncherAttachments.launch(it)
            }
        }
        ItemEditSideEffect.FrozenPaywall -> {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
        }
        ItemEditSideEffect.CreditCardSecurityCodeRequestFocus -> {
            creditCardSecurityCodeFocusRequested.requestFocus()
        }
        ItemEditSideEffect.CreditCardNumberRequestFocus -> {
            creditCardNumberFocusRequested.requestFocus()
        }
        is ItemEditSideEffect.ConnectToWiFiNetwork -> {
            resultLauncherConnectToWiFiNetwork.launch(
                sideEffect.network,
            )
        }
        is ItemEditSideEffect.ConnectToWiFiNetworkResult -> {
            when (sideEffect) {
                ItemEditSideEffect.ConnectToWiFiNetworkResult.Added ->
                    displaySnackbarMessage(
                        snackbarState = snackbarState,
                        snackScope = snackScope,
                        message = context.getString(R.string.wifi_connect_to_network_result_success_added),
                    )

                ItemEditSideEffect.ConnectToWiFiNetworkResult.Connected ->
                    displaySnackbarMessage(
                        snackbarState = snackbarState,
                        snackScope = snackScope,
                        message = context.getString(R.string.wifi_connect_to_network_result_success_connected),
                    )

                ItemEditSideEffect.ConnectToWiFiNetworkResult.AlreadyExists ->
                    displaySnackbarMessage(
                        snackbarState = snackbarState,
                        snackScope = snackScope,
                        message = context.getString(R.string.wifi_connect_to_network_result_already_exist),
                        actionLabel = context.getString(R.string.wifi_connect_to_network_result_already_exist_action),
                        actionPerformed = {
                            context.startActivity(Intent(ACTION_WIFI_SETTINGS))
                        },
                    )

                ItemEditSideEffect.ConnectToWiFiNetworkResult.AlreadyConnected ->
                    displaySnackbarMessage(
                        snackbarState = snackbarState,
                        snackScope = snackScope,
                        message = context.getString(R.string.wifi_connect_to_network_result_already_connected),
                    )

                ItemEditSideEffect.ConnectToWiFiNetworkResult.Failed ->
                    displaySnackbarMessage(
                        snackbarState = snackbarState,
                        snackScope = snackScope,
                        message = context.getString(R.string.wifi_connect_to_network_result_failed),
                    )
            }
        }
    }
}

private fun displaySnackbarMessage(
    snackbarState: SnackbarHostState,
    snackScope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    actionPerformed: () -> Unit = {},
    actionDismissed: () -> Unit = {},
) {
    snackScope.launch {
        val result = snackbarState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
        )

        if (result == SnackbarResult.ActionPerformed) {
            actionPerformed()
        } else if (result == SnackbarResult.Dismissed) {
            actionDismissed()
        }
    }
}

@Composable
private fun SaveDialog(
    title: String,
    description: String,
    secondaryAction: String,
    onActionPerformed: () -> Unit,
    onSave: () -> Unit,
    onSecondary: () -> Unit,
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
            onSecondary()
        },
        additionalActionLayout = ButtonLayout.TextOnly(secondaryAction),
        title = title,
        description = {
            Text(text = description)
        },
    )
}

@Composable
private fun ErrorSaveDialog(
    onActionPerformed: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onActionPerformed()
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = {
            onActionPerformed()
        },
        title = stringResource(id = R.string.error),
        description = {
            Text(text = stringResource(id = R.string.error_cannot_add_empty_item))
        },
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
        },
    )
}

@Composable
private fun ConfirmRemove2FADialog(name: String, onPositive: () -> Unit, onNegative: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.authenticator_item_edit_remove_popup_title),
        description = {
            Text(
                text = stringResource(
                    id = R.string.authenticator_item_edit_remove_popup_body,
                    name,
                ),
            )
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