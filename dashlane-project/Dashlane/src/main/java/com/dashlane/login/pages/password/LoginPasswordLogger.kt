package com.dashlane.login.pages.password

import androidx.annotation.StringDef
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.hermes.generated.definitions.VerificationMode

interface LoginPasswordLogger {
    fun logEmptyPassword()

    fun logPasswordInvalid()

    fun logPasswordInvalidWithRecovery()

    fun logNetworkError(@NetworkError error: String)

    companion object {
        const val NW_ERR_OFFLINE = "Offline"
        const val NW_ERR_UKI_VALID = "UkiValid"
        const val NW_ERR_INITIAL_SYNC = "InitialSync"
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        NW_ERR_OFFLINE,
        NW_ERR_UKI_VALID,
        NW_ERR_INITIAL_SYNC
    )
    annotation class NetworkError

    interface Factory {
        fun create(
            registeredUserDevice: RegisteredUserDevice,
            verification: VerificationMode
        ): LoginPasswordLogger
    }
}