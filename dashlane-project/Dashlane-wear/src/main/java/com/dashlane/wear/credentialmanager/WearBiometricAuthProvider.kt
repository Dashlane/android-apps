package com.dashlane.wear.credentialmanager

import androidx.biometric.BiometricPrompt
import com.dashlane.credentialmanager.authentication.CredentialBiometricAuthProvider
import com.dashlane.user.Username
import javax.inject.Inject

class WearBiometricAuthProvider @Inject constructor() : CredentialBiometricAuthProvider {
    override fun getBiometricPromptData(username: Username?) = null

    override fun getCurrentCryptoObject(): BiometricPrompt.CryptoObject? {
        throw UnsupportedOperationException()
    }
}