package com.dashlane.login.settings

import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.ui.screens.settings.UserSettingsLogRepository
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.skocken.presentation.definition.Base
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginSettingsPresenter @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val loginSuccessIntentFactory: LoginSuccessIntentFactory,
    private val sessionManager: SessionManager,
    private val biometricRecovery: BiometricRecovery,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val lockRepository: LockRepository,
    private val biometricAuthModule: BiometricAuthModule,
    private val logRepository: LogRepository,
    private val userSettingsLogRepository: UserSettingsLogRepository
) : BasePresenter<Base.IDataProvider, LoginSettingsContract.ViewProxy>(),
    LoginSettingsContract.Presenter {

    override fun onViewChanged() {
        super.onViewChanged()
        if (!biometricAuthModule.isHardwareSetUp() || biometricAuthModule.isOnlyWeakSupported()) {
            goToLoginSyncProgress()
        }
    }

    override fun onNext() {
        sessionManager.session?.let { session ->
            if (view.biometricSettingChecked) {
                applicationCoroutineScope.launch(defaultCoroutineDispatcher) {
                    enableBiometric(session)
                    if (view.resetMpSettingChecked) {
                        enableResetMp()
                    }
                    logRepository.queueEvent(userSettingsLogRepository.get())
                }
            }
        }

        goToLoginSyncProgress()
    }

    private fun goToLoginSyncProgress() {
        val intent = loginSuccessIntentFactory.createLoginSyncProgressIntent()
        view.context.startActivity(intent)
        activity?.finishAffinity()
    }

    private suspend fun enableBiometric(session: Session) = withContext(Dispatchers.Default) {
        
        runCatching {
            sessionCredentialsSaver.saveCredentials(session)
            
            val result = biometricAuthModule.createEncryptionKeyForBiometrics(username = session.userId)
            if (!result) return@withContext
        }
        
        lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)
    }

    private fun enableResetMp() {
        
        biometricRecovery.isFeatureKnown = true
        biometricRecovery.setBiometricRecoveryFeatureEnabled(true)
    }
}
