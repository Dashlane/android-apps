package com.dashlane.settings.main.advanced

import android.content.Context
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.settings.Advanced
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.Unlocked
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.main.advanced.SettingsAdvancedState.View
import com.dashlane.settings.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take


@Suppress("kotlin:S3776", "LongMethod")
internal fun loadAdvancedSettings(
    @ApplicationContext context: Context,
    state: View.Initial,
): Flow<SettingsEvent<Advanced>> {
    return flow {

        val advancedSettings = mutableListOf<SettingsItemData>()

        advancedSettings.add(
            SettingsItemData.Click(
                id = Advanced.Security,
                title = context.getString(R.string.settings_category_security),
                description = context.getString(R.string.settings_category_security_description),
                enabled = true,
            ),
        )

        advancedSettings.add(
            SettingsItemData.Click(
                id = Advanced.General,
                title = context.getString(R.string.settings_category_general),
                description = context.getString(R.string.settings_category_general_description),
                enabled = true,
            ),
        )

        advancedSettings.add(
            SettingsItemData.Click(
                id = Advanced.Help,
                title = context.getString(R.string.settings_category_help),
                description = context.getString(R.string.settings_category_help_description),
                enabled = true,
            ),
        )

        advancedSettings.add(
            SettingsItemData.Click(
                id = Advanced.AddNewDevice,
                title = context.getString(R.string.secret_transfer_settings_entry_title),
                description = context.getString(R.string.secret_transfer_settings_entry_description),
                enabled = true,
            ),
        )

        emit(SettingsEvent.SettingsLoaded(advancedSettings))
    }
}

fun waitForLock(
    lockManager: LockManager,
    state: View.WaitingForLock,
): Flow<SettingsEvent<Advanced>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            if (lockEvent is LockEvent.Unlock) {
                Unlocked(state.sideEffect)
            } else {
                SettingsEvent.SettingsLoaded(state.data.advancedSettings)
            }
        }
}