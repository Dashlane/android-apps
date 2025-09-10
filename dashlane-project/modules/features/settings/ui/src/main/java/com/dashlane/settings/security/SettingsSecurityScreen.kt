package com.dashlane.settings.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.settings.ApplicationLock
import com.dashlane.settings.Misc
import com.dashlane.settings.SettingsCategory
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockViewModel
import com.dashlane.settings.security.misc.SettingsMiscState
import com.dashlane.settings.security.misc.SettingsMiscViewModel
import com.dashlane.settings.ui.R

@Composable
@Suppress("LongMethod")
fun SettingsSecurityScreen(
    modifier: Modifier = Modifier,
    applicationLockViewModel: SettingsApplicationLockViewModel,
    miscViewModel: SettingsMiscViewModel,
    applicationLockSideEffect: (SettingsApplicationLockState.SideEffect) -> Unit,
    miscSideEffect: (SettingsMiscState.SideEffect) -> Unit,
) {
    val applicationLockState by applicationLockViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val miscState by miscViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = applicationLockViewModel) {
        applicationLockViewModel.stateFlow.sideEffect.collect { applicationLockSideEffect(it) }
    }

    LaunchedEffect(key1 = miscViewModel) {
        miscViewModel.stateFlow.sideEffect.collect { miscSideEffect(it) }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsApplicationLockContent(
            applicationLockSettings = applicationLockState.data.securitySettings,
            sendEvent = { event -> applicationLockViewModel.sendEvent(event) },
        )
        SettingsMiscContent(
            miscSettings = miscState.data.miscSettings,
            sendEvent = { event -> miscViewModel.sendEvent(event) },
        )
    }

    val onConfirm: (SettingsDialog) -> Unit = { dialog -> applicationLockViewModel.sendEvent(SettingsEvent.DialogPositive(dialog)) }
    val onDismiss: (SettingsDialog) -> Unit = { dialog -> applicationLockViewModel.sendEvent(SettingsEvent.DialogNegative(dialog)) }

    applicationLockState.data.dialog
        ?.let { SettingsDialog(dialog = it, onConfirm = onConfirm, onDismiss = onDismiss) }
}

@Composable
fun SettingsApplicationLockContent(
    applicationLockSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<ApplicationLock>) -> Unit,
) {
    if (applicationLockSettings.isNotEmpty()) {
        SettingsCategory(
            title = stringResource(R.string.settings_app_lock),
            settings = applicationLockSettings,
            sendEvent = { event -> sendEvent(event) },
        )
    }
}

@Composable
fun SettingsMiscContent(
    miscSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Misc>) -> Unit,
) {
    if (miscSettings.isNotEmpty()) {
        SettingsCategory(
            title = stringResource(R.string.settings_category_sharing),
            settings = miscSettings,
            sendEvent = { event -> sendEvent(event) },
        )
    }
}

@Preview
@Composable
@Suppress("LongMethod")
private fun SettingsSecurityContentPreview() {
    DashlanePreview {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsApplicationLockContent(
                applicationLockSettings = listOf(
                    SettingsItemData.Toggle(
                        id = ApplicationLock.PinLock,
                        title = stringResource(R.string.settings_use_pincode),
                        description = stringResource(R.string.setting_use_pincode_description),
                        enabled = true,
                        checked = true,
                    ),
                    SettingsItemData.Click(
                        id = ApplicationLock.ChangePin,
                        title = stringResource(R.string.settings_change_pincode),
                        description = stringResource(R.string.settings_change_pincode_description),
                        enabled = true,
                    ),
                    SettingsItemData.Toggle(
                        id = ApplicationLock.BiometricUnlock,
                        title = stringResource(R.string.setting_use_google_fingerprint_title),
                        description = stringResource(R.string.setting_use_google_fingerprint_description),
                        enabled = true,
                        checked = true,
                    ),
                    SettingsItemData.Toggle(
                        id = ApplicationLock.TwoFA,
                        title = stringResource(R.string.settings_use_2fa),
                        description = stringResource(R.string.settings_use_2fa_description),
                        enabled = false,
                        checked = false,
                    ),
                    SettingsItemData.Toggle(
                        id = ApplicationLock.BiometricRecovery,
                        title = stringResource(R.string.account_recovery_setting_title),
                        description = stringResource(R.string.account_recovery_setting_description),
                        enabled = true,
                        checked = false,
                    ),
                    SettingsItemData.Click(
                        id = ApplicationLock.AccountRecoveryKey,
                        title = stringResource(R.string.account_recovery_key_setting_title),
                        description = stringResource(R.string.account_recovery_key_setting_description_on),
                        enabled = true,
                    ),
                    SettingsItemData.Toggle(
                        id = ApplicationLock.DisableLocal2FA,
                        title = stringResource(R.string.settings_disable_totp2_for_device),
                        description = stringResource(R.string.settings_disable_totp2_for_device_description),
                        enabled = true,
                        checked = true,
                    ),
                    SettingsItemData.Toggle(
                        id = ApplicationLock.AutoLockOnExit,
                        title = stringResource(R.string.settings_lock_on_exit),
                        description = stringResource(R.string.setting_lock_on_exit_description),
                        enabled = true,
                        checked = true,
                    ),
                    SettingsItemData.Click(
                        id = ApplicationLock.AutoLockTime,
                        title = stringResource(R.string.settings_use_lock_timeout),
                        description = stringResource(R.string.settings_use_lock_timeout_description_never_lock),
                        enabled = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingsMiscContent(
                miscSettings = listOf(
                    SettingsItemData.Toggle(
                        id = Misc.Screenshots,
                        title = stringResource(R.string.settings_security_flag_title),
                        description = stringResource(R.string.settings_security_flag_description),
                        enabled = false,
                        checked = true,
                    ),
                    SettingsItemData.Toggle(
                        id = Misc.ClearClipboard,
                        title = stringResource(R.string.settings_clear_clipboard),
                        description = stringResource(R.string.settings_clear_clipboard_description),
                        enabled = false,
                        checked = true,
                    ),
                    SettingsItemData.Toggle(
                        id = Misc.Copy2FA,
                        title = stringResource(R.string.setting_automatically_copy_2fa),
                        description = stringResource(R.string.setting_automatically_copy_2fa_description),
                        enabled = false,
                        checked = true,
                    ),
                    SettingsItemData.Click(
                        id = Misc.ChangeMasterPassword,
                        title = stringResource(R.string.setting_change_master_password_title),
                        description = stringResource(R.string.setting_change_master_password_description),
                        enabled = false,
                    ),
                    SettingsItemData.Click(
                        id = Misc.KeyDerivation,
                        title = stringResource(R.string.settings_cryptography),
                        description = stringResource(R.string.settings_cryptography_description_argon2),
                        enabled = false,
                    ),
                    SettingsItemData.Click(
                        id = Misc.PrivacyAndData,
                        title = stringResource(R.string.setting_privacy_title),
                        description = stringResource(R.string.setting_privacy_message),
                        enabled = false,
                    ),
                    SettingsItemData.Click(
                        id = Misc.ManageDevices,
                        title = stringResource(R.string.setting_manage_devices_title),
                        description = stringResource(R.string.setting_manage_devices_description),
                        enabled = false,
                    ),
                    SettingsItemData.Toggle(
                        id = Misc.RichIcons,
                        title = stringResource(R.string.setting_show_rich_icons_title),
                        description = stringResource(R.string.setting_show_rich_icons_description),
                        enabled = false,
                        checked = true,
                    ),
                ),
                sendEvent = {},
            )
        }
    }
}