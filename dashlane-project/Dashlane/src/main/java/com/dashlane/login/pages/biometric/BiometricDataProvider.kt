package com.dashlane.login.pages.biometric

import androidx.biometric.BiometricPrompt
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.biometricrecovery.MasterPasswordResetIntroDialogActivity
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.pages.LoginLockBaseDataProvider
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import javax.inject.Inject

class BiometricDataProvider @Inject constructor(
    successIntentFactory: LoginSuccessIntentFactory,
    override val biometricAuthModule: BiometricAuthModule,
    sessionManager: SessionManager,
    lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    val biometricRecovery: BiometricRecovery,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : LoginLockBaseDataProvider<BiometricContract.Presenter>(
    lockManager,
    successIntentFactory,
    inAppLoginManager,
    sessionManager,
    bySessionUsageLogRepository
),
    BiometricContract.DataProvider {

    override val username = sessionManager.session?.userId ?: ""

    override fun initLockSetting(lockSetting: LockSetting) {
        super.initLockSetting(lockSetting)
        biometricAuthModule.referrer = lockSetting.lockReferrer
    }

    override fun createMasterPasswordResetIntroActivityIntent() = presenter.activity?.let { activity ->
        if (biometricRecovery.isFeatureAvailable && !biometricRecovery.isFeatureKnown) {
            MasterPasswordResetIntroDialogActivity.newIntent(activity)
        } else {
            null
        }
    }

    override fun challengeAuthentication(cryptoObject: BiometricPrompt.CryptoObject): Boolean {
        return lockManager.unlock(LockPass.ofBiometric(cryptoObject))
    }

    override fun unlockWeakBiometric(): Boolean = lockManager.unlock(LockPass.ofWeakBiometric())
}