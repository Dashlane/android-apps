package com.dashlane.login.pages.pin

import com.dashlane.core.KeyChainHelper
import com.dashlane.hermes.LogRepository
import com.dashlane.inapplogin.InAppLoginManager
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
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinLockDataProvider @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val lockRepository: LockRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val keyChainHelper: KeyChainHelper,
    successIntentFactory: LoginSuccessIntentFactory,
    lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    logRepository: LogRepository
) : LoginLockBaseDataProvider<PinLockContract.Presenter>(
    lockManager, successIntentFactory, inAppLoginManager,
    sessionManager, bySessionUsageLogRepository
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

                    logUsageLog35Pin(UsageLogConstant.ActionType.pinCodeDidNotMatch)

                    presenter.animateError()

                    presenter.clearInput()
                }
            }
        }
    }

    private fun checkInput(userInput: String) {
        if (lockManager.unlock(LockPass.ofPin(userInput))) {
            usageLogUnlock()
            loginLogger.logSuccess(loginMode = LoginMode.Pin)
            presenter.onUnlockSuccess()
        } else {
            usageLogFailedUnlockAttempt()
            loginLogger.logWrongPin()
            lockManager.addFailUnlockAttempt()
            if (lockManager.hasFailedUnlockTooManyTimes()) {
                presenter.logoutTooManyAttempts(null)
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
            lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)
        }

        userPreferencesManager
            .putBoolean(ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE, true)

        val pin = firstStepPin!!
        globalCoroutineScope.launch {
            userSecureStorageManager.storePin(sessionManager.session, pin)
        }

        logUsageLog35Settings(UsageLogConstant.ActionType.usePinCodeOn)
        if (!lockSetting.isPinSetterReset) {
            logUsageLog35Pin(UsageLogConstant.ActionType.setupSetPinSelected)
        } else {
            logUsageLog35Pin(UsageLogConstant.ActionType.changePinCode)
        }
    }

    override fun onShow() {
    }

    override fun onBack() {
    }

    private fun logUsageLog35Pin(action: String) {
        logUsageLog35(UsageLogConstant.ViewType.pin, action)
    }

    private fun logUsageLog35Settings(action: String) {
        logUsageLog35(UsageLogConstant.ViewType.settings, action)
    }

    private fun logUsageLog35(type: String, action: String) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode35(
                    type = type,
                    action = action
                )
            )
    }
}