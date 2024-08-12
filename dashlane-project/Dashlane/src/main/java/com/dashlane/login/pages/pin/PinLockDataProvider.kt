package com.dashlane.login.pages.pin

import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockValidator
import com.dashlane.login.pages.LoginLockBaseDataProvider
import com.dashlane.pin.PinSetupRepository
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PinLockDataProvider @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val pinSetupRepository: PinSetupRepository,
    successIntentFactory: LoginSuccessIntentFactory,
    lockManager: LockManager,
    logRepository: LogRepository
) : LoginLockBaseDataProvider<PinLockContract.Presenter>(
    lockManager,
    successIntentFactory
),
    PinLockContract.DataProvider {

    override val username = sessionManager.session?.userId ?: ""

    override val userPin = StringBuilder()

    override var currentStep: Int = 0

    override var firstStepPin: String? = null

    private val loginLogger = LoginLogger(logRepository)

    override fun appendToUserPin(value: Int): Boolean = (userPin.length < LockValidator.PIN_CODE_LENGTH).also {
        if (it) {
            userPin.append(value)
        }
        onUserPinUpdated(false)
    }

    override fun onUserPinUpdated(disableAnimationEffect: Boolean) {
        if (checkPinCodeComplete()) {
            presenter.enableAllKeyboardButtons(false)
            presenter.launch {
                
                delay(10)
                checkInput(userPin.toString())
            }
        }
    }

    private fun checkPinCodeComplete(): Boolean = userPin.length == LockValidator.PIN_CODE_LENGTH

    private fun checkInput(userInput: String) {
        if (lockManager.unlock(LockPass.ofPin(userInput))) {
            loginLogger.logSuccess(loginMode = LoginMode.Pin)
            presenter.onUnlockSuccess()
        } else {
            loginLogger.logWrongPin()
            lockManager.addFailUnlockAttempt()
            if (lockManager.hasFailedUnlockTooManyTimes()) {
                presenter.logoutTooManyAttempts(errorMessage = null, showToast = canUseMasterPassword())
            } else {
                presenter.onUnlockError()
            }
        }
    }

    override fun removeLastPinNumber() {
        if (userPin.isNotEmpty()) {
            userPin.deleteCharAt(userPin.length - 1)
        }
    }

    override fun savePinValue() {
        val pin = firstStepPin ?: throw IllegalStateException("Pin cannot null")
        applicationCoroutineScope.launch {
            pinSetupRepository.savePinValue(pin)
        }
    }

    override fun canUseMasterPassword(): Boolean {
        return sessionManager.session?.let { pinSetupRepository.canUseMasterPassword(it.userId) } ?: true
    }
}
