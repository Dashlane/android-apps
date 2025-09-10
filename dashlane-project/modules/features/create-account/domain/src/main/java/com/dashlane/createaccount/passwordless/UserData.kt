package com.dashlane.createaccount.passwordless

import com.dashlane.cryptography.ObfuscatedByteArray

data class UserData(
    val email: String,
    val pinCode: String? = null,
    val useBiometrics: Boolean,
    val password: ObfuscatedByteArray? = null,
    val termsOfServicesAccepted: Boolean,
    val offersAccepted: Boolean,
)