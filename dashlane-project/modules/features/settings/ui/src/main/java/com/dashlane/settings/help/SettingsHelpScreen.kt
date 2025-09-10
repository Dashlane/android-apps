package com.dashlane.settings.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.dashlane.settings.Help
import com.dashlane.settings.SettingsCategory
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.ui.R

@Composable
fun SettingsHelpScreen(
    modifier: Modifier = Modifier,
    helpViewModel: SettingsHelpViewModel,
    checkHelpSideEffect: (SettingsHelpState.SideEffect) -> Unit,
) {
    val helpState by helpViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = helpViewModel) {
        helpViewModel.stateFlow.sideEffect.collect { checkHelpSideEffect(it) }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SettingsHelpContent(
            helpSettings = helpState.data.helpSettings,
            sendEvent = { event -> helpViewModel.sendEvent(event) },
        )
    }
}

@Composable
fun ColumnScope.SettingsHelpContent(
    helpSettings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<Help>) -> Unit,
) {
    if (helpSettings.isNotEmpty()) {
        SettingsCategory(
            title = stringResource(R.string.settings_category_help),
            settings = helpSettings,
            sendEvent = sendEvent,
        )
    }
}

@Preview
@Composable
fun SettingsHelpContentPreview() {
    DashlanePreview {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            SettingsHelpContent(
                helpSettings = listOf(
                    SettingsItemData.Click(
                        id = Help.TroubleShooting,
                        title = stringResource(R.string.settings_help_troubleshooting),
                        description = stringResource(R.string.settings_help_troubleshooting_description),
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Help.UserSupportId,
                        title = stringResource(R.string.setting_about_crash_report_id),
                        description = "AC1BD703",
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Help.PrivacyPolicy,
                        title = stringResource(R.string.settings_privacy_policy),
                        description = "",
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Help.TermsOfService,
                        title = stringResource(R.string.settings_tos),
                        description = "",
                        enabled = true,
                    ),
                    SettingsItemData.Click(
                        id = Help.Licenses,
                        title = stringResource(R.string.settings_title_licenses),
                        description = "",
                        enabled = true,
                    ),
                ),
                sendEvent = {},
            )
        }
    }
}