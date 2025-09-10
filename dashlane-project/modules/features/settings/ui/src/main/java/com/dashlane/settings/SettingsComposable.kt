package com.dashlane.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.Text
import com.dashlane.design.component.Toggle
import com.dashlane.design.component.cardBackground
import com.dashlane.design.component.tooling.firstBaselineToTop
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.withSearchHighlight
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.ui.R

@Composable
fun <T : Settings> SettingsCategory(
    title: String,
    settings: List<SettingsItemData>,
    sendEvent: (SettingsEvent<T>) -> Unit,
) {
    Column(
        modifier = Modifier.cardBackground(),
    ) {
        Text(
            modifier = Modifier
                .semantics { heading() }
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            text = title,
            enforceAllCaps = true,
            style = DashlaneTheme.typography.titleSupportingSmall,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        settings.forEach { setting ->
            if (!setting.visible) return@forEach
            when (setting) {
                is SettingsItemData.Click -> {
                    SettingsItem<T>(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = setting.enabled) { sendEvent(ItemClicked(setting.id)) }
                            .padding(end = 16.dp),
                        title = setting.title,
                        description = setting.description,
                        highlightText = setting.highlightText,
                        enabled = setting.enabled,
                        isLoading = setting.isLoading,
                    )
                }

                is SettingsItemData.Toggle -> {
                    SettingsItemToggle(
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {},
                        setting = setting,
                        sendEvent = sendEvent,
                    )
                }
            }
        }
    }
}

@Composable
fun <T : Settings> SettingsItemToggle(
    modifier: Modifier = Modifier,
    setting: SettingsItemData.Toggle,
    sendEvent: (SettingsEvent<T>) -> Unit,
) {
    if (setting.isLoading) {
        SettingsItem<T>(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            title = setting.title,
            description = setting.description,
            highlightText = setting.highlightText,
            enabled = setting.enabled,
            isLoading = true,
        )
    } else {
        Toggle(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            enabled = setting.enabled,
            checked = setting.checked,
            onCheckedChange = { checked -> sendEvent(SettingsEvent.ItemCheckChanged(setting.id, checked)) },
            label = {
                SettingsItem<T>(
                    title = setting.title,
                    description = setting.description,
                    highlightText = setting.highlightText,
                    enabled = setting.enabled,
                    isLoading = false,
                )
            },
        )
    }
}

@Composable
fun <T : Settings> SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    highlightText: String? = null,
    enabled: Boolean,
    isLoading: Boolean,
) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, bottom = 12.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .firstBaselineToTop(30.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                text = title.withSearchHighlight(
                    match = highlightText,
                    ignoreCase = true,
                ),
                style = DashlaneTheme.typography.titleBlockMedium,
                color = if (enabled) DashlaneTheme.colors.textNeutralCatchy else DashlaneTheme.colors.textOddityDisabled,
            )
            if (description.isNotBlank()) {
                Text(
                    text = description.withSearchHighlight(
                        match = highlightText,
                        ignoreCase = true,
                    ),
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = if (enabled) DashlaneTheme.colors.textNeutralQuiet else DashlaneTheme.colors.textOddityDisabled,
                )
            }
        }
        if (isLoading) {
            IndeterminateLoader(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp, start = 32.dp)
                    .size(32.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SettingsCategoryPreview() {
    DashlanePreview {
        SettingsCategory<ApplicationLock>(
            title = stringResource(R.string.settings_app_lock),
            settings = listOf(
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
                    description = stringResource(R.string.settings_change_pincode_description) + " a bit of text to make it very long",
                    enabled = true,
                ),
            ),
            sendEvent = { },
        )
    }
}

@Preview
@Composable
private fun SettingsItemPreview() {
    DashlanePreview {
        SettingsItemToggle<ApplicationLock>(
            setting = SettingsItemData.Toggle(
                id = ApplicationLock.PinLock,
                title = stringResource(R.string.settings_use_pincode),
                description = stringResource(R.string.setting_use_pincode_description),
                enabled = true,
                checked = true,
            ),
            sendEvent = { },
        )
    }
}

@Preview
@Composable
private fun SettingsItemLoadingPreview() {
    DashlanePreview {
        SettingsItemToggle<ApplicationLock>(
            setting = SettingsItemData.Toggle(
                id = ApplicationLock.PinLock,
                title = stringResource(R.string.settings_use_pincode),
                description = stringResource(R.string.setting_use_pincode_description),
                enabled = true,
                checked = true,
                isLoading = true,
            ),
            sendEvent = { },
        )
    }
}

@Preview
@Composable
private fun SettingsItemTogglePreviewWithoutDescription() {
    DashlanePreview {
        SettingsItemToggle<ApplicationLock>(
            setting = SettingsItemData.Toggle(
                id = ApplicationLock.PinLock,
                title = stringResource(R.string.settings_use_pincode),
                description = "",
                enabled = true,
                checked = true,
            ),
            sendEvent = { },
        )
    }
}

@Preview
@Composable
private fun SettingsItemClickPreviewWithoutDescription() {
    DashlanePreview {
        SettingsItem<ApplicationLock>(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            title = stringResource(R.string.settings_use_pincode),
            description = "",
            enabled = true,
            isLoading = true,
        )
    }
}