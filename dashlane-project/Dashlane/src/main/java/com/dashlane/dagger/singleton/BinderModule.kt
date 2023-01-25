package com.dashlane.dagger.singleton

import com.dashlane.abtesting.RemoteAbTestManager
import com.dashlane.activatetotp.ActivateTotpAuthenticatorConnection
import com.dashlane.activatetotp.ActivateTotpServerKeyChanger
import com.dashlane.authentication.AuthenticationLocalKeyRepositoryImpl
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.authenticator.PasswordManagerServiceStubImpl
import com.dashlane.authenticator.ipc.PasswordManagerService
import com.dashlane.core.DataSync
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProvider
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProviderImpl
import com.dashlane.core.sharing.SharingSyncImpl
import com.dashlane.crashreport.CrashReporter
import com.dashlane.crashreport.CrashReporterManager
import com.dashlane.featureflipping.FeatureFlipManager
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
import com.dashlane.permission.PermissionsManager
import com.dashlane.permission.PermissionsManagerImpl
import com.dashlane.premium.offer.common.InAppBillingDebugPreference
import com.dashlane.session.RemoteConfiguration
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.session.repository.UserCryptographyRepositoryImpl
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sharing.SharingKeysHelperImpl
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.DataStorageProviderImpl
import com.dashlane.sync.sharing.SharingSync
import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.GlobalActivityLifecycleListener
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.ScreenshotPolicyImpl
import com.dashlane.ui.premium.inappbilling.InAppBillingDebugPreferenceImpl
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.Use2faSettingStateRefresher
import com.dashlane.ui.screens.settings.WindowConfigurationImpl
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.useractivity.AggregateUserActivityAccountInformationProvider
import com.dashlane.useractivity.AggregateUserActivityLogSender
import com.dashlane.useractivity.AggregateUserActivityRepository
import com.dashlane.useractivity.AggregateUserActivityTeamInformationProvider
import com.dashlane.useractivity.log.aggregate.BaseAggregateLogSender
import com.dashlane.util.AppSync
import com.dashlane.util.Toaster
import com.dashlane.util.ToasterImpl
import com.dashlane.util.WindowConfiguration
import com.dashlane.welcome.HasOtpsForBackupProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet



@Module
@InstallIn(SingletonComponent::class)
interface BinderModule {
    @Binds
    fun bindSharingKeysHelper(impl: SharingKeysHelperImpl): SharingKeysHelper

    @Binds
    fun bindWindowConfiguration(impl: WindowConfigurationImpl): WindowConfiguration

    @Binds
    fun bindScreenshotPolicy(impl: ScreenshotPolicyImpl): ScreenshotPolicy

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
    fun bindActivityLifecycleListener(listener: GlobalActivityLifecycleListener): ActivityLifecycleListener

    @Binds
    fun bindCrashReporter(crashReporter: CrashReporterManager): CrashReporter

    @Binds
    @IntoSet
    fun bindRemoteAbTestManager(remoteAbTestManager: RemoteAbTestManager): RemoteConfiguration

    @Binds
    @IntoSet
    fun bindFeatureFlipManager(featureFlipManager: FeatureFlipManager): RemoteConfiguration

    @Binds
    fun bindUserStorage(impl: UserStorageImpl): UserStorage

    @Binds
    fun bindDataReset(impl: LoginDataResetImpl): LoginDataReset

    @Binds
    fun bindAppSync(dataSync: DataSync): AppSync

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
    fun bindDataStorageProvider(impl: DataStorageProviderImpl): DataStorageProvider

    @Binds
    fun bindSharingDaoMemoryDataAccessProvider(impl: SharingDaoMemoryDataAccessProviderImpl): SharingDaoMemoryDataAccessProvider

    @Binds
    fun bindSharingSync(impl: SharingSyncImpl): SharingSync

    @Binds
    fun bindAggregateUserActivityAccountInformationProvider(impl: AggregateUserActivityAccountInformationProvider): AggregateUserActivity.AccountInformationProvider

    @Binds
    fun bindAggregateUserActivityRepository(impl: AggregateUserActivityRepository): AggregateUserActivity.Repository

    @Binds
    fun bindAggregateLogSender(impl: AggregateUserActivityLogSender): BaseAggregateLogSender

    @Binds
    fun bindAggregateUserActivityTeamInformationProvider(impl: AggregateUserActivityTeamInformationProvider): AggregateUserActivity.TeamInformationProvider

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
    fun bindActivateTotpServerKeyChanger(impl: MasterPasswordChangerImpl): ActivateTotpServerKeyChanger

    @Binds
    fun bindActivateTotpAuthenticatorConnection(impl: AuthenticatorAppConnection): ActivateTotpAuthenticatorConnection
}
