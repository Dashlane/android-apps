package com.dashlane.settings.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.BaseSearchField
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.settings.Account
import com.dashlane.settings.Advanced
import com.dashlane.settings.ApplicationLock
import com.dashlane.settings.Autofill
import com.dashlane.settings.Backup
import com.dashlane.settings.Help
import com.dashlane.settings.MainMisc
import com.dashlane.settings.Misc
import com.dashlane.settings.Notifications
import com.dashlane.settings.Settings
import com.dashlane.settings.SettingsCategory
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.DialogNegative
import com.dashlane.settings.SettingsEvent.DialogPositive
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.UsageData
import com.dashlane.settings.general.autofill.SettingsAutofillState
import com.dashlane.settings.general.autofill.SettingsAutofillViewModel
import com.dashlane.settings.general.backup.SettingsBackupState
import com.dashlane.settings.general.backup.SettingsBackupViewModel
import com.dashlane.settings.general.notifications.SettingsNotificationsState
import com.dashlane.settings.general.notifications.SettingsNotificationsViewModel
import com.dashlane.settings.general.usagedata.SettingsUsageDataViewModel
import com.dashlane.settings.help.SettingsHelpContent
import com.dashlane.settings.help.SettingsHelpState
import com.dashlane.settings.help.SettingsHelpViewModel
import com.dashlane.settings.main.SettingsMainMiscContent
import com.dashlane.settings.main.account.SettingsAccountState
import com.dashlane.settings.main.account.SettingsAccountViewModel
import com.dashlane.settings.main.advanced.SettingsAdvancedState
import com.dashlane.settings.main.advanced.SettingsAdvancedViewModel
import com.dashlane.settings.main.misc.SettingsMainMiscState
import com.dashlane.settings.main.misc.SettingsMainMiscViewModel
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockViewModel
import com.dashlane.settings.security.misc.SettingsMiscState
import com.dashlane.settings.security.misc.SettingsMiscViewModel
import com.dashlane.settings.ui.R
import com.dashlane.ui.common.compose.components.BaseEmptyContent

@SuppressWarnings("LongMethod", "kotlin:S107")
@Composable
fun SettingsSearchScreen(
    accountViewModel: SettingsAccountViewModel,
    advancedViewModel: SettingsAdvancedViewModel,
    mainMiscViewModel: SettingsMainMiscViewModel,
    applicationLockViewModel: SettingsApplicationLockViewModel,
    miscViewModel: SettingsMiscViewModel,
    autofillViewModel: SettingsAutofillViewModel,
    notificationsViewModel: SettingsNotificationsViewModel,
    backupViewModel: SettingsBackupViewModel,
    usageDataViewModel: SettingsUsageDataViewModel,
    helpViewModel: SettingsHelpViewModel,
    onBackPressed: () -> Unit,
    applicationLockSideEffect: (SettingsApplicationLockState.SideEffect) -> Unit,
    miscSideEffect: (SettingsMiscState.SideEffect) -> Unit,
    accountSideEffect: (SettingsAccountState.SideEffect) -> Unit,
    advancedSideEffect: (SettingsAdvancedState.SideEffect) -> Unit,
    mainMiscSideEffect: (SettingsMainMiscState.SideEffect) -> Unit,
    autofillSideEffect: (SettingsAutofillState.SideEffect) -> Unit,
    notificationsSideEffect: (SettingsNotificationsState.SideEffect) -> Unit,
    backupSideEffect: (SettingsBackupState.SideEffect) -> Unit,
    checkHelpSideEffect: (SettingsHelpState.SideEffect) -> Unit,
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
    BackHandler(enabled = true) { onBackPressed() }
    val accountSettingsState by accountViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val advancedSettingsState by advancedViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val mainMiscSettingsState by mainMiscViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val applicationLockSettingsState by applicationLockViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val miscSettingsState by miscViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val autofillSettingsState by autofillViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val notificationsSettingsState by notificationsViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val backupSettingsState by backupViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val usageDataSettingsState by usageDataViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    val helpSettingsState by helpViewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = applicationLockViewModel) {
        applicationLockViewModel.stateFlow.sideEffect.collect { applicationLockSideEffect(it) }
    }
    LaunchedEffect(key1 = miscViewModel) {
        miscViewModel.stateFlow.sideEffect.collect { miscSideEffect(it) }
    }
    LaunchedEffect(key1 = accountViewModel) {
        accountViewModel.stateFlow.sideEffect.collect { accountSideEffect(it) }
    }
    LaunchedEffect(key1 = advancedViewModel) {
        advancedViewModel.stateFlow.sideEffect.collect { advancedSideEffect(it) }
    }
    LaunchedEffect(key1 = mainMiscViewModel) {
        mainMiscViewModel.stateFlow.sideEffect.collect { mainMiscSideEffect(it) }
    }
    LaunchedEffect(key1 = autofillViewModel) {
        autofillViewModel.stateFlow.sideEffect.collect { autofillSideEffect(it) }
    }
    LaunchedEffect(key1 = notificationsViewModel) {
        notificationsViewModel.stateFlow.sideEffect.collect { notificationsSideEffect(it) }
    }
    LaunchedEffect(key1 = backupViewModel) {
        backupViewModel.stateFlow.sideEffect.collect { backupSideEffect(it) }
    }
    LaunchedEffect(key1 = helpViewModel) {
        helpViewModel.stateFlow.sideEffect.collect { checkHelpSideEffect(it) }
    }

    SettingsSearchContent(
        accountSettings = accountSettingsState.data.filteredSettings,
        advancedSettings = advancedSettingsState.data.filteredSettings,
        mainMiscSettings = mainMiscSettingsState.data.filteredSettings,
        applicationLockSettings = applicationLockSettingsState.data.filteredSettings,
        miscSettings = miscSettingsState.data.filteredSettings,
        autofillSettings = autofillSettingsState.data.filteredSettings,
        notificationsSettings = notificationsSettingsState.data.filteredSettings,
        backupSettings = backupSettingsState.data.filteredSettings,
        usageDataSettings = usageDataSettingsState.data.filteredSettings,
        helpSettings = helpSettingsState.data.filteredSettings,
        onBackPressed = onBackPressed,
        sendSearchEvent = {
            accountViewModel.sendEvent(SettingsEvent.Search(it))
            advancedViewModel.sendEvent(SettingsEvent.Search(it))
            mainMiscViewModel.sendEvent(SettingsEvent.Search(it))
            applicationLockViewModel.sendEvent(SettingsEvent.Search(it))
            miscViewModel.sendEvent(SettingsEvent.Search(it))
            autofillViewModel.sendEvent(SettingsEvent.Search(it))
            notificationsViewModel.sendEvent(SettingsEvent.Search(it))
            backupViewModel.sendEvent(SettingsEvent.Search(it))
            usageDataViewModel.sendEvent(SettingsEvent.Search(it))
            helpViewModel.sendEvent(SettingsEvent.Search(it))
        },
        sendAccountEvent = { event -> accountViewModel.sendEvent(event) },
        sendAdvancedEvent = { event -> advancedViewModel.sendEvent(event) },
        sendMainMiscEvent = { event -> mainMiscViewModel.sendEvent(event) },
        sendApplicationLockEvent = { event -> applicationLockViewModel.sendEvent(event) },
        sendMiscEvent = { event -> miscViewModel.sendEvent(event) },
        sendAutofillEvent = { event -> autofillViewModel.sendEvent(event) },
        sendNotificationsEvent = { event -> notificationsViewModel.sendEvent(event) },
        sendBackupEvent = { event -> backupViewModel.sendEvent(event) },
        sendUsageDataEvent = { event -> usageDataViewModel.sendEvent(event) },
        sendHelpEvent = { event -> helpViewModel.sendEvent(event) },
    )

    SettingsDialog(applicationLockSettingsState.data.dialog) { dialog -> applicationLockViewModel.sendEvent(dialog) }
    SettingsDialog(miscSettingsState.data.dialog) { dialog -> miscViewModel.sendEvent(dialog) }
    SettingsDialog(mainMiscSettingsState.data.dialog) { dialog -> mainMiscViewModel.sendEvent(dialog) }
}

@Composable
private fun <T : Settings> SettingsDialog(dialog: SettingsDialog?, sendEvent: (SettingsEvent<T>) -> Unit) {
    dialog?.let {
        SettingsDialog(
            dialog = it,
            onConfirm = { dialog -> sendEvent(DialogPositive(dialog)) },
            onDismiss = { dialog -> sendEvent(DialogNegative(dialog)) },
        )
    }
}

@SuppressWarnings("LongMethod", "CyclomaticComplexMethod", "kotlin:S107", "kotlin:S3776")
@Composable
fun SettingsSearchContent(
    accountSettings: List<SettingsItemData>,
    advancedSettings: List<SettingsItemData>,
    mainMiscSettings: List<SettingsItemData>,
    applicationLockSettings: List<SettingsItemData>,
    miscSettings: List<SettingsItemData>,
    autofillSettings: List<SettingsItemData>,
    notificationsSettings: List<SettingsItemData>,
    backupSettings: List<SettingsItemData>,
    usageDataSettings: List<SettingsItemData>,
    helpSettings: List<SettingsItemData>,
    onBackPressed: () -> Unit,
    sendSearchEvent: (String) -> Unit,
    sendAccountEvent: (SettingsEvent<Account>) -> Unit,
    sendAdvancedEvent: (SettingsEvent<Advanced>) -> Unit,
    sendMainMiscEvent: (SettingsEvent<MainMisc>) -> Unit,
    sendApplicationLockEvent: (SettingsEvent<ApplicationLock>) -> Unit,
    sendMiscEvent: (SettingsEvent<Misc>) -> Unit,
    sendAutofillEvent: (SettingsEvent<Autofill>) -> Unit,
    sendNotificationsEvent: (SettingsEvent<Notifications>) -> Unit,
    sendBackupEvent: (SettingsEvent<Backup>) -> Unit,
    sendUsageDataEvent: (SettingsEvent<UsageData>) -> Unit,
    sendHelpEvent: (SettingsEvent<Help>) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BaseSearchField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!textFieldLoaded) {
                        focusRequester.requestFocus()
                        textFieldLoaded = true
                    }
                },
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                sendSearchEvent(it)
            },
            placeholder = stringResource(R.string.settings_search_field_placeholder),
            leadingIcon = {
                ButtonMedium(
                    onClick = onBackPressed,
                    mood = Mood.Neutral,
                    intensity = Intensity.Supershy,
                    layout = ButtonLayout.IconOnly(
                        iconToken = IconTokens.arrowLeftOutlined,
                        contentDescription = stringResource(R.string.and_accessibility_back_button),
                    ),
                )
            },
        )

        @Suppress("ComplexCondition")
        if (accountSettings.isEmpty() &&
            advancedSettings.isEmpty() &&
            mainMiscSettings.isEmpty() &&
            applicationLockSettings.isEmpty() &&
            miscSettings.isEmpty() &&
            autofillSettings.isEmpty() &&
            notificationsSettings.isEmpty() &&
            backupSettings.isEmpty() &&
            usageDataSettings.isEmpty() &&
            helpSettings.isEmpty()
        ) {
            val (title, description) = if (searchQuery.isEmpty()) {
                stringResource(id = R.string.settings_search_empty_content_and_search_title) to
                    stringResource(id = R.string.settings_search_empty_content_and_search_description)
            } else {
                stringResource(id = R.string.settings_search_empty_content_title) to
                    stringResource(id = R.string.settings_search_empty_content_description)
            }
            Spacer(modifier = Modifier.weight(1f))
            BaseEmptyContent(
                modifier = Modifier.fillMaxSize(),
                iconToken = IconTokens.actionSearchOutlined,
                title = title,
                description = description,
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        
        if (accountSettings.isNotEmpty()) {
            SettingsCategory(
                title = stringResource(R.string.setting_premium_category),
                settings = accountSettings,
                sendEvent = sendAccountEvent,
            )
        }
        val advancedTitle = stringResource(R.string.settings_category_advanced)
        if (advancedSettings.isNotEmpty()) {
            SettingsCategory(
                title = advancedTitle,
                settings = advancedSettings,
                sendEvent = sendAdvancedEvent,
            )
        }
        val securityTitle = "$advancedTitle > ${stringResource(R.string.settings_category_security)}"
        if (applicationLockSettings.isNotEmpty()) {
            SettingsCategory(
                title = "$securityTitle > ${stringResource(R.string.settings_app_lock)}",
                settings = applicationLockSettings,
                sendEvent = sendApplicationLockEvent,
            )
        }
        if (miscSettings.isNotEmpty()) {
            SettingsCategory(
                title = "$securityTitle > ${stringResource(R.string.settings_category_sharing)}",
                settings = miscSettings,
                sendEvent = sendMiscEvent,
            )
        }
        val generalTitle = "$advancedTitle > ${stringResource(R.string.settings_category_general)}"
        if (autofillSettings.isNotEmpty()) {
            SettingsCategory(
                title = "$generalTitle > ${stringResource(R.string.setting_in_app_login_category)}",
                settings = autofillSettings,
                sendEvent = sendAutofillEvent,
            )
        }
        if (notificationsSettings.isNotEmpty()) {
            SettingsCategory(
                title = "$generalTitle > ${stringResource(R.string.notifications_settings_header)}",
                settings = notificationsSettings,
                sendEvent = sendNotificationsEvent,
            )
        }
        if (backupSettings.isNotEmpty()) {
            SettingsCategory(
                title = "$generalTitle > ${stringResource(R.string.setting_backup_category)}",
                settings = backupSettings,
                sendEvent = sendBackupEvent,
            )
        }
        if (usageDataSettings.isNotEmpty()) {
            SettingsCategory(
                title = "$generalTitle > ${stringResource(R.string.setting_logs_header)}",
                settings = usageDataSettings,
                sendEvent = sendUsageDataEvent,
            )
        }
        SettingsHelpContent(
            helpSettings = helpSettings,
            sendEvent = sendHelpEvent,
        )
        SettingsMainMiscContent(
            mainMiscSettings = mainMiscSettings,
            sendEvent = sendMainMiscEvent,
        )
    }
}

@Preview
@Composable
fun SettingsSearchContentPreview() {
    DashlaneTheme {
        EmptySettingsSearchContent(
            advancedSettings = listOf(
                SettingsItemData.Click(
                    id = Advanced.Security,
                    title = stringResource(R.string.settings_category_security),
                    description = stringResource(R.string.settings_category_security_description),
                    enabled = true,
                ),
            ),
        )
    }
}

@Preview
@Composable
fun SettingsSearchEmptyContentPreview() {
    DashlaneTheme {
        EmptySettingsSearchContent()
    }
}

@Composable
private fun EmptySettingsSearchContent(advancedSettings: List<SettingsItemData> = emptyList()) =
    SettingsSearchContent(
        accountSettings = emptyList(),
        advancedSettings = advancedSettings,
        mainMiscSettings = emptyList(),
        applicationLockSettings = emptyList(),
        miscSettings = emptyList(),
        autofillSettings = emptyList(),
        notificationsSettings = emptyList(),
        backupSettings = emptyList(),
        usageDataSettings = emptyList(),
        helpSettings = emptyList(),
        onBackPressed = {},
        sendSearchEvent = {},
        sendAccountEvent = {},
        sendAdvancedEvent = {},
        sendMainMiscEvent = {},
        sendApplicationLockEvent = {},
        sendMiscEvent = {},
        sendAutofillEvent = {},
        sendNotificationsEvent = {},
        sendBackupEvent = {},
        sendUsageDataEvent = {},
        sendHelpEvent = {},
    )