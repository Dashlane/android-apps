package com.dashlane.sync.cryptochanger

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyChanger
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyFixedSalt
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.XmlDecryptionEngine
import com.dashlane.cryptography.XmlEncryptionEngine
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.changeCryptographyForBase64
import com.dashlane.cryptography.createEncryptionEngine
import com.dashlane.cryptography.decryptBase64ToXmlTransaction
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encryptByteArrayToBase64String
import com.dashlane.cryptography.encryptXmlTransactionToBase64String
import com.dashlane.cryptography.forXml
import com.dashlane.cryptography.generateFixedSalt
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.cryptography.use
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.account.SharingKeys
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.endpoints.sync.CryptoChangeUploadService
import com.dashlane.server.api.endpoints.sync.MasterPasswordConfirmationService
import com.dashlane.server.api.endpoints.sync.MasterPasswordDownloadService
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction
import com.dashlane.server.api.endpoints.sync.SyncUploadTransaction
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.time.InstantEpochMilli
import com.dashlane.server.api.time.toInstant
import com.dashlane.session.AppKey
import com.dashlane.session.Session
import com.dashlane.session.VaultKey
import com.dashlane.session.serverKeyUtf8Bytes
import com.dashlane.sync.vault.SyncVault
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import com.dashlane.xml.serializer.XmlException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class SyncCryptoChangerImpl @Inject constructor(
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator,
    private val downloadService: MasterPasswordDownloadService,
    private val passwordChangeUploadService: MasterPasswordUploadService,
    private val cryptoChangeUploadService: CryptoChangeUploadService,
    private val confirmationService: MasterPasswordConfirmationService,
    private val remoteKeyIdGenerator: RemoteKeyIdGenerator
) : SyncCryptoChanger {
    override suspend fun updateCryptography(
        authorization: Authorization.User,
        userKeys: Session.UserKeys,
        newUserKeys: Session.UserKeys,
        cryptographyMarker: CryptographyMarker?,
        authTicket: String?,
        ssoServerKey: ByteArray?,
        syncVault: SyncVault,
        uploadReason: MasterPasswordUploadService.Request.UploadReason?,
        publishProgress: suspend (SyncCryptoChanger.Progress) -> Unit
    ): SyncObject.Settings {
        val appKey = userKeys.app
        val newAppKey = newUserKeys.app
        val vaultKey = userKeys.vault
        val newVaultKey = newUserKeys.vault

        val cryptoUpdateArgs = prepareCryptoUpdate(
            authorization,
            appKey,
            newAppKey,
            vaultKey,
            newVaultKey,
            cryptographyMarker,
            publishProgress
        )

        val updateVerification = createUpdateVerificationRequest(appKey, newAppKey, ssoServerKey)

        val response = try {
            passwordChangeUploadService.execute(
                authorization,
                MasterPasswordUploadService.Request(
                    remoteKeys = cryptoUpdateArgs.remoteKeys,
                    sharingKeys = cryptoUpdateArgs.reEncryptedSharingKeys,
                    updateVerification = updateVerification,
                    transactions = cryptoUpdateArgs.reEncryptedTransactions,
                    authTicket = authTicket,
                    timestamp = cryptoUpdateArgs.timestamp,
                    uploadReason = uploadReason
                )
            )
        } catch (e: DashlaneApiException) {
            throw SyncCryptoChangerUploadException(cause = e)
        }

        
        
        updateLatestBackupDatetime(
            cryptoUpdateArgs = cryptoUpdateArgs,
            newLatestBackupTime = response.data.timestamp.toInstant(),
            syncVault = syncVault
        )

        publishProgress(SyncCryptoChanger.Progress.Completed)

        return cryptoUpdateArgs.newSettings
    }

    private suspend fun updateLatestBackupDatetime(
        cryptoUpdateArgs: CryptoUpdateArgs,
        newLatestBackupTime: Instant,
        syncVault: SyncVault
    ) {
        val descriptors = cryptoUpdateArgs.reEncryptedTransactions.mapNotNull {
            val kClass = SyncObjectType.forTransactionTypeOrNull(it.type)?.kClass
                ?: return@mapNotNull null
            kClass to it.identifier
        }
        syncVault.lastSyncTime = newLatestBackupTime
        syncVault.applyBackupDate(descriptors = descriptors, backupTimeMillis = newLatestBackupTime)
    }

    override suspend fun updateCryptography(
        authorization: Authorization.User,
        userKeys: Session.UserKeys,
        cryptographyMarker: CryptographyMarker
    ): SyncObject.Settings {
        val appKey = userKeys.app
        val vaultKey = userKeys.vault

        val cryptoUpdateArgs = prepareCryptoUpdate(
            authorization = authorization,
            appKey = appKey,
            newAppKey = appKey, 
            vaultKey = vaultKey,
            newVaultKey = vaultKey, 
            cryptographyMarker = cryptographyMarker
        )

        cryptoChangeUploadService.execute(
            authorization,
            CryptoChangeUploadService.Request(
                remoteKeys = cryptoUpdateArgs.remoteKeys,
                sharingKeys = cryptoUpdateArgs.reEncryptedSharingKeys,
                transactions = cryptoUpdateArgs.reEncryptedTransactions,
                timestamp = cryptoUpdateArgs.timestamp
            )
        )

        return cryptoUpdateArgs.newSettings
    }

    private suspend fun prepareCryptoUpdate(
        authorization: Authorization.User,
        appKey: AppKey,
        newAppKey: AppKey,
        vaultKey: VaultKey,
        newVaultKey: VaultKey,
        cryptographyMarker: CryptographyMarker?,
        publishProgress: suspend (SyncCryptoChanger.Progress) -> Unit = {}
    ): CryptoUpdateArgs {
        publishProgress(SyncCryptoChanger.Progress.Downloading)

        val downloadResponse = try {
            downloadService.execute(authorization)
        } catch (e: DashlaneApiException) {
            throw SyncCryptoChangerDownloadException(cause = e)
        }
        val downloadResponseData = downloadResponse.data

        checkOtp2status(downloadResponseData, appKey)

        val settings = getSettings(downloadResponseData.data.transactions, vaultKey)
        val marker = getCryptographyMarker(cryptographyMarker, settings, newAppKey)
        val salt =
            saltGenerator.generateFixedSalt(marker) 
        val newSettings = settings.copy {
            cryptoUserPayload = marker.value
            cryptoFixedSalt = salt?.data
        }

        val reEncryptedTransactions: List<SyncUploadTransaction>
        val reEncryptedSharingKeys: SharingKeys
        vaultKey.cryptographyKey.use(cryptography::createDecryptionEngine)
            .use { decryptionEngine ->
                createVaultEncryptionEngine(newVaultKey, marker, salt).use { encryptionEngine ->
                    val cryptographyChanger =
                        CryptographyChanger(decryptionEngine, encryptionEngine)

                    reEncryptedTransactions = cryptographyChanger.changeCryptography(
                        downloadResponseData.data.transactions,
                        encryptionEngine,
                        newSettings,
                        publishProgress
                    )

                    reEncryptedSharingKeys = cryptographyChanger.changeCryptography(
                        downloadResponseData.data.sharingKeys
                    )
                }
            }

        publishProgress(SyncCryptoChanger.Progress.Uploading)

        val remoteKeys = getRemoteKeys(newVaultKey, newAppKey, marker, salt)

        return CryptoUpdateArgs(
            newSettings,
            reEncryptedSharingKeys,
            reEncryptedTransactions,
            remoteKeys,
            downloadResponseData.timestamp
        )
    }

    private fun getRemoteKeys(
        newVaultKey: VaultKey,
        newAppKey: AppKey,
        marker: CryptographyMarker,
        salt: CryptographyFixedSalt?
    ) =
        if (newVaultKey is VaultKey.RemoteKey) {
            listOf(
                RemoteKey(
                    type = when (newAppKey) {
                        is AppKey.Password -> RemoteKey.Type.MASTER_PASSWORD
                        is AppKey.SsoKey -> RemoteKey.Type.SSO
                    },
                    uuid = remoteKeyIdGenerator.generateRemoteKeyId(),
                    key = encryptRemoteKey(newVaultKey, newAppKey, marker, salt)
                )
            )
        } else {
            null
        }

    private fun getSettings(
        transactions: List<SyncDownloadTransaction>,
        vaultKey: VaultKey
    ): SyncObject.Settings {
        val settingsTransaction =
            transactions.first { it.type == SyncObjectType.SETTINGS.transactionType }
        return vaultKey.cryptographyKey.use(cryptography::createDecryptionEngine)
            .forXml()
            .decryptTransaction(settingsTransaction)
            .toObject(SyncObjectType.SETTINGS) as SyncObject.Settings
    }

    private fun getCryptographyMarker(
        cryptographyMarker: CryptographyMarker?,
        settings: SyncObject.Settings,
        newAppKey: AppKey
    ) = cryptographyMarker ?: settings.cryptoUserPayload?.toCryptographyMarkerOrNull()
    ?: when (newAppKey) {
        is AppKey.Password -> CryptographyMarker.Flexible.Defaults.argon2d
        is AppKey.SsoKey -> CryptographyMarker.Flexible.Defaults.noDerivation64
    }

    private fun encryptRemoteKey(
        remoteKey: VaultKey.RemoteKey,
        appKey: AppKey,
        marker: CryptographyMarker,
        salt: CryptographyFixedSalt?
    ) =
        createAppEncryptionEngine(appKey, marker, salt)
            .use { encryptionEngine ->
                remoteKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray)
                    .use(encryptionEngine::encryptByteArrayToBase64String)
                    .value
            }

    private fun createAppEncryptionEngine(
        appKey: AppKey,
        marker: CryptographyMarker,
        salt: CryptographyFixedSalt?
    ) =
        
        appKey.cryptographyKey.use { cryptography.createEncryptionEngine(marker, it, salt) }

    private fun createVaultEncryptionEngine(
        vaultKey: VaultKey,
        marker: CryptographyMarker,
        salt: CryptographyFixedSalt?
    ) =
        if (vaultKey is VaultKey.RemoteKey) {
            
            
            vaultKey.cryptographyKey.use(cryptography::createFlexibleNoDerivation64EncryptionEngine)
        } else {
            check(marker.keyType == CryptographyKey.Type.PASSWORD) {
                error("Cryptography marker mismatch, expected password marker, but was $marker.")
            }
            vaultKey.cryptographyKey.use { cryptography.createEncryptionEngine(marker, it, salt) }
        }

    override suspend fun reAuthorizeDevice(authorization: Authorization.User) {
        withContext(Dispatchers.Default) { confirmationService.execute(authorization) }
    }

    private data class CryptoUpdateArgs(
        val newSettings: SyncObject.Settings,
        val reEncryptedSharingKeys: SharingKeys,
        val reEncryptedTransactions: List<SyncUploadTransaction>,
        val remoteKeys: List<RemoteKey>?,
        val timestamp: InstantEpochMilli
    )
}



private fun checkOtp2status(
    downloadResponseData: MasterPasswordDownloadService.Data,
    appKey: AppKey
) {
    val isServerKeyExpected = downloadResponseData.otpStatus.isServerKeyRequired
    if (appKey is AppKey.Password && appKey.isServerKeyNotNull != isServerKeyExpected)
        error("App key OTP2 status mismatch, expected server key: $isServerKeyExpected.")
}

private fun XmlDecryptionEngine.decryptTransaction(
    transaction: SyncDownloadTransaction
) = try {
    decryptBase64ToXmlTransaction(transaction.content!!.asEncryptedBase64())
} catch (e: CryptographyException) {
    throw SyncCryptoChangerCryptographyException(
        message = "Unable to decrypt transaction ${transaction.identifier}.",
        cause = e
    )
} catch (e: XmlException) {
    throw SyncCryptoChangerCryptographyException(
        message = "Unable to parse transaction ${transaction.identifier}.",
        cause = e
    )
}

private fun XmlEncryptionEngine.encryptSettings(settings: SyncObject.Settings): String =
    encryptXmlTransactionToBase64String(settings.toTransaction()).value

private suspend fun CryptographyChanger.changeCryptography(
    transactions: List<SyncDownloadTransaction>,
    encryptionEngine: EncryptionEngine,
    newSettings: SyncObject.Settings,
    publishProgress: suspend (SyncCryptoChanger.Progress) -> Unit
): List<SyncUploadTransaction> {
    val transactionCount = transactions.size
    return transactions.mapIndexedNotNull { index, it ->
        publishProgress(SyncCryptoChanger.Progress.Ciphering(index, transactionCount))

        if (it.type == SyncObjectType.SETTINGS.transactionType) {
            SyncUploadTransaction(
                identifier = it.identifier,
                action = SyncUploadTransaction.Action.BACKUP_EDIT,
                time = it.time,
                type = it.type,
                content = encryptionEngine.forXml().encryptSettings(newSettings)
            )
        } else {
            changeCryptography(it)
        }
    }
}

private fun CryptographyChanger.changeCryptography(
    transaction: SyncDownloadTransaction
): SyncUploadTransaction? {
    val content =
        try {
            changeCryptographyForBase64(transaction.content!!.asEncryptedBase64()).value
        } catch (e: CryptographyException) {
            return null
        }
    return SyncUploadTransaction(
        identifier = transaction.identifier,
        action = SyncUploadTransaction.Action.BACKUP_EDIT,
        time = transaction.time,
        type = transaction.type,
        content = content
    )
}

private fun CryptographyChanger.changeCryptography(
    sharingKeys: SharingKeys
) = SharingKeys(
    publicKey = sharingKeys.publicKey,
    privateKey = try {
        changeCryptographyForBase64(sharingKeys.privateKey.asEncryptedBase64()).value
    } catch (e: CryptographyException) {
        throw SyncCryptoChangerCryptographyException(
            message = "Unable to re-encrypt private key.",
            cause = e
        )
    }
)

private val MasterPasswordDownloadService.Data.OtpStatus.isServerKeyRequired
    get() = this == MasterPasswordDownloadService.Data.OtpStatus.LOGIN

private fun createUpdateVerificationRequest(
    appKey: AppKey,
    newAppKey: AppKey,
    ssoServerKey: ByteArray?
): MasterPasswordUploadService.Request.UpdateVerification? {
    val securityType = appKey.toSecurityAuthType()
    val newSecurityType = newAppKey.toSecurityAuthType()

    return if (securityType == newSecurityType) {
        return null
    } else {
        MasterPasswordUploadService.Request.UpdateVerification(
            type = newSecurityType,
            serverKey = newAppKey.serverKeyUtf8Bytes?.decodeUtf8ToString(),
            ssoServerKey = ssoServerKey?.encodeBase64ToString()
        )
    }
}

private fun AppKey.toSecurityAuthType() = when (this) {
    is AppKey.Password -> if (isServerKeyNotNull) {
        AuthSecurityType.TOTP_LOGIN
    } else {
        AuthSecurityType.EMAIL_TOKEN
    }
    is AppKey.SsoKey -> AuthSecurityType.SSO
}
