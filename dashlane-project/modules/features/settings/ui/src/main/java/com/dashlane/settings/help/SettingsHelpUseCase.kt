package com.dashlane.settings.help

import android.content.Context
import com.dashlane.crashreport.CrashReporter
import com.dashlane.settings.Help
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.help.SettingsHelpState.View.Initial
import com.dashlane.settings.help.SettingsHelpState.View.UpdateUserSupportId
import com.dashlane.settings.ui.R
import com.dashlane.util.clipboard.ClipboardCopy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

@Suppress("LongMethod")
fun loadHelpSettings(
    @ApplicationContext context: Context,
    crashReporter: CrashReporter,
    state: Initial,
): Flow<SettingsEvent<Help>> {
    return flow {
        val helpSettings = mutableListOf<SettingsItemData>()

        helpSettings.add(
            SettingsItemData.Click(
                id = Help.TroubleShooting,
                title = context.getString(R.string.settings_help_troubleshooting),
                description = context.getString(R.string.settings_help_troubleshooting_description),
                enabled = true,
            ),
        )
        helpSettings.add(
            SettingsItemData.Click(
                id = Help.UserSupportId,
                title = context.getString(R.string.setting_about_crash_report_id),
                description = crashReporter.crashReporterId
                    .takeIf { it.length > 8 }
                    ?.run { substring(0, 8).uppercase(Locale.US) }
                    ?: "-",
                enabled = true,
            ),
        )
        helpSettings.add(
            SettingsItemData.Click(
                id = Help.PrivacyPolicy,
                title = context.getString(R.string.settings_privacy_policy),
                description = "",
                enabled = true,
            ),
        )
        helpSettings.add(
            SettingsItemData.Click(
                id = Help.TermsOfService,
                title = context.getString(R.string.settings_tos),
                description = "",
                enabled = true,
            ),
        )
        helpSettings.add(
            SettingsItemData.Click(
                id = Help.Licenses,
                title = context.getString(R.string.settings_title_licenses),
                description = "",
                enabled = true,
            ),
        )
        emit(SettingsEvent.SettingsLoaded(helpSettings))
    }
}

fun updateUserSupportId(
    clipboardCopy: ClipboardCopy,
    state: UpdateUserSupportId,
): Flow<SettingsEvent<Help>> {
    return flow {
        state.data.helpSettings
            .find { it.id == Help.UserSupportId }?.description
            ?.let { description ->
                clipboardCopy.copyToClipboard(data = description, sensitiveData = false, autoClear = false, showToast = false)
            }
        emit(SettingsEvent.SettingsLoaded(state.data.helpSettings))
    }
}