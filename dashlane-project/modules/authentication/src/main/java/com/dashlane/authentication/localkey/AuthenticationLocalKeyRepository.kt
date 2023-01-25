package com.dashlane.authentication.localkey

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.session.AppKey
import com.dashlane.session.LocalKey
import com.dashlane.session.Username



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