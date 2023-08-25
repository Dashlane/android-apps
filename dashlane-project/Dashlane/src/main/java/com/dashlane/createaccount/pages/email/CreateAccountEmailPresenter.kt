package com.dashlane.createaccount.pages.email

import android.app.Activity
import android.content.Intent
import com.dashlane.R
import com.dashlane.createaccount.CreateAccountPresenter
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.createaccount.pages.CreateAccountBasePresenter
import com.dashlane.login.InstallationIdDebugUtil
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.login.sso.LoginSsoActivity
import com.dashlane.login.sso.errorResId
import com.dashlane.util.getParcelableExtraCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor

class CreateAccountEmailPresenter(
    private val presenter: CreateAccountPresenter,
    private val contactSsoAdministratorDialogFactory: ContactSsoAdministratorDialogFactory
) : CreateAccountBasePresenter<CreateAccountEmailContract.DataProvider, CreateAccountEmailContract.ViewProxy>(),
    CreateAccountEmailContract.Presenter {

    override val nextEnabled: Boolean = true

    
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val validateEmailActor =
        coroutineScope.actor<String>(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
            for (email in channel) {
                presenter.showProgress = true
                val pendingAccount = try {
                    provider.validateEmail(email)
                } catch (e: CancellationException) {
                    
                    throw e
                } catch (e: CreateAccountEmailContract.EmptyEmailException) {
                    notifyEmailEmpty()
                    continue
                } catch (e: CreateAccountEmailContract.InvalidEmailException) {
                    notifyEmailInvalid()
                    continue
                } catch (e: CreateAccountBaseContract.NetworkException) {
                    notifyNetworkError()
                    continue
                } catch (e: CreateAccountEmailContract.AccountAlreadyExistsException) {
                    notifyEmailAccountAlreadyExists()
                    continue
                } catch (e: CreateAccountEmailContract.ContactSsoAdministratorException) {
                    notifyContactSsoAdministrator()
                    continue
                } catch (e: CreateAccountEmailContract.ExpiredVersionException) {
                    notifyVersionExpiredError()
                    continue
                } finally {
                    presenter.showProgress = false
                }

                if (pendingAccount.emailLikelyInvalid) {
                    confirmUnlikelyEmail(
                        pendingAccount.email,
                        pendingAccount.inEuropeanUnion,
                        pendingAccount.country,
                        pendingAccount.loginSsoIntent
                    )
                } else {
                    notifySuccess(
                        pendingAccount.email,
                        pendingAccount.inEuropeanUnion,
                        pendingAccount.country,
                        pendingAccount.loginSsoIntent
                    )
                }
            }
        }

    private fun notifyEmailEmpty() {
        view.showError(R.string.create_account_error_email_empty_description)
    }

    private fun notifyEmailInvalid() {
        view.showError(R.string.create_account_error_email_description)
    }

    private fun notifyNetworkError() {
        view.showError(R.string.unable_to_verify_username_with_dashlane_at_this_time_please_try_again_later)
    }

    private fun notifyEmailAccountAlreadyExists() {
        view.showError(R.string.username_exists_already)
    }

    private fun notifyContactSsoAdministrator() {
        contactSsoAdministratorDialogFactory.show(
            onDismiss = { view.emailText = "" }
        )
    }

    private fun notifyVersionExpiredError() {
        view.showError(R.string.expired_version_noupdate_title)
    }

    private fun confirmUnlikelyEmail(
        email: String,
        inEuropeanUnion: Boolean,
        country: String?,
        loginSsoIntent: Intent?
    ) {
        view.showConfirmEmailPopup(
            email,
            inEuropeanUnion,
            country,
            loginSsoIntent
        )
    }

    private fun notifySuccess(
        username: String,
        inEuropeanUnion: Boolean,
        country: String?,
        loginSsoIntent: Intent?
    ) {
        if (loginSsoIntent != null) {
            activity?.startActivityForResult(
                loginSsoIntent,
                REQUEST_CODE_SSO_LOGIN
            )
        } else {
            presenter.showChoosePasswordPage(username, inEuropeanUnion, country)
        }
    }

    override fun onNextClicked() {
        if (InstallationIdDebugUtil.showInstallationId(context, view::emailText, provider::getTrackingInstallationId)) {
            return
        }
        validateEmailActor.trySend(view.emailText)
    }

    override fun onShow() {
        super.onShow()
        view.exposeTrackingInstallationId(provider.getTrackingInstallationId())
    }

    override fun onConfirmEmail(
        email: String,
        inEuropeanUnion: Boolean,
        country: String?,
        loginSsoIntent: Intent?
    ) {
        notifySuccess(email, inEuropeanUnion, country, loginSsoIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CODE_SSO_LOGIN || resultCode == Activity.RESULT_CANCELED) return

        val result = data?.getParcelableExtraCompat<LoginSsoActivity.Result>(LoginSsoActivity.KEY_RESULT)
            ?: return

        when (result) {
            LoginSsoActivity.Result.Success -> activity?.finish()
            is LoginSsoActivity.Result.Error -> viewOrNull?.showError(result.errorResId)
        }
    }

    companion object {
        private const val REQUEST_CODE_SSO_LOGIN = 24_365
    }
}