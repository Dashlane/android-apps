package com.dashlane.login.pages.secrettransfer.help

import com.dashlane.authentication.RegisteredUserDevice

sealed class RecoveryHelpState {
    abstract val data: RecoveryHelpData

    data class Initial(override val data: RecoveryHelpData) : RecoveryHelpState()
    data class GoToLostKey(override val data: RecoveryHelpData) : RecoveryHelpState()
    data class GoToARK(override val data: RecoveryHelpData, val registeredUserDevice: RegisteredUserDevice.Local) : RecoveryHelpState()
}

data class RecoveryHelpData(
    val email: String? = null
)
