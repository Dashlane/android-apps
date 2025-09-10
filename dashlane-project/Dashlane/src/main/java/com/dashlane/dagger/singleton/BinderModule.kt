package com.dashlane.dagger.singleton

import com.dashlane.ApplicationObserver
import com.dashlane.DashlaneApplicationObserver
import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateHolder
import com.dashlane.accountstatus.AccountStatusPostUpdateManager
import com.dashlane.accountstatus.AccountStatusPostUpdateManagerImpl
import com.dashlane.authentication.LoginDataReset
import com.dashlane.authentication.LoginDataResetImpl
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.UserStorageImpl
import com.dashlane.authentication.twofa.Use2faSettingStateHolder
import com.dashlane.authentication.twofa.Use2faSettingStateRefresher
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.api.AutofillCreateAccountServiceImpl
import com.dashlane.autofill.api.AutofillNavigationServiceImpl
import com.dashlane.autofill.api.AutofillUpdateAccountServiceImpl
import com.dashlane.autofill.api.KeyboardAutofillServiceImpl
import com.dashlane.autofill.api.securitywarnings.UserPreferencesRememberSecurityWarningsJsonRepository
import com.dashlane.autofill.changepassword.AutofillChangePasswordLogger
import com.dashlane.autofill.changepassword.domain.AutofillUpdateAccountService
import com.dashlane.autofill.core.AutofillChangePasswordLoggerImpl
import com.dashlane.autofill.core.AutofillCreateAccountLoggerImpl
import com.dashlane.autofill.core.AutofillGeneratePasswordLoggerImpl
import com.dashlane.autofill.core.AutofillSaveRequestLoggerImpl
import com.dashlane.autofill.core.AutofillSecurityWarningsLoggerImpl
import com.dashlane.autofill.core.AutofillViewAllAccountsLoggerImpl
import com.dashlane.autofill.core.RememberAccountLoggerImpl
import com.dashlane.autofill.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.generatepassword.AutofillGeneratePasswordLogger
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountLogger
import com.dashlane.autofill.request.save.AutofillSaveRequestLogger
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.model.ExternalRepositoryRememberSecurityWarningsService
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsRepository
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.autofill.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.biometricrecovery.BiometricRecoveryImpl
import com.dashlane.breach.BreachService
import com.dashlane.breach.BreachServiceImpl
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.changemasterpassword.MasterPasswordChangerImpl
import com.dashlane.core.DataSyncImpl
import com.dashlane.core.domain.AccountStorageImpl
import com.dashlane.core.legacypremium.ConflictingBillingPlatformProviderImpl
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProviderImpl
import com.dashlane.core.sharing.SharingSyncImpl
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverterImpl
import com.dashlane.csvimport.csvimport.CsvImportViewTypeProvider
import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.BillingManagerImpl
import com.dashlane.lock.LockHelper
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockSelfChecker
import com.dashlane.lock.LockTimeManager
import com.dashlane.lock.LockTimeManagerImpl
import com.dashlane.lock.LockTypeManager
import com.dashlane.lock.LockWatcher
import com.dashlane.lock.LockWatcherImpl
import com.dashlane.login.lock.LockNavigationHelperImpl
import com.dashlane.login.lock.LockTypeManagerImpl
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.NavigatorImpl
import com.dashlane.notification.badge.SharingInvitationRepository
import com.dashlane.notification.badge.SharingInvitationRepositoryImpl
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.notification.creator.AutoFillNotificationCreatorImpl
import com.dashlane.notification.creator.ChromeAutoFillNotificationCreatorImpl
import com.dashlane.notification.creator.ChromeAutofillNotificationCreator
import com.dashlane.password.generator.PasswordGeneratorWrapper
import com.dashlane.permission.PermissionsManager
import com.dashlane.permission.PermissionsManagerImpl
import com.dashlane.plans.ui.view.PurchaseCheckingCoordinatorImpl
import com.dashlane.premium.StoreOffersCache
import com.dashlane.premium.StoreOffersManager
import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.premium.current.other.CurrentPlanStatusProviderImpl
import com.dashlane.premium.offer.common.InAppBillingDebugPreference
import com.dashlane.premium.offer.common.PurchaseCheckingCoordinator
import com.dashlane.premium.offer.common.UserBenefitStatusProvider
import com.dashlane.premium.offer.common.UserBenefitStatusProviderImpl
import com.dashlane.premium.offer.details.ConflictingBillingPlatformProvider
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDataRepositoryImpl
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.session.repository.UserDatabaseRepositoryImpl
import com.dashlane.sharing.SharingDaoMemoryDataAccessProvider
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sharing.SharingKeysHelperImpl
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.sharing.SharingSyncCommunicatorImpl
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.storage.userdata.RichIconsSettingProviderImpl
import com.dashlane.sync.DataSync
import com.dashlane.sync.sharing.SharingSync
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.M2xIntentFactoryImpl
import com.dashlane.ui.adapter.CsvImportViewTypeProviderImpl
import com.dashlane.ui.premium.inappbilling.InAppBillingDebugPreferenceImpl
import com.dashlane.ui.screens.fragments.SharingPolicyDataProviderImpl
import com.dashlane.ui.util.PasswordGeneratorWrapperImpl
import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.usercryptography.UserCryptographyRepositoryImpl
import com.dashlane.util.Toaster
import com.dashlane.util.ToasterImpl
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.clipboard.ClipboardCopyImpl
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun bindLockWatcher(impl: LockWatcherImpl): LockWatcher

    @Binds
    fun bindLockTimeManager(impl: LockTimeManagerImpl): LockTimeManager

    @Binds
    fun bindUserStorage(impl: UserStorageImpl): UserStorage

    @Binds
    fun bindDataReset(impl: LoginDataResetImpl): LoginDataReset

    @Binds
    fun bindDataSync(dataSync: DataSyncImpl): DataSync

    @Binds
    fun bindInAppBillingDebugPreference(
        inAppBillingDebugPreference: InAppBillingDebugPreferenceImpl,
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
    fun bindPermissionsManager(impl: PermissionsManagerImpl): PermissionsManager

    @Binds
    fun bindUse2faSettingStateRefresher(impl: Use2faSettingStateHolder): Use2faSettingStateRefresher

    @Binds
    fun bindAccountRecoveryKeyStateRefresher(impl: AccountRecoveryKeySettingStateHolder): AccountRecoveryKeySettingStateRefresher

    @Binds
    fun bindAccountStatusPostUpdateManager(impl: AccountStatusPostUpdateManagerImpl): AccountStatusPostUpdateManager

    @Binds
    fun bindDataIdentifierXmlConverter(impl: DataIdentifierSharingXmlConverterImpl): DataIdentifierSharingXmlConverter

    @Binds
    fun bindSharingSyncCommunicator(impl: SharingSyncCommunicatorImpl): SharingSyncCommunicator

    @Binds
    fun bindUserAccountStorage(impl: UserAccountStorageImpl): UserAccountStorage

    @Binds
    fun bindM2xIntentFactory(impl: M2xIntentFactoryImpl): M2xIntentFactory

    @Binds
    fun bindSharingInvitationRepository(impl: SharingInvitationRepositoryImpl): SharingInvitationRepository

    @Binds
    fun bindCsvImportViewTypeProviderFactory(impl: CsvImportViewTypeProviderImpl.Factory): CsvImportViewTypeProvider.Factory

    @Binds
    fun bindCsvImportAuthentifiantHelper(impl: AccountStorageImpl): ImportAuthentifiantHelper

    @Binds
    fun bindVpnThirdPartyAuthentifiantHelper(impl: AccountStorageImpl): VpnThirdPartyAuthentifiantHelper

    @Binds
    fun bindGenerateAccountLogger(impl: AutofillGeneratePasswordLoggerImpl): AutofillGeneratePasswordLogger

    @Binds
    fun bindAccountCreation(impl: AutofillCreateAccountServiceImpl): AutofillCreateAccountService

    @Binds
    fun bindCreateAccountLogger(impl: AutofillCreateAccountLoggerImpl): AutofillCreateAccountLogger

    @Binds
    fun bindUpdatePassword(impl: AutofillUpdateAccountServiceImpl): AutofillUpdateAccountService

    @Binds
    fun bindChangePasswordLogger(impl: AutofillChangePasswordLoggerImpl): AutofillChangePasswordLogger

    @Binds
    fun bindPasswordGeneratorWrapper(impl: PasswordGeneratorWrapperImpl): PasswordGeneratorWrapper

    @Binds
    fun bindAutofillNavigationService(impl: AutofillNavigationServiceImpl): AutofillNavigationService

    @Binds
    fun bindKeyboardAutofillService(impl: KeyboardAutofillServiceImpl): KeyboardAutofillService

    @Binds
    @Singleton
    fun bindRememberSecurityWarningsService(impl: ExternalRepositoryRememberSecurityWarningsService): RememberSecurityWarningsService

    @Binds
    @Singleton
    fun bindIncorrectRememberRepository(impl: UserPreferencesRememberSecurityWarningsJsonRepository): RememberSecurityWarningsRepository

    @Binds
    @Singleton
    fun bindAutofillSecurityWarningsLogger(impl: AutofillSecurityWarningsLoggerImpl): AutofillSecurityWarningsLogger

    @Binds
    @Singleton
    fun bindAutofillViewAllAccountsLogger(impl: AutofillViewAllAccountsLoggerImpl): AutofillViewAllAccountsLogger

    @Binds
    fun bindAutofillSaveRequestLogger(impl: AutofillSaveRequestLoggerImpl): AutofillSaveRequestLogger

    @Binds
    fun bindStoreOffersManager(impl: StoreOffersCache): StoreOffersManager

    @Binds
    fun bindPurchaseCheckingCoordinator(impl: PurchaseCheckingCoordinatorImpl): PurchaseCheckingCoordinator

    @Binds
    fun bindUserBenefitStatusProvider(impl: UserBenefitStatusProviderImpl): UserBenefitStatusProvider

    @Binds
    fun bindUserDataRepository(impl: UserDataRepositoryImpl): UserDataRepository

    @Binds
    fun bindUserDatabaseRepository(impl: UserDatabaseRepositoryImpl): UserDatabaseRepository

    @Binds
    fun bindMasterPasswordChanger(impl: MasterPasswordChangerImpl): MasterPasswordChanger

    @Binds
    fun bindCurrentPlanStatusProvider(impl: CurrentPlanStatusProviderImpl): CurrentPlanStatusProvider

    @Binds
    fun bindConflictingBillingPlatformProvider(impl: ConflictingBillingPlatformProviderImpl): ConflictingBillingPlatformProvider

    @Binds
    fun bindAutofillLinkServiceLogger(impl: RememberAccountLoggerImpl): RememberAccountLogger

    @Binds
    fun bindsRichIconsSettingProvider(richIconsSettingProvider: RichIconsSettingProviderImpl): RichIconsSettingProvider

    @Binds
    fun bindsSharingPolicyDataProvider(sharingPolicyDataProvider: SharingPolicyDataProviderImpl): SharingPolicyDataProvider

    @Binds
    fun bindsBreachService(impl: BreachServiceImpl): BreachService

    @Binds
    fun bindsLockSelfChecker(impl: LockManager): LockSelfChecker

    @Binds
    fun bindClipboardCopy(impl: ClipboardCopyImpl): ClipboardCopy

    @Singleton
    @Binds
    fun bindApplicationObserver(observer: DashlaneApplicationObserver): ApplicationObserver

    @Binds
    fun bindBiometricRecovery(impl: BiometricRecoveryImpl): BiometricRecovery

    @Binds
    fun bindAutoFillNotificationCreator(impl: AutoFillNotificationCreatorImpl): AutoFillNotificationCreator

    @Binds
    fun bindChromeAutoFillNotificationCreator(impl: ChromeAutoFillNotificationCreatorImpl): ChromeAutofillNotificationCreator
}