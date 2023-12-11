package com.dashlane.login.pages.pin

import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.core.KeyChainHelper
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.lock.LockValidator
import com.dashlane.login.pages.LoginLockBaseDataProvider
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PinLockDataProvider @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val lockRepository: LockRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val keyChainHelper: KeyChainHelper,
    private val userAccountStorage: UserAccountStorage,
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

            when {
                lockSetting.isPinSetter -> presenter.launch {
                    
                    delay(10)
                    nextStep(userPin.toString(), disableAnimationEffect)
                }

                else -> presenter.launch {
                    
                    delay(10)
                    checkInput(userPin.toString())
                }
            }
        }
    }

    private fun checkPinCodeComplete(): Boolean = userPin.length == LockValidator.PIN_CODE_LENGTH

    private fun nextStep(userInput: String, disableAnimationEffect: Boolean) {
        when (currentStep) {
            0 -> {
                currentStep = 1
                firstStepPin = userInput
                presenter.onRequestReenterPin()
                presenter.clearInput()
            }

            1 -> {
                if (isPinMatching(userInput)) {
                    presenter.newPinConfirmed(disableAnimationEffect)
                } else {
                    currentStep = 0

                    presenter.animateError()

                    presenter.clearInput()
                }
            }
        }
    }

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

    private fun isPinMatching(userInput: String): Boolean =
        userInput.length == LockValidator.PIN_CODE_LENGTH && firstStepPin == userInput

    override fun removeLastPinNumber() {
        if (userPin.isNotEmpty()) {
            userPin.deleteCharAt(userPin.length - 1)
        }
    }

    override fun savePinValue() {
        sessionManager.session?.let { session ->
            keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
            if (canUseMasterPassword()) lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)
        }

        userPreferencesManager
            .putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)

        val pin = firstStepPin!!
        applicationCoroutineScope.launch {
            userSecureStorageManager.storePin(sessionManager.session, pin)
        }
    }

    override fun canUseMasterPassword(): Boolean {
        return sessionManager.session?.username?.let { userAccountStorage[it]?.accountType is UserAccountInfo.AccountType.MasterPassword } ?: true
    }
}
