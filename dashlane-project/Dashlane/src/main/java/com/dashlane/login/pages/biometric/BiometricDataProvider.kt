package com.dashlane.login.pages.biometric

import androidx.biometric.BiometricPrompt
import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.biometricrecovery.MasterPasswordResetIntroDialogActivity
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.pages.LoginLockBaseDataProvider
import com.dashlane.session.SessionManager
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import javax.inject.Inject

class BiometricDataProvider @Inject constructor(
    successIntentFactory: LoginSuccessIntentFactory,
    override val biometricAuthModule: BiometricAuthModule,
    private val sessionManager: SessionManager,
    lockManager: LockManager,
    private val userAccountStorage: UserAccountStorage,
    val biometricRecovery: BiometricRecovery
) : LoginLockBaseDataProvider<BiometricContract.Presenter>(
    lockManager,
    successIntentFactory
),
    BiometricContract.DataProvider {

    override val username = sessionManager.session?.userId ?: ""

    override fun createMasterPasswordResetIntroActivityIntent() = presenter.activity?.let { activity ->
        if (biometricRecovery.isFeatureAvailable() && !biometricRecovery.isFeatureKnown) {
            MasterPasswordResetIntroDialogActivity.newIntent(activity)
        } else {
            null
        }
    }

    override fun challengeAuthentication(cryptoObject: BiometricPrompt.CryptoObject): Boolean {
        return lockManager.unlock(LockPass.ofBiometric(cryptoObject))
    }

    override fun unlockWeakBiometric(): Boolean = lockManager.unlock(LockPass.ofWeakBiometric())

    override fun isAccountTypeMasterPassword(): Boolean {
        return sessionManager.session?.username
            ?.let { userAccountStorage[it]?.accountType } == UserAccountInfo.AccountType.MasterPassword
    }
}