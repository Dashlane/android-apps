package com.dashlane.createaccount

import android.content.Context
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.toHttpUrl
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.createEmail
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.model.getDefaultCountry
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class AccountCreationSetup @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val dataSaver: DataSaver,
    private val userPreferencesManager: UserPreferencesManager,
    private val authenticatorAppConnection: AuthenticatorAppConnection,
    private val urlDomainRegistryFactory: UrlDomainRegistryFactory,
    private val dataSync: DataSync,
    private val teamSpaceAccessor: OptionalProvider<TeamSpaceAccessor>
) {

    suspend fun setupCreatedAccount(
        username: String,
        userOrigin: String?,
    ) {
        userPreferencesManager.putString(ConstantsPrefs.USER_ORIGIN, userOrigin)
        insertVaultEmail(username)
        backupOtps()
        dataSync.sync(Trigger.ACCOUNT_CREATION)
    }

    private suspend fun insertVaultEmail(
        username: String
    ) {
        val timeStamp = Instant.now()
        val (emailType, teamId) = teamSpaceAccessor.get()?.enforcedSpace?.let { SyncObject.Email.Type.PRO to it.teamId }
            ?: (SyncObject.Email.Type.PERSO to null)
        val email = createEmail(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                formatLang = context.getDefaultCountry(),
                syncState = SyncState.MODIFIED,
                locallyViewedDate = Instant.EPOCH,
                creationDate = timeStamp,
                userModificationDate = timeStamp,
                teamSpaceId = teamId
            ),
            type = emailType,
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
}
