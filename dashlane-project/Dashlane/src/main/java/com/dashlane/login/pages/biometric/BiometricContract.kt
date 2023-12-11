package com.dashlane.login.pages.biometric

import android.content.Intent
import androidx.biometric.BiometricPrompt
import com.dashlane.login.pages.LoginLockBaseContract
import com.dashlane.util.hardwaresecurity.BiometricAuthModule

interface BiometricContract {

    interface ViewProxy : LoginLockBaseContract.ViewProxy {
        fun showEmail(email: String)
    }

    interface Presenter : LoginLockBaseContract.Presenter

    interface DataProvider : LoginLockBaseContract.DataProvider {
        val biometricAuthModule: BiometricAuthModule

        fun challengeAuthentication(cryptoObject: BiometricPrompt.CryptoObject): Boolean

        fun createMasterPasswordResetIntroActivityIntent(): Intent?

        fun unlockWeakBiometric(): Boolean

        fun isAccountTypeMasterPassword(): Boolean
    }
}