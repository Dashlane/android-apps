package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import com.dashlane.R
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.events.user.ToggleAnalytics
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.DarkThemeHelper
import com.dashlane.util.inject.OptionalProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("UseDataClass")
class RootSettingsGeneralList(
    context: Context,
    coroutineScope: CoroutineScope,
    userFeaturesChecker: UserFeaturesChecker,
    lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    navigator: Navigator,
    rootHeader: SettingHeader,
    backupCoordinator: BackupCoordinator,
    darkThemeHelper: DarkThemeHelper,
    logRepository: LogRepository,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    userPreferencesManager: UserPreferencesManager,
    globalPreferencesManager: GlobalPreferencesManager,
    followUpNotificationSettings: FollowUpNotificationSettings,
    dataSync: DataSync,
    dialogHelper: DialogHelper,
    teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
) {

    private val settingsGeneralAutoLoginList = SettingsGeneralAutoLoginList(
        context,
        lockManager,
        inAppLoginManager,
        navigator,
        userPreferencesManager
    )

    private val settingsGeneralNotificationsList = SettingsGeneralNotificationsList(
        context,
        navigator,
        followUpNotificationSettings
    )

    private val settingsGeneralBackupList = SettingsGeneralBackupList(
        context = context,
        backupCoordinator = backupCoordinator,
        sensibleSettingsClickHelper = sensibleSettingsClickHelper,
        dialogHelper = dialogHelper,
        teamSpaceAccessorProvider = teamSpaceAccessorProvider,
    )

    private val displayHeader = SettingHeader(context.getString(R.string.settings_display_category))

    private val darkThemeItem = object : SettingItem, SettingCheckable {
        override val id = "dark-theme"
        override val header = displayHeader
        override val title = context.getString(R.string.settings_dark_theme)
        override val description = context.getString(R.string.settings_dark_theme_description)
        override fun isEnable() = true
        override fun isVisible() = darkThemeHelper.isSettingAvailable
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = darkThemeHelper.isSettingEnabled
        override fun onCheckChanged(context: Context, enable: Boolean) {
            darkThemeHelper.isSettingEnabled = enable
        }
    }

    private val syncHeader =
        SettingHeader(context.getString(R.string.setting_sync_now))

    private val syncItem = object : SettingItem {
        override val id = "sync"
        override val header = syncHeader
        override val title = context.getString(R.string.setting_sync_now)
        override val description = context.getString(R.string.setting_sync_now_description)
        override fun isEnable() = true
        override fun isVisible() = true

        override fun onClick(context: Context) = dataSync.sync(Trigger.MANUAL)
    }

    private val allowSendLogs = object : SettingItem, SettingCheckable {
        override val id = "allowSendLogs"
        override val header = SettingHeader(context.getString(R.string.setting_logs_header))
        override val title = context.getString(R.string.setting_allow_logs_title)
        override val description = context.getString(R.string.setting_allow_logs_description)
        override fun isEnable() = true
        override fun isVisible() = userFeaturesChecker.has(FeatureFlip.SHOW_ALLOW_SEND_LOGS)

        override fun isChecked(context: Context): Boolean = globalPreferencesManager.allowSendLogs

        override fun onCheckChanged(context: Context, enable: Boolean) {
            coroutineScope.launch {
                
                
                if (enable) {
                    globalPreferencesManager.allowSendLogs = enable
                    logRepository.apply {
                        queueEvent(ToggleAnalytics(isAnalyticsEnabled = enable))
                        flushLogs()
                    }
                } else {
                    logRepository.apply {
                        queueEvent(ToggleAnalytics(isAnalyticsEnabled = enable))
                        flushLogs()
                    }
                    globalPreferencesManager.allowSendLogs = enable
                }
            }
        }

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
    }

    val root = SettingScreenItem(
        navigator,
        AnyPage.SETTINGS_GENERAL,
        object : SettingItem {
            override val id = "general"
            override val header = rootHeader
            override val title = context.getString(R.string.settings_category_general)
            override val description =
                context.getString(R.string.settings_category_general_description)

            override fun isEnable() = true
            override fun isVisible() = true
            override fun onClick(context: Context) {}
        },
        listOfNotNull(
            settingsGeneralAutoLoginList.getAll(),
            settingsGeneralNotificationsList.getAll(),
            settingsGeneralBackupList.getAll(),
            listOf(
                allowSendLogs,
                darkThemeItem,
                syncItem
            )
        ).flatten()
    )
}