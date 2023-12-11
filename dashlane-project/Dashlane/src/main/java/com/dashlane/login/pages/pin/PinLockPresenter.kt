package com.dashlane.login.pages.pin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dashlane.R
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.LoginLockBasePresenter
import com.dashlane.login.root.LoginPresenter
import com.dashlane.ui.screens.settings.WarningRememberMasterPasswordDialog
import com.dashlane.ui.widgets.PinCodeKeyboardView
import com.dashlane.util.Toaster
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PinLockPresenter(
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    lockManager: LockManager,
    sso: Boolean,
    toaster: Toaster,
    private val warningRememberMasterPasswordDialog: WarningRememberMasterPasswordDialog,
) : LoginLockBasePresenter<PinLockContract.DataProvider, PinLockContract.ViewProxy>(
    rootPresenter = rootPresenter,
    coroutineScope = coroutineScope,
    lockManager = lockManager,
    toaster = toaster
),
    PinLockContract.Presenter {

    override val lockTypeName: Int = LockTypeManager.LOCK_TYPE_PIN_CODE

    private var endAnimationExpectedTimestamp: Long = 0

    private val logOutResId =
        if (sso) R.string.sso_lock_use_sso else R.string.fragment_lock_pin_button_use_master_password

    override fun onStart() {
        super.onStart()
        
        if (lockManager.isLocked && provider.lockSetting.isPinSetter) {
            lockManager.showLockActivity(context!!)
        }
    }

    override fun onNextClicked() {
        
    }

    
    private val onLogoutClicked = View.OnClickListener {
        rootPresenter.onPrimaryFactorCancelOrLogout()
    }

    
    private val onUseMasterPasswordClicked = View.OnClickListener {
        rootPresenter.onBiometricNegativeClicked()
    }

    private val pinLockKeyboardListener: PinCodeKeyboardView.PinCodeKeyboardListener =
        object : PinCodeKeyboardView.PinCodeKeyboardListener {
            override fun onClickNumber(value: Int) {
                if (provider.appendToUserPin(value)) {
                    view.setPinsVisible(provider.userPin.length)
                }
            }

            override fun onClickEraseLastNumber() {
                provider.removeLastPinNumber()
                view.setPinsVisible(provider.userPin.length)
            }
        }

    override fun enableAllKeyboardButtons(enabled: Boolean) {
        view.enableAllKeyboardButtons(enabled)
    }

    override fun onRequestReenterPin() {
        toaster.show(
            context?.getString(R.string.please_re_enter_your_pin_code_to_confirm),
            Toast.LENGTH_LONG,
            Toaster.Position.TOP
        )
    }

    override fun newPinConfirmed(disableAnimationEffect: Boolean) {
        val animationDuration = view.animateSuccess(disableAnimationEffect)
        endAnimationExpectedTimestamp = System.currentTimeMillis() + animationDuration
        
        warningRememberMasterPasswordDialog.showIfNecessary(
            context = view.context,
            onMasterPasswordRememberedIfPossible = ::onNewPinConfirmedAndMasterPasswordStored,
            onRememberMasterPasswordDeclined = { activity?.finish() }
        )
    }

    private fun onNewPinConfirmedAndMasterPasswordStored() {
        provider.savePinValue()

        val timeLeftBeforeEndAnimation = max(0, endAnimationExpectedTimestamp - System.currentTimeMillis())

        launch {
            if (timeLeftBeforeEndAnimation > 0) {
                delay(timeLeftBeforeEndAnimation)
            }
            finishSuccessfully()
        }
    }

    override fun onUnlockSuccess() {
        val animationDuration = view.animateSuccess(false)
        launch {
            if (animationDuration > 0) {
                delay(animationDuration.toLong())
            }
            finishSuccessfully()
        }
    }

    private fun finishSuccessfully() {
        val result = provider.onUnlockSuccess()
        val intent = provider.createNextActivityIntent()
        if (intent != null) {
            activity?.startActivity(intent)
        }
        activity?.setResult(Activity.RESULT_OK, result)
        activity?.finish()
    }

    override fun onUnlockError() {
        setFailedAttempts()
        clearInput()
        animateError()
    }

    override fun animateError() {
        view.animateError()
    }

    override fun clearInput() {
        enableAllKeyboardButtons(true)
        provider.userPin.delete(0, provider.userPin.length)
        view.setPinsVisible(provider.userPin.length)
    }

    override fun initView() {
        super.initView()

        providerOrNull ?: return

        loadSavedState()

        viewOrNull ?: return

        if (provider.lockSetting.shouldThemeAsDialog) {
            setupDialogWindowSize()
        }

        view.apply {
            setupKeyboard(pinLockKeyboardListener)
            setPinsVisible(provider.userPin.length)

            val (logoutResId, listener) = if (provider.lockSetting.isLockCancelable) {
                R.string.cancel to onLogoutClicked
            } else {
                logOutResId to onUseMasterPasswordClicked
            }

            if (provider.canUseMasterPassword()) {
                initLogoutButton(context.getString(logoutResId), listener)
            } else {
                hideLogoutButton()
            }
        }

        initTopicAndQuestion()
        setFailedAttempts()
        checkPinCodeComplete()
    }

    private fun setupDialogWindowSize() {
        val width = view.context.resources?.getDimension(R.dimen.activity_lock_as_dialog_pin_width)?.toInt() ?: 0
        val height = view.context.resources?.getDimension(R.dimen.activity_lock_as_dialog_pin_height)?.toInt() ?: 0
        activity?.window?.setLayout(width, height)
    }

    private fun loadSavedState() {
        provider.apply {
            currentStep = savedInstanceState?.getInt(SAVED_STATE_CURRENT_STEP, 0) ?: 0
            userPin.clear().append(savedInstanceState?.getString(SAVED_STATE_CURRENT_PIN) ?: "")
            if (lockSetting.isPinSetter) {
                firstStepPin = savedInstanceState?.getString(SAVED_STATE_EXPECTED_PIN)
            }
        }
    }

    private fun initTopicAndQuestion() {
        if (provider.lockSetting.isPinSetter) {
            view.setTopic(view.context.getString(R.string.pin_dialog_set_topic))
            view.setQuestion(view.context.getString(R.string.pin_dialog_set_question))
        } else {
            view.setTopic(view.context.getString(R.string.enter_pin))
            view.setQuestion(provider.username)
        }
    }

    private fun setFailedAttempts() {
        val failedAttempts = lockManager.getFailUnlockAttemptCount()
        if (failedAttempts > 0) {
            view.setTextError(
                resources!!.getQuantityString(
                    R.plurals.failed_attempt,
                    failedAttempts,
                    failedAttempts
                )
            )
        }
    }

    private fun checkPinCodeComplete() {
        providerOrNull?.onUserPinUpdated(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_STATE_CURRENT_STEP, provider.currentStep)
        outState.putString(SAVED_STATE_EXPECTED_PIN, provider.firstStepPin)
        outState.putString(SAVED_STATE_CURRENT_PIN, provider.userPin.toString())
    }

    companion object {
        private const val SAVED_STATE_CURRENT_STEP = "saved_state_current_step"
        private const val SAVED_STATE_EXPECTED_PIN = "saved_state_expected_pin"
        private const val SAVED_STATE_CURRENT_PIN = "saved_state_current_pin"
    }
}
