package com.dashlane.login.pages.authenticator

import com.dashlane.R
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationTimeoutException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBasePresenter
import com.dashlane.login.root.LoginPresenter
import com.dashlane.util.coroutines.DeferredViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginDashlaneAuthenticatorPresenter(
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    private val authenticationHolder: DeferredViewModel<Pair<RegisteredUserDevice, String?>>
) : LoginBasePresenter<LoginDashlaneAuthenticatorContract.DataProvider, LoginDashlaneAuthenticatorContract.ViewProxy>(
    rootPresenter,
    coroutineScope
),
    LoginDashlaneAuthenticatorContract.Presenter {
    private var useEmailTokenJob: Job? = null
    private var authenticateJob: Job? = null

    override fun onNextClicked() = Unit

    override fun initView() {
        super.initView()

        if (!isViewAttached) return

        val deferred = authenticationHolder.deferred

        if (deferred == null) {
            authenticate()
        } else if (authenticateJob == null || authenticateJob?.isActive == false) {
            authenticateJob = await(deferred)
        }

        viewOrNull?.showTotpAvailable = provider.secondFactor is AuthenticationSecondFactor.Totp
    }

    override fun onUseAlternativeClicked() {
        useEmailTokenJob?.cancel()

        when (val secondFactor = provider.secondFactor) {
            is AuthenticationSecondFactor.Totp -> {
                rootPresenter.showTotpPage(secondFactor, replacePage = true)
                authenticationHolder.deferred?.cancel()
                authenticationHolder.deferred = null
            }

            is AuthenticationSecondFactor.EmailToken -> {
                useEmailTokenJob = launch {
                    try {
                        provider.sendEmailToken(secondFactor)
                    } catch (_: AuthenticationException) {
                        return@launch
                    }

                    rootPresenter.showTokenPage(secondFactor, replacePage = true)
                    authenticationHolder.deferred?.cancel()
                    authenticationHolder.deferred = null
                }
            }
        }
    }

    override fun onResendRequestClicked() {
        authenticate()
    }

    override fun onBackPressed(): Boolean {
        authenticationHolder.deferred?.cancel()
        authenticationHolder.deferred = null
        return super.onBackPressed()
    }

    private fun authenticate() {
        authenticationHolder.deferred?.cancel()

        val deferred = authenticationHolder.async {
            provider.executeAuthenticatorAuthentication()
        }

        authenticateJob = await(deferred)
    }

    private fun await(deferred: Deferred<Pair<RegisteredUserDevice, String?>>): Job {
        return launch {
            view.showLoading()
            val (registeredUserDevice, authTicket) = try {
                deferred.await()
            } catch (_: CancellationException) {
                return@launch
            } catch (_: AuthenticationTimeoutException) {
                view.showError(R.string.login_dashlane_authenticator_request_timed_out)
                return@launch
            } catch (_: AuthenticationExpiredVersionException) {
                view.showError(R.string.expired_version_noupdate_title)
                return@launch
            } catch (e: Exception) {
                view.showError(R.string.login_dashlane_authenticator_request_rejected)
                return@launch
            }

            view.showSuccess {
                rootPresenter.showPrimaryFactorStep(registeredUserDevice = registeredUserDevice, authTicket = authTicket)
                authenticationHolder.deferred = null
            }
        }
    }
}
