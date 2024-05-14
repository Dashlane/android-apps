package com.dashlane.dagger.singleton

import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.api.AutofillCreateAccountServiceImpl
import com.dashlane.autofill.api.AutofillGeneratePasswordServiceImpl
import com.dashlane.autofill.api.AutofillNavigationServiceImpl
import com.dashlane.autofill.api.AutofillUpdateAccountServiceImpl
import com.dashlane.autofill.api.KeyboardAutofillServiceImpl
import com.dashlane.autofill.api.securitywarnings.UserPreferencesRememberSecurityWarningsJsonRepository
import com.dashlane.autofill.changepassword.AutofillChangePasswordLogger
import com.dashlane.autofill.changepassword.domain.AutofillUpdateAccountService
import com.dashlane.autofill.core.AutofillChangePasswordLoggerImpl
import com.dashlane.autofill.core.AutofillCreateAccountLoggerImpl
import com.dashlane.autofill.core.AutofillGeneratePasswordLoggerImpl
import com.dashlane.autofill.core.AutofillLinkServiceLoggerImpl
import com.dashlane.autofill.core.AutofillSaveRequestLoggerImpl
import com.dashlane.autofill.core.AutofillSecurityWarningsLoggerImpl
import com.dashlane.autofill.core.AutofillViewAllAccountsLoggerImpl
import com.dashlane.autofill.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.generatepassword.AutofillGeneratePasswordLogger
import com.dashlane.autofill.generatepassword.AutofillGeneratePasswordService
import com.dashlane.autofill.rememberaccount.view.AutofillLinkServiceLogger
import com.dashlane.autofill.request.save.AutofillSaveRequestLogger
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.model.ExternalRepositoryRememberSecurityWarningsService
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsRepository
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.autofill.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.core.domain.AccountStorageImpl
import com.dashlane.core.legacypremium.ConflictingBillingPlatformProviderImpl
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverterImpl
import com.dashlane.credentialmanager.CredentialManagerDAO
import com.dashlane.credentialmanager.CredentialManagerDAOImpl
import com.dashlane.credentialmanager.CredentialManagerIntent
import com.dashlane.credentialmanager.CredentialManagerIntentImpl
import com.dashlane.credentialmanager.CredentialManagerLocker
import com.dashlane.credentialmanager.CredentialManagerLockerImpl
import com.dashlane.csvimport.csvimport.CsvImportViewTypeProvider
import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.device.DeviceInfoRepositoryImpl
import com.dashlane.masterpassword.MasterPasswordChanger
import com.dashlane.masterpassword.MasterPasswordChangerImpl
import com.dashlane.notification.badge.SharingInvitationRepository
import com.dashlane.notification.badge.SharingInvitationRepositoryImpl
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.plans.ui.view.PurchaseCheckingCoordinatorImpl
import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.premium.current.other.CurrentPlanStatusProviderImpl
import com.dashlane.premium.offer.common.PurchaseCheckingCoordinator
import com.dashlane.premium.offer.common.StoreOffersManager
import com.dashlane.premium.offer.common.UserBenefitStatusProvider
import com.dashlane.premium.offer.common.UserBenefitStatusProviderImpl
import com.dashlane.premium.offer.details.ConflictingBillingPlatformProvider
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.securearchive.BackupCoordinatorImpl
import com.dashlane.session.UserDataRepository
import com.dashlane.session.repository.UserDataRepositoryImpl
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.session.repository.UserDatabaseRepositoryImpl
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.sharing.SharingSyncCommunicatorImpl
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.M2xIntentFactoryImpl
import com.dashlane.ui.adapter.CsvImportViewTypeProviderImpl
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.ui.util.PasswordGeneratorWrapperImpl
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.useractivity.RacletteLoggerImpl
import com.dashlane.useractivity.SharingDeveloperLogger
import com.dashlane.useractivity.SharingDeveloperLoggerImpl
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
    fun bindRacletteLogger(impl: RacletteLoggerImpl): RacletteLogger

    @Binds
    fun bindSharingDeveloperLogger(impl: SharingDeveloperLoggerImpl): SharingDeveloperLogger

    @Binds
    fun bindAutofillLinkServiceLogger(impl: AutofillLinkServiceLoggerImpl): AutofillLinkServiceLogger

    @Binds
    fun bindCredentialManagerIntent(impl: CredentialManagerIntentImpl): CredentialManagerIntent

    @Binds
    fun bindCredentialManagerLocker(impl: CredentialManagerLockerImpl): CredentialManagerLocker

    @Binds
    fun bindCredentialManagerDatabase(impl: CredentialManagerDAOImpl): CredentialManagerDAO
}