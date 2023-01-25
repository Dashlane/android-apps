package com.dashlane.authentication

import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.session.AppKey
import com.dashlane.session.LocalKey
import com.dashlane.session.SessionTrasher
import com.dashlane.session.Username
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureStorageManager
import dagger.Lazy
import javax.inject.Inject

class AuthenticationLocalKeyRepositoryImpl @Inject constructor(
    private val localKeyRepository: LocalKeyRepository,
    private val secureStorageManager: SecureStorageManager,
    private val sessionTrasher: Lazy<SessionTrasher>
) : AuthenticationLocalKeyRepository {

    override suspend fun createForRemote(
        username: Username,
        appKey: AppKey,
        cryptographyMarker: CryptographyMarker
    ): LocalKey {
        
        sessionTrasher.get().trash(username, false)
        return localKeyRepository.createLocalKey(username, appKey, cryptographyMarker)
    }

    override fun validateForLocal(
        username: Username,
        appKey: AppKey
    ): AuthenticationLocalKeyRepository.LocalResult {
        val localKey = localKeyRepository.getLocalKey(username, appKey)
            ?: throw AuthenticationLocalKeyRepository.AccessLocalKeyException()

        val secretKey = secureStorageManager.getKeyData(SecureDataKey.SECRET_KEY, username, localKey)?.decodeUtf8ToString()
            ?: throw AuthenticationLocalKeyRepository.AccessSecretKeyException()

        return AuthenticationLocalKeyRepository.LocalResult(secretKey = secretKey, localKey = localKey)
    }
}
