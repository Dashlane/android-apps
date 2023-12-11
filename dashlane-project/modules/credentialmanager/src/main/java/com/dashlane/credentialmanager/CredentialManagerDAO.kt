package com.dashlane.credentialmanager

import android.content.Context
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

interface CredentialManagerDAO {
    suspend fun savePasskeyCredential(
        counter: Long,
        privateKey: SyncObject.Passkey.PrivateKey,
        credentialId: String,
        rpName: String,
        rpId: String,
        userHandle: String,
        userDisplayName: String,
        keyAlgorithm: Long
    ): VaultItem<SyncObject.Passkey>?

    suspend fun savePasswordCredential(
        context: Context,
        title: String?,
        website: String?,
        login: String,
        password: String,
        packageName: String?
    ): VaultItem<SyncObject.Authentifiant>?

    suspend fun updatePasskey(itemId: String, lastUsedDate: Instant, counter: Long): Boolean
}