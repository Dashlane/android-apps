package com.dashlane.ui.common.compose.components

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun BiometricLockPrompt(
    allowedAuthenticator: Int,
    title: String,
    subtitle: String? = null,
    isBiometricPromptDisplayed: Boolean,
    negativeButtonText: String,
    cryptoObject: BiometricPrompt.CryptoObject?,
    biometricPrompt: BiometricPrompt,
) {
    if (isBiometricPromptDisplayed) {
        LaunchedEffect(cryptoObject) {
            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setAllowedAuthenticators(allowedAuthenticator)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setConfirmationRequired(false)
            subtitle?.let { promptInfoBuilder.setSubtitle(it) }

            val promptInfo = promptInfoBuilder.build()

            cryptoObject?.let {
                biometricPrompt.authenticate(promptInfo, it)
            } ?: run {
                biometricPrompt.authenticate(promptInfo)
            }
        }
    } else {
        biometricPrompt.cancelAuthentication()
    }
}