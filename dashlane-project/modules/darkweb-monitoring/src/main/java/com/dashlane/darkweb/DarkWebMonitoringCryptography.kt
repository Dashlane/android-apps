package com.dashlane.darkweb

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.cryptography.SharingEncryptedBase64String
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.decryptBase64ToUtf8StringOrNull
import com.dashlane.cryptography.decryptRsaPkcs1OaepPaddingBase64OrNull
import javax.inject.Inject

class DarkWebMonitoringCryptography @Inject constructor(
    private val cryptography: Cryptography,
    private val sharingCryptography: SharingCryptography
) {
    fun decryptAlertKey(
        userPrivateKey: SharingKeys.Private,
        encryptedAlertKey: SharingEncryptedBase64String
    ): ByteArray? =
        sharingCryptography.decryptRsaPkcs1OaepPaddingBase64OrNull(
            encryptedAlertKey,
            userPrivateKey
        )

    fun decryptAlertContent(
        cryptographyKey: CryptographyKey.Raw32,
        encryptedAlertContents: EncryptedBase64String
    ): String? =
        cryptography.createDecryptionEngine(cryptographyKey).use { decryptionEngine ->
            decryptionEngine.decryptBase64ToUtf8StringOrNull(
                encryptedAlertContents,
                compressed = false
            )
        }
}