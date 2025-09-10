package com.dashlane.settings.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.settings.Account
import com.dashlane.settings.Advanced
import com.dashlane.settings.MainMisc
import com.dashlane.settings.SettingsCategory
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.DialogNegative
import com.dashlane.settings.SettingsEvent.DialogPositive
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.main.account.SettingsAccountState
import com.dashlane.settings.main.account.SettingsAccountViewModel
import com.dashlane.settings.main.advanced.SettingsAdvancedState
import com.dashlane.settings.main.advanced.SettingsAdvancedViewModel
import com.dashlane.settings.main.misc.SettingsMainMiscState
import com.dashlane.settings.main.misc.SettingsMainMiscViewModel
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.settings.ui.R
import com.dashlane.ui.common.compose.components.socialmedia.DashlaneSocialMedia
import com.dashlane.ui.common.compose.components.socialmedia.SocialMediaBar

@Suppress("kotlin:S3776", "kotlin:S107", "LongMethod")
@Composable
fun SettingsMainScreen(
    modifier: Modifier = Modifier,
    accountViewModel: SettingsAccountViewModel,
    advancedViewModel: SettingsAdvancedViewModel,
    miscViewModel: SettingsMainMiscViewModel,
    accountSideEffect: (SettingsAccountState.SideEffect) -> Unit,
    advancedSideEffect: (SettingsAdvancedState.SideEffect) -> Unit,
    mainMiscSideEffect: (SettingsMainMiscState.SideEffect) -> Unit,
) {
    val accountSettingsState by accountViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = accountViewModel) {
        accountViewModel.stateFlow.sideEffect.collect { accountSideEffect(it) }
    }
    val advancedSettingsState by advancedViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = advancedViewModel) {
        advancedViewModel.stateFlow.sideEffect.collect { advancedSideEffect(it) }
    }
    val miscSettingsState by miscViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = miscViewModel) {
        miscViewModel.stateFlow.sideEffect.collect { mainMiscSideEffect(it) }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsAccountContent(
            accountSettings = accountSettingsState.data.accountSettings,
            sendEvent = { event -> accountViewModel.sendEvent(event) },
        )
        SettingsAdvancedContent(
            advancedSettings = advancedSettingsState.data.advancedSettings,
            sendEvent = { event -> advancedViewModel.sendEvent(event) },
        )
        SettingsMainMiscContent(
            mainMiscSettings = miscSettingsState.data.miscSettings,
            sendEvent = { event -> miscViewModel.sendEvent(event) },
        )
        SettingSocialMedia(sendEvent = { event -> miscViewModel.sendEvent(event) })
    }

    val onConfirm: (SettingsDialog) -> Unit = { dialog -> miscViewModel.sendEvent(DialogPositive(dialog)) }
    val onDismiss: (SettingsDialog) -> Unit = { dialog -> miscViewModel.sendEvent(DialogNegative(dialog)) }

    miscSettingsState.data.dialog
        ?.let { SettingsDialog(dialog = it, onConfirm = onConfirm, onDismiss = onDismiss) }
}

@Composable
fun SettingsAccountContent(
    accountSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Account>) -> Unit,
) {
    if (accountSettings.isNotEmpty()) {
        SettingsCategory(
            title = stringResource(R.string.setting_premium_category),
            settings = accountSettings,
            sendEvent = sendEvent,
        )
    }
}

@Composable
fun SettingsAdvancedContent(
    advancedSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Advanced>) -> Unit,
) {
    if (advancedSettings.isNotEmpty()) {
        SettingsCategory(
            title = stringResource(R.string.settings_category_advanced),
            settings = advancedSettings,
            sendEvent = sendEvent,
        )
    }
}

@Composable
fun SettingsMainMiscContent(
    mainMiscSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<MainMisc>) -> Unit,
) {
    if (mainMiscSettings.isNotEmpty()) {
        SettingsCategory(
            title = stringResource(R.string.settings_category_sharing),
            settings = mainMiscSettings,
            sendEvent = sendEvent,
        )
    }
}

@Composable
fun SettingSocialMedia(
    modifier: Modifier = Modifier,
    sendEvent: (SettingsEvent<MainMisc>) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            text = stringResource(R.string.settings_social_media_header),
            enforceAllCaps = true,
            style = DashlaneTheme.typography.titleSupportingSmall,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        SocialMediaBar(
            onClick = { url -> sendEvent(SettingsEvent.SocialMediaClick(url)) },
            items = DashlaneSocialMedia.entries,
        )
    }
}

@Preview
@Composable
@Suppress("LongMethod")
fun SettingsMainContentPreview() {
    DashlanePreview {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsAccountContent(
                accountSettings = listOf(
                    SettingsItemData.Click(
                        id = Account.Plan,
                        title = stringResource(R.string.plan_action_bar_title),
                        description = stringResource(R.string.frozen_state_subtitle),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Account.AccountSummary,
                        title = stringResource(R.string.account_status_settings_title),
                        description = stringResource(R.string.account_status_settings_description),
                        enabled = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingsAdvancedContent(
                advancedSettings = listOf(
                    SettingsItemData.Click(
                        id = Advanced.Security,
                        title = stringResource(R.string.settings_category_security),
                        description = stringResource(R.string.settings_category_security_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Advanced.General,
                        title = stringResource(R.string.settings_category_general),
                        description = stringResource(R.string.settings_category_general_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Advanced.Help,
                        title = stringResource(R.string.settings_category_help),
                        description = stringResource(R.string.settings_category_help_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Advanced.AddNewDevice,
                        title = stringResource(R.string.secret_transfer_settings_entry_title),
                        description = stringResource(R.string.secret_transfer_settings_entry_description),
                        enabled = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingsMainMiscContent(
                mainMiscSettings = listOf(
                    SettingsItemData.Click(
                        id = MainMisc.InviteFriend,
                        title = stringResource(R.string.invites),
                        description = stringResource(R.string.setting_send_invites_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = MainMisc.SendLove,
                        title = stringResource(R.string.setting_send_love),
                        description = stringResource(R.string.setting_send_love_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = MainMisc.Logout,
                        title = stringResource(R.string.logout),
                        description = "",
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = MainMisc.Version,
                        title = stringResource(R.string.setting_about_version),
                        description = "dev_build_dev",
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = MainMisc.Debug,
                        title = stringResource(R.string.setting_debug_link),
                        description = "",
                        enabled = true,
                    ),
                ),
                sendEvent = {},
            )
            SettingSocialMedia(sendEvent = {})
        }
    }
}

@Preview
@Composable
fun SettingSocialMediaPreview() {
    DashlanePreview {
        SettingSocialMedia(sendEvent = {})
    }
}