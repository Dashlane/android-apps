package com.dashlane.settings.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.settings.Autofill
import com.dashlane.settings.Backup
import com.dashlane.settings.Notifications
import com.dashlane.settings.SettingsCategory
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.UsageData
import com.dashlane.settings.general.autofill.SettingsAutofillState
import com.dashlane.settings.general.autofill.SettingsAutofillViewModel
import com.dashlane.settings.general.backup.SettingsBackupState
import com.dashlane.settings.general.backup.SettingsBackupViewModel
import com.dashlane.settings.general.notifications.SettingsNotificationsState
import com.dashlane.settings.general.notifications.SettingsNotificationsViewModel
import com.dashlane.settings.general.usagedata.SettingsUsageDataViewModel
import com.dashlane.settings.ui.R

@Composable
@Suppress("kotlin:S3776", "kotlin:S107", "LongMethod")
fun SettingsGeneralScreen(
    modifier: Modifier = Modifier,
    autofillViewModel: SettingsAutofillViewModel,
    notificationsViewModel: SettingsNotificationsViewModel,
    backupViewModel: SettingsBackupViewModel,
    usageDataViewModel: SettingsUsageDataViewModel,
    autofillSideEffect: (SettingsAutofillState.SideEffect) -> Unit,
    notificationsSideEffect: (SettingsNotificationsState.SideEffect) -> Unit,
    backupSideEffect: (SettingsBackupState.SideEffect) -> Unit,
) {
    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> autofillViewModel.sendEvent(SettingsEvent.ViewResumed)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val autofillState by autofillViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val notificationsState by notificationsViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val backupState by backupViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val usageDataState by usageDataViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = autofillViewModel) {
        autofillViewModel.stateFlow.sideEffect.collect { autofillSideEffect(it) }
    }
    LaunchedEffect(key1 = notificationsViewModel) {
        notificationsViewModel.stateFlow.sideEffect.collect { notificationsSideEffect(it) }
    }
    LaunchedEffect(key1 = backupViewModel) {
        backupViewModel.stateFlow.sideEffect.collect { backupSideEffect(it) }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsAutofillContent(
            autofillSettings = autofillState.data.autofillSettings,
            sendEvent = { event -> autofillViewModel.sendEvent(event) },
        )
        SettingsNotificationsContent(
            notificationsSettings = notificationsState.data.notificationsSettings,
            sendEvent = { event -> notificationsViewModel.sendEvent(event) },
        )
        SettingsBackupContent(
            backupSettings = backupState.data.backupSettings,
            sendEvent = { event -> backupViewModel.sendEvent(event) },
        )
        SettingsUsageDataContent(
            usageDataSettings = usageDataState.data.usageDataSettings,
            sendEvent = { event -> usageDataViewModel.sendEvent(event) },
        )
    }
}

@Composable
fun SettingsAutofillContent(
    autofillSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Autofill>) -> Unit,
) {
    if (autofillSettings.isNotEmpty()) {
        SettingsCategory<Autofill>(
            title = stringResource(R.string.setting_in_app_login_category),
            settings = autofillSettings,
            sendEvent = { event -> sendEvent(event) },
        )
    }
}

@Composable
fun SettingsNotificationsContent(
    notificationsSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Notifications>) -> Unit,
) {
    if (notificationsSettings.isNotEmpty()) {
        SettingsCategory<Notifications>(
            title = stringResource(R.string.notifications_settings_header),
            settings = notificationsSettings,
            sendEvent = { event -> sendEvent(event) },
        )
    }
}

@Composable
fun SettingsBackupContent(
    backupSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Backup>) -> Unit,
) {
    if (backupSettings.isNotEmpty()) {
        SettingsCategory<Backup>(
            title = stringResource(R.string.setting_backup_category),
            settings = backupSettings,
            sendEvent = { event -> sendEvent(event) },
        )
    }
}

@Composable
fun SettingsUsageDataContent(
    usageDataSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<UsageData>) -> Unit,
) {
    if (usageDataSettings.isNotEmpty()) {
        SettingsCategory<UsageData>(
            title = stringResource(R.string.setting_logs_header),
            settings = usageDataSettings,
            sendEvent = { event -> sendEvent(event) },
        )
    }
}

@Preview
@Composable
@Suppress("LongMethod")
fun SettingsGeneralContentPreview() {
    DashlanePreview {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            SettingsAutofillContent(
                autofillSettings = listOf(
                    SettingsItemData.Toggle(
                        id = Autofill.EnableAutofill,
                        title = stringResource(R.string.setting_in_app_login_enabled_title),
                        description = stringResource(R.string.setting_in_app_login_enabled_description),
                        enabled = true,
                        checked = true,
                    ),
                    SettingsItemData.Click(
                        id = Autofill.PausedAutofill,
                        title = stringResource(R.string.autofill_pause_resume_settings_title),
                        description = stringResource(R.string.autofill_pause_resume_settings_description),
                        enabled = true,
                        visible = false,
                    ),
                    SettingsItemData.Toggle(
                        id = Autofill.KeyboardAutofill,
                        title = stringResource(R.string.inline_autofill_settings_title_2),
                        description = stringResource(R.string.inline_autofill_settings_description),
                        enabled = true,
                        checked = true,
                    ),
                    SettingsItemData.Toggle(
                        id = Autofill.PhishingAlert,
                        title = stringResource(R.string.settings_anti_phishing),
                        description = stringResource(R.string.settings_anti_phishing_description),
                        enabled = true,
                        checked = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingsNotificationsContent(
                notificationsSettings = listOf(
                    SettingsItemData.Click(
                        id = Notifications.General,
                        title = stringResource(R.string.general_notifications_settings_title),
                        description = stringResource(R.string.general_notifications_settings_description),
                        enabled = false,
                    ),
                    SettingsItemData.Toggle(
                        id = Notifications.CopyAndPaste,
                        title = stringResource(R.string.follow_up_settings_title),
                        description = stringResource(R.string.follow_up_settings_description),
                        enabled = false,
                        checked = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingsBackupContent(
                backupSettings = listOf(
                    SettingsItemData.Click(
                        id = Backup.Export,
                        title = stringResource(R.string.setting_backup_export),
                        description = stringResource(R.string.setting_backup_export_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Backup.ExportCsv,
                        title = stringResource(R.string.setting_backup_export_csv),
                        description = stringResource(R.string.setting_backup_export_csv_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Backup.Import,
                        title = stringResource(R.string.setting_backup_import),
                        description = stringResource(R.string.setting_backup_import_description),
                        enabled = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingsUsageDataContent(
                usageDataSettings = listOf(
                    SettingsItemData.Toggle(
                        id = UsageData.EnableUsageData,
                        title = stringResource(R.string.setting_allow_logs_title),
                        description = stringResource(R.string.setting_allow_logs_description),
                        enabled = true,
                        checked = true,
                    ),
                ),
                sendEvent = {},
            )
        }
    }
}