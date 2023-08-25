package com.dashlane.login.pages.token

import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBasePresenter
import com.dashlane.login.root.LoginPresenter
import com.dashlane.navigation.NavigationConstants
import com.dashlane.navigation.NavigationHelper
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.getParcelableExtraCompat
import kotlin.properties.Delegates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginTokenPresenter(
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    private val loginLogger: LoginLogger
) :
    LoginBasePresenter<LoginTokenContract.DataProvider, LoginTokenContract.ViewProxy>(
        rootPresenter,
        coroutineScope
    ),
    LoginTokenContract.Presenter {

    override fun onWhereIsClicked() {
        popupShown = true
    }

    private var job: Job? = null

    private var popupShown: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        context?.apply {
            if (!oldValue && newValue) {
                DialogHelper().builder(this)
                    .setTitle(R.string.login_token_where_is_popup_title)
                    .setMessage(R.string.login_token_where_is_popup_message)
                    .setPositiveButton(R.string.login_token_where_is_popup_resend) { _, _ ->
                        loginLogger.logResendToken()
                        launch {
                            provider.resendToken()
                        }
                    }
                    .setNegativeButton(R.string.close) { _, _ -> }
                    .setOnDismissListener {
                        popupShown = false
                    }
                    .create()
                    .show()
            }
        }
    }

    override fun onViewOrProviderChanged() {
        super.onViewOrProviderChanged()
        val provider = providerOrNull ?: return
        val view = viewOrNull ?: return
        view.initDebug(provider.username)
    }

    override fun onCodeCompleted() {
        if (visible) {
            validateToken(true)
        }
    }

    private fun notifyTokenError(lockedOut: Boolean) {
        loginLogger.logWrongOtp(VerificationMode.EMAIL_TOKEN)
        if (lockedOut) {
            view.showError(R.string.token_failed_resend_or_try_later)
        } else {
            view.showError(R.string.token_failed_please_check_and_try_again)
        }
    }

    private fun notifyTokenValid(registeredUserDevice: RegisteredUserDevice, authTicket: String?) {
        rootPresenter.showPrimaryFactorStep(registeredUserDevice, authTicket)
    }

    override fun onNextClicked() {
        validateToken(false)
    }

    private fun validateToken(auto: Boolean) {
        job?.cancel()
        job = launch {
            rootPresenter.showProgress = true
            val (registeredUserDevice, authTicket) = try {
                provider.validateToken(view.tokenText, auto)
            } catch (e: CancellationException) {
                return@launch
            } catch (t: Throwable) {
                notifyError(t)
                return@launch
            } finally {
                rootPresenter.showProgress = false
            }
            notifyTokenValid(registeredUserDevice, authTicket)
        }
    }

    private fun notifyError(t: Throwable) {
        when (t) {
            is LoginBaseContract.OfflineException -> notifyOffline()
            is LoginBaseContract.NetworkException -> notifyNetworkError()
            is LoginTokenContract.InvalidTokenException -> notifyTokenError(t.lockedOut)
        }
    }

    override fun initView() {
        super.initView()
        viewOrNull?.takeIf { isCreated }?.let {
            popupShown = savedInstanceState?.getBoolean(STATE_TOKEN_POPUP) ?: false
        }
    }

    override fun onStart() {
        checkTokenDeepLink()
    }

    override fun onNewIntent() {
        checkTokenDeepLink()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_TOKEN_POPUP, popupShown)
    }

    private fun checkTokenDeepLink() {
        val activity = this.activity ?: return
        val view = this.viewOrNull ?: return
        val intentOrigin =
            activity.intent.getParcelableExtraCompat<Intent>(NavigationConstants.STARTED_WITH_INTENT)
                ?: return
        val callingUri = intentOrigin.data ?: return
        if (NavigationHelper.Destination.MainPath.LOGIN != callingUri.lastPathSegment) return
        val tokenStr = callingUri.getQueryParameter("token") ?: return

        val token = try {
            
            Integer.parseInt(tokenStr)
        } catch (ex: NumberFormatException) {
            
            return
        }

        
        activity.intent.removeExtra(NavigationConstants.STARTED_WITH_INTENT)
        view.tokenText = token.toString()
    }

    companion object {
        const val STATE_TOKEN_POPUP = "login_token_popup"
    }
}
