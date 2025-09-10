package com.dashlane.item.v3.display

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiNetworkSuggestion
import android.provider.Settings
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.LifecycleOwner
import com.dashlane.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.InfoBoxData.Button.Action.ENABLE_LOCATION_PERMISSION
import com.dashlane.item.v3.data.InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE
import com.dashlane.item.v3.data.InfoBoxData.Button.Action.OPEN_LOCATION_SETTINGS
import com.dashlane.item.v3.data.InfoBoxData.Button.Action.OPEN_NETWORK_SETTINGS
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.forms.credentialForm
import com.dashlane.item.v3.display.forms.secretForm
import com.dashlane.item.v3.display.forms.secureNoteForm
import com.dashlane.item.v3.display.sections.ItemDateSection
import com.dashlane.item.v3.itemtype.creditcard.CreditCardDialog
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardItemEditViewModel
import com.dashlane.item.v3.itemtype.creditcard.ui.creditCardForm
import com.dashlane.item.v3.itemtype.wifi.WiFiDialog
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiItemEditAction
import com.dashlane.item.v3.itemtype.wifi.WiFiItemEditViewModel
import com.dashlane.item.v3.itemtype.wifi.ui.wiFiForm
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditDialog
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.item.v3.viewmodels.SecretItemEditViewModel
import com.dashlane.item.v3.viewmodels.SecureNoteItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.ui.action.Action
import com.dashlane.vault.summary.SummaryObject
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Suppress(
    "LongMethod",
    
    "kotlin:S107",
)
@Composable
internal fun ItemEditScreen(
    activity: AppCompatActivity,
    viewModel: ItemEditViewModel<out FormData>,
    uiState: ItemEditState<out FormData>,
    lazyListState: LazyListState,
    navigator: Navigator,
    viewLifecycleOwner: LifecycleOwner,
    clock: Clock,
    resultLauncherAuthenticator: ActivityResultLauncher<HasLogins.CredentialItem>,
    resultLauncherAttachments: ActivityResultLauncher<SummaryObject>,
    resultLauncherConnectToWiFiNetwork: ActivityResultLauncher<WifiNetworkSuggestion>,
    actionsMenu: MutableMap<Action, MenuItem>,
) {
    val context = LocalContext.current
    val data = uiState.datas?.current
    val snackbarState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    val menuHost: MenuHost = activity
    val menuProvider = getMenuProvider(
        activity,
        viewModel,
        actionsMenu,
        uiState.menuActions,
    )
    val creditCardSecurityCodeFocusRequested = remember { FocusRequester() }
    val creditCardNumberFocusRequested = remember { FocusRequester() }

    LaunchedEffect(key1 = menuProvider) {
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
    }
    BackHandler {
        viewModel.onBackPressed()
    }

    val locationRequestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        val wifiViewModel =
            viewModel as? WiFiItemEditViewModel ?: return@rememberLauncherForActivityResult

        if (isGranted) {
            wifiViewModel.handleAction(WiFiItemEditAction.RefreshWifiInfoState)
        } else {
            wifiViewModel.handleAction(WiFiItemEditAction.ShowLocationPermissionDialog)
        }
    }

    LazyColumn(
        modifier = Modifier
            .background(DashlaneTheme.colors.backgroundAlternate)
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 40.dp),
        state = lazyListState,
    ) {
        if (!uiState.isEditMode) {
            data?.let {
                item {
                    ItemHeader(data = data)
                }
            }
        }
        displayInfoBox(
            infoBoxes = uiState.infoBoxes,
            onInfoBoxActionClicked = { infoBoxAction ->
                when (infoBoxAction) {
                    LAUNCH_GUIDED_CHANGE -> (viewModel as? CredentialItemEditViewModel)?.actionOpenGuidedPasswordChange()
                    ENABLE_LOCATION_PERMISSION -> {
                        locationRequestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    OPEN_NETWORK_SETTINGS -> {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    OPEN_LOCATION_SETTINGS -> {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            },
        )
        data?.let {
            displayFormData(
                data = data,
                viewModel = viewModel,
                uiState = uiState,
                creditCardSecurityCodeFocusRequested = creditCardSecurityCodeFocusRequested,
                creditCardNumberFocusRequested = creditCardNumberFocusRequested,
            )
            item {
                ItemDateSection(
                    clock = clock,
                    data = data,
                    editMode = uiState.isEditMode,
                )
            }
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiState.sideEffect.collect { sideEffect ->
            performSideEffect(
                context = context,
                sideEffect = sideEffect,
                navigator = navigator,
                resultLauncherAuthenticator = resultLauncherAuthenticator,
                resultLauncherAttachments = resultLauncherAttachments,
                resultLauncherConnectToWiFiNetwork = resultLauncherConnectToWiFiNetwork,
                activity = activity,
                snackbarState = snackbarState,
                snackScope = snackbarScope,
                creditCardSecurityCodeFocusRequested = creditCardSecurityCodeFocusRequested,
                creditCardNumberFocusRequested = creditCardNumberFocusRequested,
            )
        }
    }
    uiState.itemAction?.let { action ->
        data?.let {
            PerformAction(
                data,
                action,
                viewModel,
                navigator,
                activity,
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        DashlaneSnackbarWrapper {
            SnackbarHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                hostState = snackbarState,
            )
        }
    }

    if (uiState.dialog != null) {
        DisplayDialog(
            dialog = uiState.dialog,
            onDismiss = { viewModel.onDialogDismissed() },
        )
    }
}

private fun LazyListScope.displayFormData(
    data: Data<out FormData>,
    viewModel: ItemEditViewModel<out FormData>,
    uiState: ItemEditState<out FormData>,
    creditCardSecurityCodeFocusRequested: FocusRequester,
    creditCardNumberFocusRequested: FocusRequester,
) {
    @Suppress("UNCHECKED_CAST")
    when (data.formData) {
        is CredentialFormData -> credentialForm(
            viewModel = viewModel as CredentialItemEditViewModel,
            uiState = (uiState as ItemEditState<CredentialFormData>).toNonNullableState(),
        )
        is SecureNoteFormData -> secureNoteForm(
            viewModel = viewModel as SecureNoteItemEditViewModel,
            uiState = (uiState as ItemEditState<SecureNoteFormData>).toNonNullableState(),
        )
        is SecretFormData -> secretForm(
            viewModel = viewModel as SecretItemEditViewModel,
            uiState = (uiState as ItemEditState<SecretFormData>).toNonNullableState(),
        )
        is CreditCardFormData -> creditCardForm(
            uiState = (uiState as ItemEditState<CreditCardFormData>).toNonNullableState(),
            handleAction = (viewModel as CreditCardItemEditViewModel)::handleAction,
            securityCodeFocusRequested = creditCardSecurityCodeFocusRequested,
            numberFocusRequested = creditCardNumberFocusRequested,
        )
        is WiFiFormData -> wiFiForm(
            uiState = (uiState as ItemEditState<WiFiFormData>).toNonNullableState(),
            handleAction = (viewModel as WiFiItemEditViewModel)::handleAction,
        )
    }
}

fun getMenuProvider(
    activity: AppCompatActivity,
    viewModel: ItemEditViewModel<out FormData>,
    currentMenuActions: MutableMap<Action, MenuItem>,
    menuActions: List<MenuAction>,
) = object : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        currentMenuActions.clear()
        menu.clear()
        menuActions.forEach {
            val s = SpannableString(activity.getString(it.text))
            if (it.color != null) {
                s.setSpan(ForegroundColorSpan(activity.getColor(it.color)), 0, s.length, 0)
            }
            menu.add(s).apply {
                setShowAsAction(it.displayFlags)
                isCheckable = it.checkable
                setIcon(it.icon)
                val buttonColor = if (!it.enabled) {
                    activity.getColor(R.color.text_oddity_disabled)
                } else {
                    activity.getColor(R.color.text_neutral_standard)
                }
                icon = icon?.mutate()?.also { ic -> ic.setTint(buttonColor) }
            }.also { item ->
                if (it.enabled) {
                    currentMenuActions[it] = item
                }
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return selectMenuItem(activity, menuItem, viewModel, currentMenuActions)
    }
}

private fun selectMenuItem(
    activity: AppCompatActivity,
    item: MenuItem,
    viewModel: ItemEditViewModel<out FormData>,
    currentMenuActions: MutableMap<Action, MenuItem>,
): Boolean {
    if (item.itemId == android.R.id.home) {
        viewModel.onBackPressed()
        return true
    }
    val menuAction = currentMenuActions.filter { it.value == item }
    menuAction.forEach {
        val action = it.key
        action.onClickAction(activity)
    }
    return menuAction.isNotEmpty()
}

@Composable
private fun DisplayDialog(
    dialog: ItemEditDialog,
    onDismiss: () -> Unit = {},
) {
    val context = LocalContext.current

    when (dialog) {
        is CreditCardDialog.NfcReaderFailed ->
            Dialog(
                onDismissRequest = onDismiss,
                mainActionClick = onDismiss,
                mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.nfc_creditcard_prompt_positive_button)),
                title = stringResource(id = R.string.nfc_creditcard_prompt_error_title),
                description = {
                    Text(text = stringResource(id = R.string.nfc_creditcard_prompt_error_message))
                },
            )

        is CreditCardDialog.NfcReaderSucceed ->
            Dialog(
                onDismissRequest = onDismiss,
                mainActionClick = onDismiss,
                mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.nfc_creditcard_success_positive_button)),
                title = stringResource(id = R.string.nfc_creditcard_success_title),
                description = {
                    Text(text = stringResource(id = R.string.nfc_creditcard_success_message))
                },
            )

        is WiFiDialog.LocationPermissionDenied -> Dialog(
            title = stringResource(id = R.string.wifi_location_permission_denied_dialog_title),
            description = {
                Text(text = stringResource(id = R.string.wifi_location_permission_denied_dialog_message))
            },
            onDismissRequest = onDismiss,
            mainActionClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
                context.startActivity(intent)
                onDismiss()
            },
            mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.wifi_location_permission_denied_dialog_open_settings)),
            additionalActionClick = onDismiss,
            additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        )
    }
}