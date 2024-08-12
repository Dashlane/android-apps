package com.dashlane.authentication.localkey

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.user.Username

interface AuthenticationLocalKeyRepository {

    suspend fun createForRemote(
        username: Username,
        appKey: AppKey,
        cryptographyMarker: CryptographyMarker
    ): LocalKey

    fun validateForLocal(
        username: Username,
        appKey: AppKey
    ): LocalResult

    data class LocalResult(val secretKey: String, val localKey: LocalKey)

    class AccessSecretKeyException : Exception()
    class AccessLocalKeyException : Exception()
}