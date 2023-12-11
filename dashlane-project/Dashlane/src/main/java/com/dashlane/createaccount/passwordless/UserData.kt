package com.dashlane.createaccount.passwordless

import com.dashlane.cryptography.ObfuscatedByteArray

data class UserData(
    val login: String,
    val pinCode: String,
    val useBiometrics: Boolean,
    val masterPassword: ObfuscatedByteArray,
    val termsOfServicesAccepted: Boolean,
    val privacyPolicyAccepted: Boolean
)