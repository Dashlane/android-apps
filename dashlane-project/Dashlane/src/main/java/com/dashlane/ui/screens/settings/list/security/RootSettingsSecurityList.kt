package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateHolder
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.lock.LockManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.navigation.Navigator
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.util.Toaster
import com.dashlane.util.inject.OptionalProvider
import kotlinx.coroutines.CoroutineScope

@Suppress("UseDataClass")
class RootSettingsSecurityList(
    private val context: Context,
    coroutineScope: CoroutineScope,
    lockManager: LockManager,
    securityHelper: SecurityHelper,
    biometricAuthModule: BiometricAuthModule,
    navigator: Navigator,
    screenshotPolicy: ScreenshotPolicy,
    preferencesManager: PreferencesManager,
    teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    sessionManager: SessionManager,
    userAccountStorage: UserAccountStorage,
    sessionCredentialsSaver: SessionCredentialsSaver,
    dialogHelper: DialogHelper,
    cryptographyRepository: UserCryptographyRepository,
    rootHeader: SettingHeader,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    biometricRecovery: BiometricRecovery,
    toaster: Toaster,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    subscriptionCodeRepository: SubscriptionCodeRepository,
    accountRecoveryKeySettingStateHolder: AccountRecoveryKeySettingStateHolder,
    userDataRepository: UserDataRepository,
    richIconsSettingProvider: RichIconsSettingProvider
) {

    private val settingsSecurityApplicationLockList = SettingsSecurityApplicationLockList(
        context = context,
        lockManager = lockManager,
        securityHelper = securityHelper,
        biometricAuthModule = biometricAuthModule,
        teamSpaceAccessorProvider = teamSpaceAccessorProvider,
        sessionManager = sessionManager,
        userAccountStorage = userAccountStorage,
        dialogHelper = dialogHelper,
        sensibleSettingsClickHelper = sensibleSettingsClickHelper,
        biometricRecovery = biometricRecovery,
        use2faSettingStateHolder = use2faSettingStateHolder,
        sessionCredentialsSaver = sessionCredentialsSaver,
        navigator = navigator,
        teamspaceNotificator = teamspaceRestrictionNotificator,
        accountRecoveryKeySettingStateHolder = accountRecoveryKeySettingStateHolder,
    )

    private val settingsSecurityMiscList = SettingsSecurityMiscList(
        context = context,
        coroutineScope = coroutineScope,
        navigator = navigator,
        screenshotPolicy = screenshotPolicy,
        preferencesManager = preferencesManager,
        teamSpaceAccessorProvider = teamSpaceAccessorProvider,
        sessionManager = sessionManager,
        dialogHelper = dialogHelper,
        cryptographyRepository = cryptographyRepository,
        sensibleSettingsClickHelper = sensibleSettingsClickHelper,
        masterPasswordFeatureAccessChecker = masterPasswordFeatureAccessChecker,
        toaster = toaster,
        userAccountStorage = userAccountStorage,
        subscriptionCodeRepository = subscriptionCodeRepository,
        userDataRepository = userDataRepository,
        richIconsSettingProvider = richIconsSettingProvider
    )

    val root = SettingScreenItem(
        navigator,
        AnyPage.SETTINGS_SECURITY,
        object : SettingItem {
            override val id = "security"
            override val header = rootHeader
            override val title = context.getString(R.string.settings_category_security)
            override val description = context.getString(R.string.settings_category_security_description)
            override fun isEnable() = true
            override fun isVisible() = true
            override fun onClick(context: Context) = Unit
        },
        listOf(
            
            settingsSecurityApplicationLockList.getAll(),
            settingsSecurityMiscList.getAll()
        ).flatten()
    )
}
