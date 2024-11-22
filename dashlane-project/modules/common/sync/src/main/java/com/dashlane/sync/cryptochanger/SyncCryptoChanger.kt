package com.dashlane.sync.cryptochanger

import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.sync.vault.SyncVault
import com.dashlane.xml.domain.SyncObject

interface SyncCryptoChanger {

    suspend fun updateCryptography(
        authorization: Authorization.User,
        appKey: AppKey,
        newAppKey: AppKey,
        vaultKey: VaultKey,
        newVaultKey: VaultKey,
        cryptographyMarker: CryptographyMarker? = null,
        authTicket: String? = null,
        ssoServerKey: ByteArray? = null,
        syncVault: SyncVault,
        uploadReason: MasterPasswordUploadService.Request.UploadReason? = null,
        publishProgress: suspend (Progress) -> Unit = {}
    ): SyncObject.Settings

    suspend fun updateCryptography(
        authorization: Authorization.User,
        appKey: AppKey,
        vaultKey: VaultKey,
        cryptographyMarker: CryptographyMarker
    ): SyncObject.Settings

    suspend fun reAuthorizeDevice(authorization: Authorization.User)

    sealed class Progress {
        data object Downloading : Progress()

        data class Ciphering(val index: Int, val total: Int) : Progress()

        data object Uploading : Progress()

        data object Completed : Progress()
    }
}
