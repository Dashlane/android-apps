package com.dashlane.login.pages.sso

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.dashlane.R
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.GetUserSsoInfoActivity
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.LoginLockBasePresenter
import com.dashlane.login.pages.LoginSwitchAccountUtil
import com.dashlane.login.root.LoginPresenter
import com.dashlane.login.sso.LoginSsoActivity.Companion.KEY_RESULT
import com.dashlane.util.Toaster
import com.dashlane.util.dpToPx
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.getWindowSizeWithoutStatusBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor

private const val SSO_EXCEPTION_MESSAGE = "SSO session exception"

class SsoLockPresenter(
    private val loginLogger: LoginLogger,
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    lockManager: LockManager,
    toaster: Toaster
) : LoginLockBasePresenter<SsoLockContract.DataProvider, SsoLockContract.ViewProxy>(
    rootPresenter = rootPresenter,
    coroutineScope = coroutineScope,
    lockManager = lockManager,
    toaster = toaster
),
    SsoLockContract.Presenter {
    override val lockTypeName: Int
        get() = LockTypeManager.LOCK_TYPE_UNSPECIFIED

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actor = actor<suspend () -> Unit> {
        for (command in this) command()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        
        
        activity?.run {
            if (intent.flags.and(Intent.FLAG_ACTIVITY_NO_HISTORY) != 0) {
                intent.flags = intent.flags and Intent.FLAG_ACTIVITY_NO_HISTORY.inv()
                startActivity(intent)
                finish()
            }
        }
    }

    override fun initView() {
        super.initView()
        val activity = activity

        viewOrNull?.run {
            email = provider.username
            provider.lockSetting.run {
                if (shouldThemeAsDialog && activity != null) {
                    
                    val rect = activity.getWindowSizeWithoutStatusBar()
                    val desiredWindowWidth = (rect.width() - activity.dpToPx(32f))
                        .coerceAtMost(activity.dpToPx(360f))
                        .toInt()
                    activity.window.setLayout(
                        desiredWindowWidth,
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )
                }
                setCancelable(isLockCancelable)
                canSwitchAccount(LoginSwitchAccountUtil.canSwitch(unlockReason))
            }
            initSpinner(provider.loginHistory)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GET_SSO_USER_INFO) {
            handleGetSsoUserInfoResult(resultCode, data)
        }
    }

    override fun onNextClicked() {
        val activity = activity ?: return

        actor.trySend {
            rootPresenter.showProgress = true
            val ssoInfo = provider.getSsoInfo()
            try {
                activity.startActivityForResult(
                    GetUserSsoInfoActivity.createStartIntent(
                        activity,
                        provider.username,
                        ssoInfo.serviceProviderUrl,
                        ssoInfo.isNitroProvider
                    ),
                    REQUEST_CODE_GET_SSO_USER_INFO
                )
            } catch (e: SsoLockContract.NoSessionLoadedException) {
                notifyUnknownError()
            } catch (_: AuthenticationOfflineException) {
                notifyOffline()
            } catch (_: AuthenticationNetworkException) {
                notifyNetworkError()
            } catch (e: AuthenticationUnknownException) {
                notifyUnknownError()
            }
        }
    }

    override fun onCancelClicked() {
        rootPresenter.onPrimaryFactorCancelOrLogout()
    }

    override fun onClickChangeAccount(email: String?) {
        val activity = activity ?: return
        actor.trySend { activity.startActivity(provider.changeAccount(email)) }
    }

    private fun handleGetSsoUserInfoResult(resultCode: Int, data: Intent?) {
        showProgress = false

        if (resultCode != Activity.RESULT_OK) return

        val userSsoInfo =
            data?.getParcelableExtraCompat<GetSsoInfoResult>(KEY_RESULT)
                ?.let { it as? GetSsoInfoResult.Success }
                ?.userSsoInfo

        if (userSsoInfo == null) {
            notifyUnknownError()
            return
        }

        actor.trySend {
            rootPresenter.showProgress = true

            try {
                provider.unlock(userSsoInfo)
                notifySuccess()
            } catch (_: AuthenticationInvalidSsoException) {
                notifyInvalidSsoError()
            } catch (e: SsoLockContract.NoSessionLoadedException) {
                notifyUnknownError()
            } catch (_: AuthenticationOfflineException) {
                notifyOffline()
            } catch (_: AuthenticationNetworkException) {
                notifyNetworkError()
            } catch (e: AuthenticationUnknownException) {
                notifyUnknownError()
            }
        }
    }

    private fun notifySuccess() {
        loginLogger.logSuccess(isFirstLogin = false, loginMode = LoginMode.Sso)
        activity?.run {
            val resultData = provider.onUnlockSuccess()
            provider.createNextActivityIntent()?.let(this::startActivity)
            setResult(Activity.RESULT_OK, resultData)
            finish()
        }
    }

    private fun notifyInvalidSsoError() {
        loginLogger.logInvalidSso()
        rootPresenter.showProgress = false
        view.showError(R.string.sso_error_not_correct)
    }

    private fun notifyUnknownError() {
        loginLogger.logErrorUnknown(loginMode = LoginMode.Sso)
        rootPresenter.showProgress = false
        view.showError(R.string.login_button_error)
    }

    companion object {
        private const val REQUEST_CODE_GET_SSO_USER_INFO = 45_387
    }
}
