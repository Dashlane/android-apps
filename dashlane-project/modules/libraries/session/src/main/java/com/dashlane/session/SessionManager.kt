package com.dashlane.session

import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.login.LoginMode
import com.dashlane.user.Username

interface SessionManager {

    val session: Session?

    suspend fun loadSession(
        username: Username,
        appKey: AppKey,
        secretKey: String,
        localKey: LocalKey,
        accessKey: String? = null,
        loginMode: LoginMode
    ): SessionResult

    suspend fun destroySession(session: Session, byUser: Boolean, forceLogout: Boolean = true)

    fun attach(observer: SessionObserver)

    fun detach(observer: SessionObserver)

    fun detachAll()
}

sealed class SessionResult {
    data class Success(val session: Session) : SessionResult()

    data class Error(val errorCode: ErrorCode, val errorReason: String, val cause: Exception? = null) : SessionResult()

    enum class ErrorCode {
        ERROR_UKI,

        ERROR_LOCAL_KEY,

        ERROR_INIT,

        ERROR_REMOTE_KEY,

        ERROR_SESSION_ACCESS_KEY
    }
}