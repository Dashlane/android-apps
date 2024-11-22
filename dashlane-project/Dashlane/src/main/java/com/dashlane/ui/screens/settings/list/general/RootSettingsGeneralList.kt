package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import com.dashlane.R
import com.dashlane.autofill.phishing.AutofillPhishingLogger
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.events.user.ToggleAnalytics
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.util.DialogHelper
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
    logRepository: LogRepository,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    preferencesManager: PreferencesManager,
    globalPreferencesManager: GlobalPreferencesManager,
    followUpNotificationSettings: FollowUpNotificationSettings,
    dialogHelper: DialogHelper,
    teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    autofillPhishingLogger: AutofillPhishingLogger,
    frozenStateManager: FrozenStateManager,
    sessionManager: SessionManager
) {

    private val settingsGeneralAutoLoginList = SettingsGeneralAutoLoginList(
        context = context,
        lockManager = lockManager,
        inAppLoginManager = inAppLoginManager,
        navigator = navigator,
        preferencesManager = preferencesManager,
        sessionManager = sessionManager,
        userFeaturesChecker = userFeaturesChecker,
        autofillPhishingLogger = autofillPhishingLogger,
        frozenStateManager = frozenStateManager,
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
        navigator = navigator,
    )

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
                        sessionEnded()
                    }
                } else {
                    logRepository.apply {
                        queueEvent(ToggleAnalytics(isAnalyticsEnabled = enable))
                        sessionEnded()
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
            )
        ).flatten()
    )
}