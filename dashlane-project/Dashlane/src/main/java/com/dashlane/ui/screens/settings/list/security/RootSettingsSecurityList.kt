package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.activatetotp.ActivateTotpLogger
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.lock.LockManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.navigation.Navigator
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.SecurityHelper
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.Toaster
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
    userPreferencesManager: UserPreferencesManager,
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
    activateTotpLogger: ActivateTotpLogger,
    userFeaturesChecker: UserFeaturesChecker,
    accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    @IoCoroutineDispatcher
    ioDispatcher: CoroutineDispatcher,
    subscriptionCodeRepository: SubscriptionCodeRepository,
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
        activateTotpLogger = activateTotpLogger,
        sessionCredentialsSaver = sessionCredentialsSaver,
        accountRecoveryKeyRepository = accountRecoveryKeyRepository,
        navigator = navigator,
        teamspaceNotificator = teamspaceRestrictionNotificator
    )

    private val settingsSecurityMiscList = SettingsSecurityMiscList(
        context = context,
        coroutineScope = coroutineScope,
        navigator = navigator,
        screenshotPolicy = screenshotPolicy,
        userPreferencesManager = userPreferencesManager,
        teamSpaceAccessorProvider = teamSpaceAccessorProvider,
        sessionManager = sessionManager,
        dialogHelper = dialogHelper,
        cryptographyRepository = cryptographyRepository,
        sensibleSettingsClickHelper = sensibleSettingsClickHelper,
        masterPasswordFeatureAccessChecker = masterPasswordFeatureAccessChecker,
        toaster = toaster,
        userFeaturesChecker = userFeaturesChecker,
        userAccountStorage = userAccountStorage,
        subscriptionCodeRepository = subscriptionCodeRepository,
        ioDispatcher = ioDispatcher,
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
