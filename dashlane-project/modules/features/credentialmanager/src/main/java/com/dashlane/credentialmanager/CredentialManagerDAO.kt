package com.dashlane.credentialmanager

import android.content.Context
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm
import com.dashlane.vault.model.CloudPasskeyData
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

interface CredentialManagerDAO {
    suspend fun savePasskeyCredential(
        saveRequest: PasskeySaveRequest,
        credentialId: String,
        rpName: String,
        rpId: String,
        userHandle: String,
        userDisplayName: String,
    ): VaultItem<SyncObject.Passkey>?

    suspend fun savePasswordCredential(
        context: Context,
        title: String?,
        website: String?,
        login: String,
        password: String,
        packageName: String?,
    ): VaultItem<SyncObject.Authentifiant>?

    suspend fun updatePasskey(itemId: String, lastUsedDate: Instant, counter: Long): Boolean

    sealed class PasskeySaveRequest {
        abstract val privateKey: SyncObject.Passkey.PrivateKey?
        abstract val cloudPasskeyData: CloudPasskeyData?
        abstract val keyAlgorithm: Long
        abstract val credentialId: String

        data class Local(
            override val privateKey: SyncObject.Passkey.PrivateKey,
            override val credentialId: String,
        ) : PasskeySaveRequest() {
            override val keyAlgorithm: Long = PasskeyAlgorithm.Local.ES256.algorithmIdentifier
            override val cloudPasskeyData: CloudPasskeyData? = null
        }

        data class Cloud(
            override val cloudPasskeyData: CloudPasskeyData,
            override val credentialId: String,
        ) : PasskeySaveRequest() {
            override val keyAlgorithm: Long = PasskeyAlgorithm.Cloud.algorithmIdentifier
            override val privateKey: SyncObject.Passkey.PrivateKey? = null
        }
    }
}