package com.dashlane.login.accountrecoverykey

import kotlinx.serialization.Serializable

sealed interface LoginAccountRecoveryDestination {
    @Serializable
    data class Intro(val login: String) : LoginAccountRecoveryDestination

    @Serializable
    data class EmailToken(val login: String) : LoginAccountRecoveryDestination

    @Serializable
    data class Totp(val login: String) : LoginAccountRecoveryDestination

    @Serializable
    data class EnterArk(val login: String) : LoginAccountRecoveryDestination

    @Serializable
    data object Recovery : LoginAccountRecoveryDestination

    @Serializable
    data object PinSetup : LoginAccountRecoveryDestination

    @Serializable
    data object BiometricsSetup : LoginAccountRecoveryDestination

    @Serializable
    data object ChangeMasterPassword : LoginAccountRecoveryDestination
}