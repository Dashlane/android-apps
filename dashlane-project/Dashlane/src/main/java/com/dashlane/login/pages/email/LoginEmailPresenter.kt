package com.dashlane.login.pages.email

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.createaccount.CreateAccountActivity
import com.dashlane.login.InstallationIdDebugUtil
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBasePresenter
import com.dashlane.login.root.LoginPresenter
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.login.sso.LoginSsoActivity
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.login.sso.errorResId
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.startActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay

class LoginEmailPresenter(
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    private val endOfLife: EndOfLife,
    private val contactSsoAdministratorDialogFactory: ContactSsoAdministratorDialogFactory
) : LoginBasePresenter<LoginEmailContract.DataProvider, LoginEmailContract.ViewProxy>(rootPresenter, coroutineScope),
    LoginEmailContract.Presenter {

    override fun onCreateAccountClicked(skipEmailIfPrefilled: Boolean) {
        context?.let { context ->
            context.startActivity<CreateAccountActivity> {
                putExtra(CreateAccountActivity.EXTRA_PRE_FILLED_EMAIL, view.emailText)
                putExtra(CreateAccountActivity.EXTRA_SKIP_EMAIL_IF_PRE_FILLED, skipEmailIfPrefilled)
            }
        }
    }

    
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val loginActor = actor<suspend () -> Unit>(start = CoroutineStart.UNDISPATCHED) {
        for (command in channel) command()
    }

    override fun onViewOrProviderChanged() {
        super.onViewOrProviderChanged()
        initViewWithProvider()
    }

    override fun onAutoFill() = provider.onAutoFill()

    override fun showOtpPage(secondFactor: AuthenticationSecondFactor.Totp) {
        rootPresenter.showProgress = false
        rootPresenter.showTotpPage(secondFactor)
    }

    override fun notifyUnknownError() {
        rootPresenter.showProgress = false
        view.showError(R.string.login_button_error)
    }

    override fun notifyTeamError() {
        rootPresenter.showProgress = false
        view.showError(R.string.login_team_error)
    }

    override fun notifyEmailError() {
        rootPresenter.showProgress = false
        view.showError(R.string.account_doesn_t_exist_do_you_have_an_account_with_dashlane)
    }

    override fun showTokenPage(secondFactor: AuthenticationSecondFactor.EmailToken) {
        rootPresenter.showProgress = false
        rootPresenter.showTokenPage(secondFactor)
    }

    override fun showAuthenticatorPage(secondFactor: AuthenticationSecondFactor.EmailToken) {
        rootPresenter.showProgress = false
        rootPresenter.showDashlaneAuthenticatorPage(secondFactor)
    }

    override fun showSecretTransferQRPage(email: String?, startDestination: String) {
        rootPresenter.showProgress = false
        rootPresenter.showSecretTransferQRPage(email, startDestination)
    }

    override fun onNextClicked() {
        val email = view.emailText
        val trimmedEmail = email.trim()
        if (email != trimmedEmail) {
            view.emailText = trimmedEmail
        }

        if ("diagnostic" == email || "debug" == email) {
            view.emailText = ""
            askUserForConfirmation()
            return
        }

        if (InstallationIdDebugUtil.showInstallationId(context, view::emailText, provider::getTrackingInstallationId)) {
            return
        }

        login(trimmedEmail)
    }

    override fun login(email: String, delay: Boolean) {
        view.showError(null)

        loginActor.trySend {
            if (delay) {
                delay(500)
            }
            rootPresenter.run {
                showProgress = true
                migrationToSsoMemberInfo = null
            }
            try {
                provider.executeLogin(email)
            } catch (e: LoginEmailContract.EmptyEmailException) {
                notifyInvalidEmail()
            } catch (e: LoginEmailContract.InvalidEmailException) {
                notifyInvalidEmail()
            } catch (e: LoginEmailContract.ContactSsoAdministratorException) {
                notifyContactSsoAdministrator()
            } catch (e: LoginBaseContract.OfflineException) {
                notifyOffline()
            } catch (e: LoginBaseContract.ExpiredVersionException) {
                notifyVersionExpiredError()
            } finally {
                rootPresenter.showProgress = false
            }
        }
    }

    override fun askUserForConfirmation() = view.showConfirmationDialog()

    override fun userSupportFileConfirmed() = provider.uploadUserSupportFile()

    override fun showUploadingDialog() = view.showUploadingDialog()

    override fun showUploadFinishedDialog(crashDeviceId: String, copy: (String) -> Unit) = view.showUploadFinishedDialog(crashDeviceId, copy)

    override fun showUploadFailedDialog() = view.showUploadFailedDialog()

    private fun notifyVersionExpiredError() {
        activity?.run {
            endOfLife.showExpiredVersionMessaging(this)
        }
    }

    private fun notifyInvalidEmail() {
        view.showError(R.string.invalid_email)
    }

    private fun notifyContactSsoAdministrator() {
        contactSsoAdministratorDialogFactory.show(
            onDismiss = { view.emailText = "" }
        )
    }

    override fun showPasswordStep(registeredUserDevice: RegisteredUserDevice) {
        rootPresenter.showProgress = false
        rootPresenter.showPrimaryFactorStep(registeredUserDevice = registeredUserDevice, authTicket = null)
    }

    override fun startLoginSso(intent: Intent) {
        activity?.startActivityForResult(
            intent,
            REQUEST_CODE_SSO_LOGIN
        )
    }

    private fun initViewWithProvider() {
        val provider = providerOrNull ?: return
        val view = viewOrNull ?: return
        view.setSuggestion(provider.loginHistory)
        view.exposeTrackingInstallationId(provider.getTrackingInstallationId())
    }

    override fun skipIfPrefilled() {
        val email = provider.preferredLogin.takeIf { provider.preFillLogin }
        if (email != null) {
            login(email, true)
        }
    }

    override fun setMigrationToSsoMember(migrationToSsoMemberInfo: MigrationToSsoMemberInfo) {
        rootPresenter.migrationToSsoMemberInfo = migrationToSsoMemberInfo
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_EMAIL, email)
    }

    companion object {
        private const val REQUEST_CODE_SSO_LOGIN = 24_365
    }
}
