package com.dashlane.login.pages.totp

import androidx.appcompat.app.AlertDialog
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBasePresenter
import com.dashlane.login.root.LoginPresenter
import com.dashlane.ui.util.DialogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginTotpPresenter(
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope
) : LoginBasePresenter<LoginTotpContract.DataProvider, LoginTotpContract.ViewProxy>(
    rootPresenter,
    coroutineScope
),
    LoginTotpContract.Presenter {

    override var showProgress: Boolean
        get() = view.showProgress
        set(value) {
            view.showProgress = value
        }

    override fun onDuoClicked() {
        if (provider.secondFactor.isDuoPushEnabled) {
            useDuoAuthentication()
        }
    }

    override fun onAuthenticatorClicked() {
        if (provider.secondFactor.isAuthenticatorEnabled) {
            rootPresenter.showDashlaneAuthenticatorPage(
                secondFactor = provider.secondFactor,
                replacePage = true
            )
        }
    }

    override fun initView() {
        super.initView()
        viewOrNull?.run {
            val secondFactor = provider.secondFactor
            showAuthenticatorAvailable = secondFactor.isAuthenticatorEnabled
            showDuoAvailable = secondFactor.isDuoPushEnabled
            showU2fAvailable = secondFactor.isU2fEnabled
        }
        useU2fAuthenticationIfAvailable()
    }

    override fun onCodeCompleted() {
        if (visible) {
            validateTotp(true)
        }
    }

    override fun onNextClicked() {
        validateTotp(false)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun validateTotp(auto: Boolean) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            rootPresenter.showProgress = true
            try {
                provider.validateTotp(view.totpText, auto)
            } catch (e: LoginBaseContract.OfflineException) {
                notifyOffline()
            } finally {
                rootPresenter.showProgress = false
            }
        }
    }

    override fun notifyUnknownError() {
        rootPresenter.showProgress = false
        view.showError(R.string.error)
    }

    override fun notifyNetworkError() {
        super.notifyNetworkError()
        useU2fAuthenticationIfAvailable()
    }

    override fun notifyTotpError(lockedOut: Boolean) {
        rootPresenter.showProgress = false
        if (lockedOut) {
            view.showError(R.string.totp_failed_locked_out)
        } else {
            view.showError(R.string.totp_failed)
        }
        useU2fAuthenticationIfAvailable()
    }

    

    private fun notifyDuoAuthenticationDenied() {
        rootPresenter.showProgress = false
        view.showError(R.string.request_duo_challenge_declined)
    }

    

    private fun notifyDuoTimeoutError() {
        rootPresenter.showProgress = false
        view.showError(R.string.request_duo_challenge_timed_out)
    }

    override fun onTotpSuccess(registeredUserDevice: RegisteredUserDevice, authTicket: String?) {
        rootPresenter.run {
            showProgress = false
            migrationToSsoMemberInfo = migrationToSsoMemberInfo?.copy(totpAuthTicket = authTicket)
        }
        coroutineScope.launch(Dispatchers.Main) {
            rootPresenter.onTotpSuccess(registeredUserDevice)
        }
    }

    override fun notifyU2fKeyDetected() {
        viewOrNull?.showU2fKeyDetected()
    }

    override fun notifyU2fKeyMatched() {
        viewOrNull?.showU2fKeyMatched()
    }

    override fun notifyU2fKeyNeedsUserPresence() {
        viewOrNull?.showU2fKeyNeedsUserPresence()
    }

    override fun notifyU2fKeyMatchFailError() {
        rootPresenter.showProgress = false
        viewOrNull?.showError(R.string.request_u2f_verification_fail)
        useU2fAuthenticationIfAvailable()
    }

    override fun onU2fPopupOpened() = provider.u2fPopupOpened()

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun useDuoAuthentication() {
        launch(start = CoroutineStart.UNDISPATCHED) {
            val dialog = showDuoDialog()
            try {
                provider.executeDuoAuthentication()
            } catch (e: LoginTotpContract.DuoTimeoutException) {
                notifyDuoTimeoutError()
            } catch (e: LoginBaseContract.NetworkException) {
                notifyNetworkError()
            } catch (e: LoginBaseContract.OfflineException) {
                notifyOffline()
            } catch (e: LoginTotpContract.DuoDeniedException) {
                notifyDuoAuthenticationDenied()
            } finally {
                dialog.dismiss()
            }
        }
    }

    private fun useU2fAuthenticationIfAvailable() {
        launch {
            if (provider.secondFactor.isU2fEnabled) provider.executeU2fAuthentication(this)
        }
    }

    private fun showDuoDialog(): AlertDialog {
        return DialogHelper().builder(context!!)
            .setTitle(R.string.request_duo_challenge_title)
            .setMessage(R.string.request_duo_challenge_prompt)
            .setNegativeButton(R.string.cancel) { _, _ ->
                provider.duoPopupClosed()
            }
            .setCancelable(false)
            .create().apply {
                setOnShowListener { provider.duoPopupOpened() }
                show()
            }
    }
}