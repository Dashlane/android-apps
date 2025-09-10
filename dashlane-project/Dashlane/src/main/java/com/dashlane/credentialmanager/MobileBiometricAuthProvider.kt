package com.dashlane.credentialmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.credentials.provider.BiometricPromptData
import com.dashlane.credentialmanager.authentication.CredentialBiometricAuthProvider
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.user.Username
import javax.inject.Inject

class MobileBiometricAuthProvider @Inject constructor(
    private val biometricAuthModule: BiometricAuthModule,
) : CredentialBiometricAuthProvider {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun getBiometricPromptData(username: Username?): BiometricPromptData? {
        val email = username?.email ?: return null
        val cryptoObjectAndAuthenticators =
            biometricAuthModule.createCryptoObjectAndPromptAuthenticators(email)
        val (cryptoObject, promptAuthenticators) = cryptoObjectAndAuthenticators
        if (promptAuthenticators == null) return null
        return BiometricPromptData(
            cryptoObject = cryptoObject,
            allowedAuthenticators = promptAuthenticators,
        )
    }

    override fun getCurrentCryptoObject(): CryptoObject? = biometricAuthModule.currentCryptoObject
}