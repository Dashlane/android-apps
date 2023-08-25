package com.dashlane.dagger.singleton

import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.biometricrecovery.BiometricRecoveryLogger
import com.dashlane.biometricrecovery.BiometricRecoveryLoggerImpl
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.api.AutofillCreateAccountServiceImpl
import com.dashlane.autofill.api.AutofillGeneratePasswordServiceImpl
import com.dashlane.autofill.api.AutofillNavigationServiceImpl
import com.dashlane.autofill.api.AutofillUpdateAccountServiceImpl
import com.dashlane.autofill.api.KeyboardAutofillServiceImpl
import com.dashlane.autofill.api.changepassword.AutofillChangePasswordLogger
import com.dashlane.autofill.api.changepassword.domain.AutofillUpdateAccountService
import com.dashlane.autofill.api.common.AutofillGeneratePasswordLogger
import com.dashlane.autofill.api.common.domain.AutofillGeneratePasswordService
import com.dashlane.autofill.api.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.api.rememberaccount.view.AutofillLinkServiceLogger
import com.dashlane.autofill.api.request.save.AutofillSaveRequestLogger
import com.dashlane.autofill.api.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.api.securitywarnings.UserPreferencesRememberSecurityWarningsJsonRepository
import com.dashlane.autofill.api.securitywarnings.model.ExternalRepositoryRememberSecurityWarningsService
import com.dashlane.autofill.api.securitywarnings.model.RememberSecurityWarningsRepository
import com.dashlane.autofill.api.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.autofill.api.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.autofill.core.AutofillChangePasswordLoggerImpl
import com.dashlane.autofill.core.AutofillCreateAccountLoggerImpl
import com.dashlane.autofill.core.AutofillGeneratePasswordLoggerImpl
import com.dashlane.autofill.core.AutofillLinkServiceLoggerImpl
import com.dashlane.autofill.core.AutofillSaveRequestLoggerImpl
import com.dashlane.autofill.core.AutofillSecurityWarningsLoggerImpl
import com.dashlane.autofill.core.AutofillViewAllAccountsLoggerImpl
import com.dashlane.core.domain.AccountStorageImpl
import com.dashlane.core.premium.ConflictingBillingPlatformProviderImpl
import com.dashlane.core.premium.CurrentPlanStatusProviderImpl
import com.dashlane.core.premium.FormattedPremiumStatusImpl
import com.dashlane.core.premium.OffersFromAutofillResolverImpl
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverterImpl
import com.dashlane.csvimport.CsvImportViewTypeProvider
import com.dashlane.csvimport.ImportAuthentifiantHelper
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.device.DeviceInfoRepositoryImpl
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterCoordinatorImpl
import com.dashlane.masterpassword.MasterPasswordChanger
import com.dashlane.masterpassword.MasterPasswordChangerImpl
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.NavigatorImpl
import com.dashlane.notification.badge.SharingInvitationRepository
import com.dashlane.notification.badge.SharingInvitationRepositoryImpl
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.plans.ui.view.PurchaseCheckingCoordinatorImpl
import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.offer.common.OffersFromAutofillResolver
import com.dashlane.premium.offer.common.PurchaseCheckingCoordinator
import com.dashlane.premium.offer.common.StoreOffersManager
import com.dashlane.premium.offer.details.ConflictingBillingPlatformProvider
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.securearchive.BackupCoordinatorImpl
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDataRepositoryImpl
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.session.repository.UserDatabaseRepositoryImpl
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.sharing.SharingSyncCommunicatorImpl
import com.dashlane.storage.securestorage.CryptographyMigrationLogger
import com.dashlane.storage.securestorage.CryptographyMigrationLoggerImpl
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.M2xIntentFactoryImpl
import com.dashlane.ui.adapter.CsvImportViewTypeProviderImpl
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.ui.util.PasswordGeneratorWrapperImpl
import com.dashlane.useractivity.DashlaneDeviceExtraData
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.useractivity.RacletteLoggerImpl
import com.dashlane.useractivity.SharingDeveloperLogger
import com.dashlane.useractivity.SharingDeveloperLoggerImpl
import com.dashlane.useractivity.log.DeviceExtraData
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface SingletonAbstractModule {
    @Binds
    fun bindDeviceIdRepository(impl: DeviceInfoRepositoryImpl): DeviceInfoRepository

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
    fun bindBackupIntentCoordinator(impl: BackupCoordinatorImpl): BackupCoordinator

    @Binds
    fun bindAccountRecoveryLogger(impl: BiometricRecoveryLoggerImpl): BiometricRecoveryLogger

    @Binds
    fun bindAGeneratePassword(impl: AutofillGeneratePasswordServiceImpl): AutofillGeneratePasswordService

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
    fun bindHelpCenterNavigator(impl: HelpCenterCoordinatorImpl): HelpCenterCoordinator

    @Binds
    fun bindDashlaneDeviceExtraData(deviceExtraData: DashlaneDeviceExtraData): DeviceExtraData

    @Binds
    fun bindAutofillNavigationService(impl: AutofillNavigationServiceImpl): AutofillNavigationService

    @Binds
    fun bindNavigator(navigator: NavigatorImpl): Navigator

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
    fun bindFormattedPremiumStatusManager(impl: FormattedPremiumStatusImpl): FormattedPremiumStatusManager

    @Binds
    fun bindUserDataRepository(impl: UserDataRepositoryImpl): UserDataRepository

    @Binds
    fun bindUserDatabaseRepository(impl: UserDatabaseRepositoryImpl): UserDatabaseRepository

    @Binds
    fun bindCryptographyMigrationLogger(impl: CryptographyMigrationLoggerImpl): CryptographyMigrationLogger

    @Binds
    fun bindMasterPasswordChanger(impl: MasterPasswordChangerImpl): MasterPasswordChanger

    @Binds
    fun bindCurrentPlanStatusProvider(impl: CurrentPlanStatusProviderImpl): CurrentPlanStatusProvider

    @Binds
    fun bindOffersFromAutofillResolver(impl: OffersFromAutofillResolverImpl): OffersFromAutofillResolver

    @Binds
    fun bindConflictingBillingPlatformProvider(impl: ConflictingBillingPlatformProviderImpl): ConflictingBillingPlatformProvider

    @Binds
    fun bindRacletteLogger(impl: RacletteLoggerImpl): RacletteLogger

    @Binds
    fun bindSharingDeveloperLogger(impl: SharingDeveloperLoggerImpl): SharingDeveloperLogger

    @Binds
    fun bindAutofillLinkServiceLogger(impl: AutofillLinkServiceLoggerImpl): AutofillLinkServiceLogger
}