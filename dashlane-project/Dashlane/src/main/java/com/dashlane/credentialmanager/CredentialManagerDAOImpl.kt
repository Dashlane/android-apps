package com.dashlane.credentialmanager

import android.content.Context
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.util.isValidEmail
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.createPasskey
import com.dashlane.vault.util.copyWithDefaultValue
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import java.time.Instant
import javax.inject.Inject

class CredentialManagerDAOImpl @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val dataSaver: DataSaver,
    private val sessionManager: SessionManager,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val dataSync: DataSync
) : CredentialManagerDAO {

    override suspend fun savePasskeyCredential(
        counter: Long,
        privateKey: SyncObject.Passkey.PrivateKey,
        credentialId: String,
        rpName: String,
        rpId: String,
        userHandle: String,
        userDisplayName: String,
        keyAlgorithm: Long
    ): VaultItem<SyncObject.Passkey>? {
        val createTimestamp = Instant.now()
        val passkey = createPasskey(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                syncState = SyncState.MODIFIED,
                creationDate = createTimestamp,
                userModificationDate = createTimestamp
            ),
            counter = counter,
            privateKey = privateKey,
            credentialId = credentialId,
            rpName = rpName,
            rpId = rpId,
            userHandle = userHandle,
            userDisplayName = userDisplayName,
            keyAlgorithm = keyAlgorithm
        )
        if (dataSaver.save(passkey)) {
            
            dataSync.sync(Trigger.SAVE)

            return passkey
        }
        return null
    }

    override suspend fun savePasswordCredential(
        context: Context,
        title: String?,
        website: String?,
        login: String,
        password: String,
        packageName: String?
    ): VaultItem<SyncObject.Authentifiant>? {
        val createTimestamp = Instant.now()
        val linkedServices = if (packageName != null) {
            linkedServicesHelper.getLinkedServicesWithAppSignature(packageName)
        } else {
            null
        }
        val (newLogin, newEmail) = getLoginAndEmail(login)

        return createAuthentifiant(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                syncState = SyncState.MODIFIED,
                creationDate = createTimestamp,
                userModificationDate = createTimestamp
            ),
            title = title,
            deprecatedUrl = website,
            email = newEmail,
            login = newLogin,
            password = SyncObfuscatedValue(password),
            autoLogin = "true",
            passwordModificationDate = createTimestamp,
            linkedServices = linkedServices
        ).copyWithDefaultValue(context, sessionManager.session)
            .asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
            ?.let {
                if (dataSaver.save(it)) {
                    
                    dataSync.sync(Trigger.SAVE)

                    return it
                }
                return null
            }
    }

    private fun getLoginAndEmail(login: String?): Pair<String?, String?> {
        return if (login != null) {
            if (login.isValidEmail()) {
                null to login
            } else {
                login to null
            }
        } else {
            "" to ""
        }
    }

    override suspend fun updatePasskey(
        itemId: String,
        lastUsedDate: Instant,
        counter: Long
    ): Boolean {
        val filter = vaultFilter {
            specificUid(itemId)
        }
        val updatedAccount = vaultDataQuery
            .query(filter)
            ?.asVaultItemOfClassOrNull(SyncObject.Passkey::class.java)
            ?.copySyncObject {
                this.counter = counter
            }
            ?.copyWithAttrs {
                this.locallyViewedDate = lastUsedDate
                this.locallyUsedCount = locallyUsedCount + 1
            } ?: return false
        return dataSaver.save(updatedAccount)
    }
}