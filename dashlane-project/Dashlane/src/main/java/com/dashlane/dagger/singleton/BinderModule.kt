package com.dashlane.dagger.singleton

import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateHolder
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateRefresher
import com.dashlane.activatetotp.ActivateTotpAuthenticatorConnection
import com.dashlane.activatetotp.ActivateTotpServerKeyChanger
import com.dashlane.authentication.AuthenticationLocalKeyRepositoryImpl
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.authenticator.AuthenticatorSunsetChecker
import com.dashlane.authenticator.AuthenticatorSunsetCheckerImpl
import com.dashlane.authenticator.PasswordManagerServiceStubImpl
import com.dashlane.authenticator.ipc.PasswordManagerService
import com.dashlane.core.DataSyncImpl
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProvider
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProviderImpl
import com.dashlane.core.sharing.SharingSyncImpl
import com.dashlane.crashreport.CrashReporter
import com.dashlane.crashreport.CrashReporterManager
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.BillingManagerImpl
import com.dashlane.lock.LockHelper
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockWatcher
import com.dashlane.login.LoginDataReset
import com.dashlane.login.LoginDataResetImpl
import com.dashlane.login.UserStorageImpl
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockMessageHelper
import com.dashlane.login.lock.LockMessageHelperImpl
import com.dashlane.login.lock.LockNavigationHelperImpl
import com.dashlane.login.lock.LockTimeManager
import com.dashlane.login.lock.LockTimeManagerImpl
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.lock.LockTypeManagerImpl
import com.dashlane.login.lock.LockWatcherImpl
import com.dashlane.masterpassword.MasterPasswordChangerImpl
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.NavigatorImpl
import com.dashlane.permission.PermissionsManager
import com.dashlane.permission.PermissionsManagerImpl
import com.dashlane.premium.offer.common.InAppBillingDebugPreference
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.session.repository.UserCryptographyRepositoryImpl
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sharing.SharingKeysHelperImpl
import com.dashlane.sync.DataSync
import com.dashlane.sync.sharing.SharingSync
import com.dashlane.ui.premium.inappbilling.InAppBillingDebugPreferenceImpl
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.Use2faSettingStateRefresher
import com.dashlane.util.Toaster
import com.dashlane.util.ToasterImpl
import com.dashlane.welcome.HasOtpsForBackupProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BinderModule {
    @Binds
    fun bindNavigator(navigator: NavigatorImpl): Navigator

    @Binds
    fun bindSharingKeysHelper(impl: SharingKeysHelperImpl): SharingKeysHelper

    @Binds
    fun bindLockHelper(lockManager: LockManager): LockHelper

    @Binds
    fun bindLockTypeManager(impl: LockTypeManagerImpl): LockTypeManager

    @Binds
    fun bindLockNavigationHelper(impl: LockNavigationHelperImpl): LockNavigationHelper

    @Binds
    fun bindLockMessageHelper(impl: LockMessageHelperImpl): LockMessageHelper

    @Binds
    fun bindLockWatcher(impl: LockWatcherImpl): LockWatcher

    @Binds
    fun bindLockTimeManager(impl: LockTimeManagerImpl): LockTimeManager

    @Binds
    fun bindCrashReporter(crashReporter: CrashReporterManager): CrashReporter

    @Binds
    fun bindUserStorage(impl: UserStorageImpl): UserStorage

    @Binds
    fun bindDataReset(impl: LoginDataResetImpl): LoginDataReset

    @Binds
    fun bindDataSync(dataSync: DataSyncImpl): DataSync

    @Binds
    fun bindLocalKeyRepository(
        localKeyRepository: AuthenticationLocalKeyRepositoryImpl
    ): AuthenticationLocalKeyRepository

    @Binds
    fun bindInAppBillingDebugPreference(
        inAppBillingDebugPreference: InAppBillingDebugPreferenceImpl
    ): InAppBillingDebugPreference

    @Binds
    fun bindUserCryptographyRepository(impl: UserCryptographyRepositoryImpl): UserCryptographyRepository

    @Binds
    fun bindSharingDaoMemoryDataAccessProvider(impl: SharingDaoMemoryDataAccessProviderImpl): SharingDaoMemoryDataAccessProvider

    @Binds
    fun bindSharingSync(impl: SharingSyncImpl): SharingSync

    @Binds
    fun bindBillingManager(impl: BillingManagerImpl): BillingManager

    @Binds
    fun bindToaster(impl: ToasterImpl): Toaster

    @Binds
    fun bindPasswordManagerServiceStub(impl: PasswordManagerServiceStubImpl): PasswordManagerService.Stub

    @Binds
    fun bindHasOtpsForBackupProvider(impl: AuthenticatorAppConnection): HasOtpsForBackupProvider

    @Binds
    fun bindPermissionsManager(impl: PermissionsManagerImpl): PermissionsManager

    @Binds
    fun bindUse2faSettingStateRefresher(impl: Use2faSettingStateHolder): Use2faSettingStateRefresher

    @Binds
    fun bindAccountRecoveryKeyStateRefresher(impl: AccountRecoveryKeySettingStateHolder): AccountRecoveryKeySettingStateRefresher

    @Binds
    fun bindActivateTotpServerKeyChanger(impl: MasterPasswordChangerImpl): ActivateTotpServerKeyChanger

    @Binds
    fun bindActivateTotpAuthenticatorConnection(impl: AuthenticatorAppConnection): ActivateTotpAuthenticatorConnection

    @Binds
    fun bindAuthenticatorSunsetChecker(impl: AuthenticatorSunsetCheckerImpl): AuthenticatorSunsetChecker
}
