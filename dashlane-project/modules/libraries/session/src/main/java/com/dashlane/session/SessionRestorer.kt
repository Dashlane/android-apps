package com.dashlane.session

import com.dashlane.user.Username
import kotlinx.coroutines.Job

interface SessionRestorer {
    val job: Job

    fun canRestoreSession(
        user: String,
        serverKey: String?,
        acceptLoggedOut: Boolean = false,
    ): Boolean

    suspend fun restore(username: Username?): Session?

    suspend fun restoreSession(
        username: Username,
        serverKey: String?,
        accessKey: String? = null,
        acceptLoggedOut: Boolean = false,
    ): Session?
}