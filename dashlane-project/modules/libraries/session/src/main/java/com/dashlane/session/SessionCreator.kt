package com.dashlane.session

import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.user.Username
import javax.inject.Inject

class SessionCreator @Inject constructor() {

    @Throws(InvalidRemoteKeyException::class)
    fun createSession(
        username: Username,
        accessKey: String,
        secretKey: String,
        localKey: LocalKey,
        appKey: AppKey,
        remoteKey: VaultKey.RemoteKey?,
    ): Session {
        if (remoteKey == null && appKey !is AppKey.Password) {
            throw InvalidRemoteKeyException()
        }
        return Session(username, accessKey, secretKey, localKey, appKey, remoteKey)
    }
}

class InvalidRemoteKeyException : Exception()