package com.dashlane.dagger.singleton

import com.dashlane.account.UserAccountStorage
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillUsageLog
import com.dashlane.autofill.AutofillAnalyzerDef.IUserPreferencesAccess
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.autofill.internal.AutofillApiEntryPoint
import com.dashlane.autofill.linkedservices.AppMetaDataToLinkedAppsMigration
import com.dashlane.autofill.linkedservices.RememberToLinkedAppsMigration
import com.dashlane.autofill.rememberaccount.AutofillApiRememberAccountComponent
import com.dashlane.autofill.viewallaccounts.AutofillApiViewAllAccountsComponent
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.core.DataSync
import com.dashlane.core.DataSyncNotification
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.crashreport.CrashReporter
import com.dashlane.cryptography.CryptographyComponent
import com.dashlane.cryptography.SharingCryptographyComponent
import com.dashlane.database.DatabaseProvider
import com.dashlane.debug.DaDaDa
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.followupnotification.FollowUpNotificationEntryPoint
import com.dashlane.hermes.LogFlush
import com.dashlane.hermes.LogRepository
import com.dashlane.logger.utils.LogsSender
import com.dashlane.navigation.Navigator
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.permission.PermissionsManager
import com.dashlane.preference.DashlanePreferencesComponent
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.observer.CryptographyMigrationObserver
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.session.repository.LockRepository
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sharing.SharingKeysHelperComponent
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.injection.DataAccessComponent
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.component.UiPartEntryPoint
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.util.DarkThemeHelper
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import com.dashlane.util.inject.ApplicationComponent
import com.dashlane.util.log.UserSupportFileLoggerApplicationCreated
import com.dashlane.vault.VaultReportLogger
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@InstallIn(SingletonComponent::class)
@EntryPoint
interface SingletonComponentProxy :
    ApplicationComponent,
    UiPartEntryPoint,
    DashlanePreferencesComponent,
    DataAccessComponent,
    SharingKeysHelperComponent,
    AutofillApiEntryPoint,
    AutofillApiViewAllAccountsComponent,
    AutofillApiRememberAccountComponent,
    CryptographyComponent,
    SharingCryptographyComponent,
    FollowUpNotificationEntryPoint {
    override val permissionsManager: PermissionsManager
    override val crashReporter: CrashReporter
    val daDaDa: DaDaDa
    override val mainDataAccessor: MainDataAccessor
    val dataSync: DataSync
    val announcementCenter: AnnouncementCenter
    val userAccountStorage: UserAccountStorage
    val cryptoObjectHelper: CryptoObjectHelper
    override val sessionManager: SessionManager
    val accountStatusRepository: AccountStatusRepository
    val teamspaceRepository: TeamspaceManagerRepository
    val logRepository: LogRepository
    val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository
    val userDataRepository: UserDataRepository
    val userDatabaseRepository: UserDatabaseRepository
    val lockRepository: LockRepository
    val darkThemeHelper: DarkThemeHelper
    val userSupportFileLoggerApplicationCreated: UserSupportFileLoggerApplicationCreated
    val biometricRecovery: BiometricRecovery
    val tokenService: GetTokenService
    val m2xIntentFactory: M2xIntentFactory
    val autofillUsageLog: IAutofillUsageLog
    override val userPreferencesAccess: IUserPreferencesAccess
    val sessionCredentialsSaver: SessionCredentialsSaver
    val logFlush: LogFlush
    override val navigator: Navigator
    val userCryptographyRepository: UserCryptographyRepository
    val cryptographyMigrationObserver: CryptographyMigrationObserver
    val teamspaceForceCategorizationManager: TeamspaceForceCategorizationManager
    val databaseProvider: DatabaseProvider
    val dataStorageProvider: DataStorageProvider
    val sharingItemUpdater: SharingItemUpdater
    val dataIdentifierSharingXmlConverter: DataIdentifierSharingXmlConverter
    val vaultReportLogger: VaultReportLogger
    val clock: Clock
    val premiumStatusManager: FormattedPremiumStatusManager
    val authenticatorAppConnection: AuthenticatorAppConnection
    val appMetaDataToLinkedAppsMigration: AppMetaDataToLinkedAppsMigration
    val rememberToLinkedAppsMigration: RememberToLinkedAppsMigration
    val linkedServicesHelper: LinkedServicesHelper
    val racletteLogger: RacletteLogger
    val dataSyncNotification: DataSyncNotification
    val logsSender: LogsSender
    val packageNameSignatureHelper: PackageNameSignatureHelper
    val knownApplicationProvider: KnownApplicationProvider
}
