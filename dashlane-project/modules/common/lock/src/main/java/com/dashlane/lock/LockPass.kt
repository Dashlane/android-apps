package com.dashlane.lock

import androidx.biometric.BiometricPrompt
import com.dashlane.crypto.keys.AppKey

sealed class LockPass {
    data class PinPass(val pin: String) : LockPass()
    data class BiometricPass(val cryptoObject: BiometricPrompt.CryptoObject) : LockPass()
    data class PasswordPass(val appKey: AppKey) : LockPass()

    object WeakBiometricPass : LockPass()
    companion object {
        fun ofPin(pin: String) = PinPass(pin)
        fun ofPassword(appKey: AppKey) = PasswordPass(appKey)
        fun ofBiometric(cryptoObject: BiometricPrompt.CryptoObject) = BiometricPass(cryptoObject)

        fun ofWeakBiometric() = WeakBiometricPass
    }
}