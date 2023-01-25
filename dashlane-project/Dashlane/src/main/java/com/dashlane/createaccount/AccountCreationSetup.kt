package com.dashlane.createaccount

import android.content.Context
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.core.DataSync
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.toHttpUrl
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode1
import com.dashlane.useractivity.log.usage.UsageLogCode134
import com.dashlane.useractivity.log.usage.UsageLogCode17
import com.dashlane.useractivity.log.usage.UsageLogCode53
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.copyWithPremiumStatus
import com.dashlane.util.Constants
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.createEmail
import com.dashlane.vault.model.formatTitle
import com.dashlane.xml.domain.SyncObject
import com.dashlane.vault.model.getDefaultCountry
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.Locale
import javax.inject.Inject



class AccountCreationSetup @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val dataAccessor: MainDataAccessor,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val authenticatorAppConnection: AuthenticatorAppConnection,
    private val urlDomainRegistryFactory: UrlDomainRegistryFactory
) {

    private val dataSaver
        get() = dataAccessor.getDataSaver()

    suspend fun setupCreatedAccount(
        username: String,
        isAccountReset: Boolean,
        userOrigin: String?,
        emailConsentsGiven: Boolean?
    ) {
        userPreferencesManager.putString(ConstantsPrefs.USER_ORIGIN, userOrigin)
        insertVaultEmail(username)
        backupOtps()

        logDeviceRegistered()
        logAccountCreated(
            isAccountReset,
            userOrigin,
            deviceInfoRepository.deviceCountry.uppercase(Locale.US),
            emailConsentsGiven
        )
        logPremiumStatus()
        DataSync.sync(UsageLogCode134.Origin.ACCOUNT_CREATION)
    }

    

    private suspend fun insertVaultEmail(
        username: String
    ) {
        val timeStamp = Instant.now()
        val email = createEmail(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                formatLang = context.getDefaultCountry(),
                syncState = SyncState.MODIFIED,
                locallyViewedDate = Instant.EPOCH,
                creationDate = timeStamp,
                userModificationDate = timeStamp
            ),
            type = SyncObject.Email.Type.PERSO,
            emailName = context.getString(R.string.email),
            emailAddress = username
        )

        dataSaver.save(email)
    }

    private suspend fun backupOtps() {
        val timeStamp = Instant.now()
        val urlDomainRegistryFactory = urlDomainRegistryFactory.create()

        val otpsForBackup = authenticatorAppConnection.otpsForBackup

        if (otpsForBackup.isEmpty()) return

        val authentifiants = otpsForBackup.map {
            createAuthentifiant(
                dataIdentifier = CommonDataIdentifierAttrsImpl(
                    syncState = SyncState.MODIFIED,
                    locallyViewedDate = Instant.EPOCH,
                    creationDate = timeStamp,
                    userModificationDate = timeStamp
                ),
                title = SyncObject.Authentifiant.formatTitle(it.issuer),
                deprecatedUrl = it.issuer?.let { issuer ->
                    urlDomainRegistryFactory.search(issuer)
                        .firstOrNull()
                        ?.toHttpUrl()
                        ?.toString()
                },
                login = it.user,
                otpSecret = it.takeIf { it.isStandardOtp() }?.secret.toSyncObfuscatedValue(),
                otpUrl = it.url.toSyncObfuscatedValue(),
                autoLogin = "true"
            )
        }

        val saved = dataSaver.save(authentifiants)

        if (saved) {
            authenticatorAppConnection.confirmBackupDone()
        }
    }

    private fun logDeviceRegistered() {
        log(
            UsageLogCode17(
                otpstyle = 0, 
                anonymouscomputerid = deviceInfoRepository.anonymousDeviceId,
                creation = true,
                preloaded = false
            )
        )
    }

    private fun logAccountCreated(
        isAccountReset: Boolean,
        userOrigin: String?,
        format: String,
        emailConsentsGiven: Boolean?
    ) {
        log(
            UsageLogCode1(
                origin = userOrigin,
                reset = isAccountReset,
                anonymouscomputerid = deviceInfoRepository.anonymousDeviceId,
                osformat = format,
                oslang = Constants.getOSLang(),
                format = format,
                lang = Constants.getLang().lowercase(Locale.US),
                subscribe = emailConsentsGiven
            )
        )
    }

    private fun logPremiumStatus() {
        sessionManager.session?.let { accountStatusRepository[it] }
            ?.let { premiumStatus ->
                log(UsageLogCode53().copyWithPremiumStatus(premiumStatus))
            }
    }

    private fun log(log: UsageLog, priority: Boolean = false) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(log, priority)
    }
}