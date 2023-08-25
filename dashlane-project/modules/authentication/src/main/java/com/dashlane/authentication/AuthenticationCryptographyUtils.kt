package com.dashlane.authentication

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.EncryptionEngine
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.XmlDecryptionEngine
import com.dashlane.cryptography.XmlEncryptionEngine
import com.dashlane.cryptography.createEncryptionEngine
import com.dashlane.cryptography.decryptBase64ToByteArray
import com.dashlane.cryptography.decryptBase64ToUtf8String
import com.dashlane.cryptography.decryptBase64ToXmlTransaction
import com.dashlane.cryptography.encryptByteArrayToBase64String
import com.dashlane.cryptography.encryptUtf8ToBase64String
import com.dashlane.cryptography.encryptXmlTransactionToBase64String
import com.dashlane.cryptography.use
import com.dashlane.session.AppKey
import com.dashlane.session.VaultKey
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import com.dashlane.xml.serializer.XmlException

internal fun Cryptography.createAppDecryptionEngine(key: AppKey): DecryptionEngine =
    key.cryptographyKey.use(::createDecryptionEngine)

internal fun Cryptography.createVaultDecryptionEngine(key: VaultKey): DecryptionEngine =
    key.cryptographyKey.use(::createDecryptionEngine)

internal fun Cryptography.createPasswordEncryptionEngine(
    key: VaultKey.Password,
    settings: Settings
): EncryptionEngine =
    key.cryptographyKey.use {
        createEncryptionEngine(settings.cryptographyMarker, it, settings.cryptographyFixedSalt)
    }

internal fun Cryptography.createRemoteKeyEncryptionEngine(
    remoteKey: VaultKey.RemoteKey
): EncryptionEngine =
    remoteKey.cryptographyKey.use(::createFlexibleNoDerivation64EncryptionEngine)

fun Cryptography.createSsoEncryptionEngine(
    ssoKey: AppKey.SsoKey
): EncryptionEngine =
    ssoKey.cryptographyKey.use(::createFlexibleNoDerivation64EncryptionEngine)

fun EncryptionEngine.encryptRemoteKey(
    remoteKey: VaultKey.RemoteKey
): EncryptedBase64String =
    remoteKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray).use(::encryptByteArrayToBase64String)

@Throws(CryptographyException::class)
internal fun DecryptionEngine.decryptRemoteKey(
    remoteKey: EncryptedBase64String
): VaultKey.RemoteKey = decryptBase64ToByteArray(remoteKey).use(VaultKey::RemoteKey)

internal fun XmlEncryptionEngine.encryptSettings(
    settings: SyncObject.Settings
): EncryptedBase64String = encryptXmlTransactionToBase64String(settings.toTransaction())

@Throws(CryptographyException::class, XmlException::class)
internal fun XmlDecryptionEngine.decryptSettings(
    settings: EncryptedBase64String
): SyncObject.Settings =
    decryptBase64ToXmlTransaction(settings).toObject(SyncObjectType.SETTINGS) as SyncObject.Settings

internal fun EncryptionEngine.encryptSharingPrivateKey(
    privateKey: SharingKeys.Private
): EncryptedBase64String = encryptUtf8ToBase64String(privateKey.value)

@Throws(CryptographyException::class)
internal fun DecryptionEngine.decryptSharingPrivateKey(
    privateKey: EncryptedBase64String
): String = decryptBase64ToUtf8String(privateKey)

@Throws(CryptographyException::class)
internal fun DecryptionEngine.decryptBackupToken(
    backupToken: EncryptedBase64String
): String = decryptBase64ToUtf8String(backupToken, compressed = true)
