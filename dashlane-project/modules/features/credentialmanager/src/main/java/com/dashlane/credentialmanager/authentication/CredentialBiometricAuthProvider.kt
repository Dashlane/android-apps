package com.dashlane.credentialmanager.authentication

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.credentials.provider.BiometricPromptData
import com.dashlane.user.Username

interface CredentialBiometricAuthProvider {
    fun getBiometricPromptData(username: Username?): BiometricPromptData?

    fun getCurrentCryptoObject(): CryptoObject?
}