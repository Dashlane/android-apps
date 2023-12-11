package com.dashlane.login.pages.password

import android.app.Activity
import android.content.Intent
import android.text.style.UnderlineSpan
import android.widget.Toast
import com.dashlane.R
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.login.LoginActivity
import com.dashlane.login.LoginIntents
import com.dashlane.login.LoginLogger
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyActivity
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginLockBasePresenter
import com.dashlane.login.pages.LoginSwitchAccountUtil
import com.dashlane.login.pages.password.LoginPasswordContract.InvalidPasswordException.InvalidReason
import com.dashlane.login.root.LoginPresenter
import com.dashlane.util.Toaster
import com.dashlane.util.coroutines.DeferredViewModel
import com.dashlane.util.getFormattedSpannable
import com.dashlane.util.getWindowSizeWithoutStatusBar
import com.dashlane.util.hideSoftKeyboard
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.showToaster
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch

@Suppress("LargeClass")
class LoginPasswordPresenter(
    private val passwordValidationHolder: DeferredViewModel<LoginPasswordContract.SuccessfulLogin>,
    private val loginLogger: LoginLogger,
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    lockManager: LockManager,
    toaster: Toaster
) : LoginLockBasePresenter<LoginPasswordContract.DataProvider, LoginPasswordContract.ViewProxy>(
    rootPresenter = rootPresenter,
    coroutineScope = coroutineScope,
    lockManager = lockManager,
    toaster = toaster
),
    LoginPasswordContract.Presenter {

    private var unlockBiometricRecovery: CompletableDeferred<Unit>? = null

    override val lockTypeName: Int = LockTypeManager.LOCK_TYPE_MASTER_PASSWORD

    private sealed class Command {
        object Login : Command()
        object Help : Command()
        object ForgotPassword : Command()
        class ChangeAccount(val email: String? = null) : Command()
        object BiometricRecovery : Command()
        object RecoveryDialog : Command()
        object AccountRecoveryKey : Command()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actor = actor<Command> {
        
        passwordValidationHolder.deferred?.let { login(it) }

        for (command in channel) {
            
            passwordValidationHolder.deferred = null

            val done = when (command) {
                Command.Login -> login(view.passwordText.toString())
                Command.Help -> {
                    val intent = provider.loginHelp()
                    context?.safelyStartBrowserActivity(intent)
                    false
                }

                Command.ForgotPassword -> {
                    val intent = provider.passwordForgotten()
                    context?.safelyStartBrowserActivity(intent)
                    false
                }

                is Command.ChangeAccount -> {
                    startActivityChangeAccount(command.email)
                    false
                }

                Command.BiometricRecovery -> {
                    activity?.run {
                        
                        if (!provider.lockSetting.isLoggedIn) {
                            provider.loadStaleSession()
                        }
                        provider.onPromptBiometricForRecovery()
                        lockManager.showLockForBiometricRecovery(
                            this,
                            UNLOCK_FOR_BIOMETRIC_RECOVERY,
                            resources.getString(R.string.account_recovery_biometric_prompt_title),
                            resources.getString(
                                R.string.account_recovery_biometric_prompt_description,
                                provider.username
                            )
                        )
                    }
                    unlockBiometricRecovery = CompletableDeferred()
                    unlockBiometricRecovery?.await()
                    false
                }

                Command.RecoveryDialog -> {
                    view.showRecoveryDialog()
                    false
                }

                Command.AccountRecoveryKey -> {
                    val intent = provider.getAccountRecoveryKeyIntent()
                    intent?.let { activity?.startActivityForResult(it, RESULT_FOR_ACCOUNT_RECOVERY_KEY) }
                    false
                }
            }
            if (done) {
                break
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UNLOCK_FOR_BIOMETRIC_RECOVERY) {
            if (resultCode == Activity.RESULT_OK) {
                provider.getChangeMPIntent()?.let {
                    activity?.startActivity(it)
                }
            } else if (!provider.lockSetting.isLoggedIn) {
                
                provider.unloadSession()
            }
            unlockBiometricRecovery?.complete(Unit)
        } else if (requestCode == RESULT_FOR_ACCOUNT_RECOVERY_KEY) {
            if (resultCode == Activity.RESULT_OK) {
                coroutineScope.launch {
                    val password =
                        data?.getSerializableExtra(LoginAccountRecoveryKeyActivity.ACCOUNT_RECOVERY_PASSWORD_RESULT) as? ObfuscatedByteArray
                    password?.let { login(it.decodeUtf8ToString()) }
                }
            } else if (!provider.lockSetting.isLoggedIn) {
                
                provider.unloadSession()
            }
        }
    }

    override fun initView() {
        super.initView()
        viewOrNull?.apply {
            if (provider.lockSetting.shouldThemeAsDialog) {
                val width = context.resources?.getDimension(R.dimen.activity_lock_as_dialog_mp_width)?.toInt() ?: 0
                val height =
                    context.resources?.getDimension(R.dimen.activity_lock_as_dialog_mp_height)?.toInt() ?: 0

                activity?.apply {
                    val size = getWindowSizeWithoutStatusBar()

                    window.setLayout(
                        width.coerceAtMost(size.width()),
                        height.coerceAtMost(size.height())
                    )
                }
            }
            provider.lockSetting.apply {
                isDialog = shouldThemeAsDialog
                showUnlockLayout(isLoggedIn)
                setUnlockTopic(topicLock)

                if (LoginSwitchAccountUtil.canSwitch(unlockReason)) {
                    showSwitchAccount(provider.loginHistory)
                } else {
                    hideSwitchAccount()
                }

                val cancelText = when {
                    provider.lockSetting.allowBypass -> R.string.login_enter_mp_later
                    provider.lockSetting.isLockCancelable -> R.string.cancel
                    else -> null
                }
                setCancelBtnText(cancelText?.let { context.getString(it) })
            }

            if (activity.let { it != null && it.intent.getBooleanExtra(LoginActivity.SYNC_ERROR, false) }) {
                showError(R.string.login_sync_error)
            }
        }
    }

    override fun onCancelClicked() {
        when {
            provider.lockSetting.allowBypass -> {
                
                provider.askMasterPasswordLater()
                rootPresenter.showLockPage()
            }

            provider.lockSetting.isLoggedIn -> rootPresenter.onPrimaryFactorCancelOrLogout()
            else -> offerCommand(Command.ChangeAccount())
        }
    }

    override fun onNextClicked() {
        offerCommand(Command.Login)
    }

    private suspend fun login(password: String): Boolean {
        val leaveAfterSuccess = LoginIntents.shouldCloseLoginAfterSuccess(activity!!.intent)
        val deferred = passwordValidationHolder.async(Dispatchers.Default) {
            provider.validatePassword(password, leaveAfterSuccess)
        }
        return login(deferred)
    }

    private suspend fun login(deferred: Deferred<LoginPasswordContract.SuccessfulLogin>): Boolean {
        view.showError(null)
        if (!provider.lockSetting.isLoggedIn) {
            
            rootPresenter.showProgress = true
        }

        val successfulLogin = try {
            deferred.await()
        } catch (e: CancellationException) {
            throw e 
        } catch (t: Throwable) {
            rootPresenter.showProgress = false
            notifyError(t)
            return false
        }

        notifySuccess(successfulLogin.intent)
        return true
    }

    private fun notifyError(t: Throwable) {
        when (t) {
            is LoginPasswordContract.InvalidPasswordException -> {
                when (t.reason) {
                    InvalidReason.EMPTY -> notifyEmptyPassword()
                    InvalidReason.FAILED_UNLOCK -> notifyFailedUnlock()
                    InvalidReason.INVALID -> notifyPasswordError()
                }
            }

            is LoginPasswordContract.AccountResetException -> {
                activity?.showToaster(R.string.forced_logout, Toast.LENGTH_LONG)
                startActivityChangeAccount()
            }

            is LoginBaseContract.OfflineException -> notifyOffline()
            is LoginBaseContract.NetworkException -> notifyNetworkError()
            else -> notifyUnknownError()
        }
    }

    private fun notifyEmptyPassword() {
        view.showError(R.string.password_empty)
    }

    private fun notifyPasswordError() {
        coroutineScope.launch {
            val hasBiometricReset = provider.canMakeBiometricRecovery
            val hasAccountRecoveryKeyEnabled = provider.getAccountRecoveryKeyStatus().enabled

            when {
                hasBiometricReset && hasAccountRecoveryKeyEnabled -> offerCommand(Command.RecoveryDialog)
                hasBiometricReset -> {
                    val error = view.resources.getFormattedSpannable(
                        R.string.account_recovery_password_is_not_correct,
                        view.resources.getString(R.string.account_recovery_password_is_not_correct_underline),
                        listOf(UnderlineSpan())
                    )

                    view.showError(error) {
                        offerCommand(Command.BiometricRecovery)
                    }
                }

                hasAccountRecoveryKeyEnabled -> {
                    val error = view.resources.getFormattedSpannable(
                        R.string.account_recovery_password_is_not_correct,
                        view.resources.getString(R.string.account_recovery_password_is_not_correct_underline),
                        listOf(UnderlineSpan())
                    )

                    view.showError(error) {
                        offerCommand(Command.AccountRecoveryKey)
                    }
                }

                else -> {
                    val error = view.resources.getFormattedSpannable(
                        R.string.password_is_not_correct_please_try_again_with_login_issues,
                        view.resources.getString(R.string.password_is_not_correct_please_try_again_with_login_issues_underline),
                        listOf(UnderlineSpan())
                    )

                    view.showError(error) {
                        view.showPasswordHelp()
                    }
                }
            }
        }
    }

    private fun notifyFailedUnlock() {
        view.showError(R.string.password_is_not_correct_please_try_again)
        if (lockManager.hasFailedUnlockTooManyTimes()) {
            logoutTooManyAttempts(null)
        }
    }

    private fun notifyUnknownError() {
        rootPresenter.showProgress = false
        view.showError(R.string.error)
    }

    private fun notifySuccess(intent: Intent?) {
        lockManager.hasEnteredMP = true

        if (intent != null) {
            rootPresenter.showNextScreen(intent)
        }

        provider.onUnlockSuccess()

        val result = Intent().apply {
            putExtra(LockSetting.EXTRA_LOCK_REASON, provider.lockSetting.unlockReason)
        }
        activity?.apply {
            setResult(Activity.RESULT_OK, result)
            hideSoftKeyboard()
            finish()
        }
    }

    override fun onClickLoginHelpRequested() {
        offerCommand(Command.Help)
    }

    override fun onClickForgotButton() {
        startRecoveryOrSendToFAQ()
    }

    override fun onClickForgotPassword() {
        startRecoveryOrSendToFAQ()
    }

    override fun onClickChangeAccount(email: String?) {
        loginLogger.logUseAnotherAccount()
        offerCommand(Command.ChangeAccount(email))
    }

    override fun onClickBiometricRecovery() {
        offerCommand(Command.BiometricRecovery)
    }

    override fun onClickAccountRecoveryKey() {
        offerCommand(Command.AccountRecoveryKey)
    }

    private fun startRecoveryOrSendToFAQ() {
        coroutineScope.launch {
            val hasBiometricReset = provider.canMakeBiometricRecovery
            val hasAccountRecoveryKeyEnabled = provider.getAccountRecoveryKeyStatus().enabled
            loginLogger.logForgetMasterPassword(hasBiometricReset)

            when {
                hasBiometricReset && hasAccountRecoveryKeyEnabled -> offerCommand(Command.RecoveryDialog)
                hasBiometricReset -> offerCommand(Command.BiometricRecovery)
                hasAccountRecoveryKeyEnabled -> offerCommand(Command.AccountRecoveryKey)
                else -> offerCommand(Command.ForgotPassword)
            }
        }
    }

    private fun offerCommand(command: Command) {
        try {
            if (!actor.trySend(command).isSuccess) {
            }
        } catch (t: Throwable) {
        }
    }

    private fun startActivityChangeAccount(email: String? = null) {
        coroutineScope.launch(Dispatchers.Main) {
            val intent = provider.changeAccount(email)
            context?.startActivity(intent)
        }
    }

    companion object {
        const val UNLOCK_FOR_BIOMETRIC_RECOVERY = 4631
        const val RESULT_FOR_ACCOUNT_RECOVERY_KEY = 4632
    }
}
