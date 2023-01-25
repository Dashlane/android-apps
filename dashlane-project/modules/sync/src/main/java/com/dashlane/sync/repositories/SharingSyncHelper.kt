package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decryptBase64ToUtf8String
import com.dashlane.cryptography.encryptUtf8ToBase64String
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sync.domain.SyncCryptographyException
import com.dashlane.sync.repositories.strategies.toUserAuthorization
import com.dashlane.sync.sharing.SharingSync
import javax.inject.Inject
import com.dashlane.cryptography.SharingKeys as CryptographySharingKeys
import com.dashlane.server.api.endpoints.account.SharingKeys as ApiSharingKeys



open class SharingSyncHelper @Inject constructor(
    private val sharingCryptography: SharingCryptography,
    private val sharingKeysHelper: SharingKeysHelper,
    private val sharingSync: SharingSync
) {
    val shouldRequestSharingKeys: Boolean
        get() = sharingKeysHelper.publicKey == null

    suspend fun syncSharing(
        serverCredentials: ServerCredentials,
        sharingKeys: ApiSharingKeys?,
        sharingSummary: SyncDownloadService.Data.SharingSummary,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncProgressChannel: SyncProgressChannel?
    ) {
        val encryptedPrivateKey = sharingKeys?.privateKey
        val publicKey = sharingKeys?.publicKey
        if (encryptedPrivateKey != null && publicKey != null) {
            runCatching {
                cryptographyEngineFactory.createDecryptionEngine().use { decryptionEngine ->
                    decryptPrivateKey(encryptedPrivateKey.asEncryptedBase64(), decryptionEngine)
                }
            }.onSuccess { privateKey ->
                saveSharingKeys(CryptographySharingKeys(publicKey, privateKey))
            }
        }

        syncProgressChannel?.trySend(SyncProgress.SharingSync)
        sharingSync.syncSharing(serverCredentials.toUserAuthorization(), sharingSummary)
    }

    fun generateSharingKeys(): CryptographySharingKeys =
        try {
            sharingCryptography.generateSharingKeys()
        } catch (e: CryptographyException) {
            throw SyncCryptographyException("Sharing key generation failed. ", e)
        }

    fun encryptPrivateKey(
        privateKey: String,
        encryptionEngine: EncryptionEngine
    ): EncryptedBase64String =
        encryptionEngine.encryptUtf8ToBase64String(privateKey, compressed = false)

    private fun decryptPrivateKey(
        privateKey: EncryptedBase64String,
        decryptionEngine: DecryptionEngine
    ): String =
        decryptionEngine.decryptBase64ToUtf8String(privateKey, compressed = false)

    fun saveSharingKeys(sharingKeys: CryptographySharingKeys) {
        sharingKeysHelper.publicKey = sharingKeys.public.value
        sharingKeysHelper.privateKey = sharingKeys.private.value
    }
}