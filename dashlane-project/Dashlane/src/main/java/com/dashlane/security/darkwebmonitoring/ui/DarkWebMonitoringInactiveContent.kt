package com.dashlane.security.darkwebmonitoring.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.IntroScreenContent

@Composable
internal fun DarkWebMonitoringInactiveContent(
    startMonitoring: () -> Unit,
) {
    IntroScreenContent(
        iconToken = IconTokens.featureDarkWebMonitoringOutlined,
        title = stringResource(R.string.dark_web_module_title_get_alerts),
        items = listOf(
            IntroScreenContent.Item(
                icon = IconTokens.webOutlined,
                title = stringResource(R.string.dark_web_module_inactive_discover_title),
                description = stringResource(R.string.dark_web_module_inactive_discover_description),
            ),
            IntroScreenContent.Item(
                icon = IconTokens.notificationOutlined,
                title = stringResource(R.string.dark_web_module_inactive_be_the_first_title),
                description = stringResource(R.string.dark_web_module_inactive_be_the_first_description),
            ),
            IntroScreenContent.Item(
                icon = IconTokens.protectionOutlined,
                title = stringResource(R.string.dark_web_module_inactive_advice_title),
                description = stringResource(R.string.dark_web_module_inactive_advice_description),
            ),
        ),
        action = IntroScreenContent.Action(
            text = stringResource(R.string.dark_web_module_start_monitoring_cta),
            onClick = startMonitoring,
        ),
    )
}

@Preview
@Composable
private fun DarkWebMonitoringInactiveContentPreview() {
    DashlanePreview {
        DarkWebMonitoringInactiveContent(
            startMonitoring = {},
        )
    }
}