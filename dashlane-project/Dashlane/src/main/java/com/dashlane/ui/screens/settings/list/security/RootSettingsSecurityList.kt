package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.activatetotp.ActivateTotpLogger
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.lock.LockManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.navigation.Navigator
import com.dashlane.network.inject.LegacyWebservicesApi
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.SecurityHelper
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.ThreadHelper
import com.dashlane.util.Toaster
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import retrofit2.Retrofit



@Suppress("UseDataClass")
class RootSettingsSecurityList(
    private val context: Context,
    lockManager: LockManager,
    securityHelper: SecurityHelper,
    biometricAuthModule: BiometricAuthModule,
    navigator: Navigator,
    screenshotPolicy: ScreenshotPolicy,
    userPreferencesManager: UserPreferencesManager,
    teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    @LegacyWebservicesApi retrofit: Retrofit,
    sessionManager: SessionManager,
    userAccountStorage: UserAccountStorage,
    sessionCredentialsSaver: SessionCredentialsSaver,
    dialogHelper: DialogHelper,
    cryptographyRepository: UserCryptographyRepository,
    threadHelper: ThreadHelper,
    rootHeader: SettingHeader,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    accountRecovery: AccountRecovery,
    toaster: Toaster,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    activateTotpLogger: ActivateTotpLogger,
    userFeaturesChecker: UserFeaturesChecker
) {

    private val settingsSecurityApplicationLockList = SettingsSecurityApplicationLockList(
        context,
        lockManager,
        securityHelper,
        biometricAuthModule,
        teamspaceAccessorProvider,
        sessionManager,
        userAccountStorage,
        dialogHelper,
        sensibleSettingsClickHelper,
        accountRecovery,
        bySessionUsageLogRepository,
        use2faSettingStateHolder,
        activateTotpLogger,
        sessionCredentialsSaver
    )

    private val settingsSecurityMiscList = SettingsSecurityMiscList(
        context,
        navigator,
        screenshotPolicy,
        userPreferencesManager,
        teamspaceAccessorProvider,
        retrofit,
        sessionManager,
        dialogHelper,
        cryptographyRepository,
        threadHelper,
        sensibleSettingsClickHelper,
        masterPasswordFeatureAccessChecker,
        toaster,
        bySessionUsageLogRepository,
        userFeaturesChecker
    )

    val root = SettingScreenItem(
        navigator,
        AnyPage.SETTINGS_SECURITY,
        object : SettingItem {
            override val id = "security"
            override val header = rootHeader
            override val title = context.getString(R.string.settings_category_security)
            override val description = context.getString(R.string.settings_category_security_description)
            override fun isEnable(context: Context) = true
            override fun isVisible(context: Context) = true
            override fun onClick(context: Context) = Unit
        },
        listOf(
            
            settingsSecurityApplicationLockList.getAll(),
            settingsSecurityMiscList.getAll()
        ).flatten()
    )
}
