package com.dashlane.createaccount.pages.confirmpassword

import com.dashlane.R
import com.dashlane.createaccount.CreateAccountPresenter
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.createaccount.pages.CreateAccountBasePresenter
import com.dashlane.createaccount.pages.confirmpassword.CreateAccountConfirmPasswordContract.PasswordSuccess
import com.dashlane.util.coroutines.DeferredViewModel
import com.dashlane.util.hideSoftKeyboard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateAccountConfirmPasswordPresenter(
    val presenter: CreateAccountPresenter,
    private val passwordSuccessHolder: DeferredViewModel<PasswordSuccess>
) : CreateAccountBasePresenter<CreateAccountConfirmPasswordContract.DataProvider, CreateAccountConfirmPasswordContract.ViewProxy>(),
    CreateAccountConfirmPasswordContract.Presenter {

    override val nextEnabled = true

    override fun onPasswordVisibilityToggle(passwordShown: Boolean) = provider.passwordVisibilityToggled(passwordShown)

    override fun onShow() {
        super.onShow()
        view.setRecapPassword(provider.clearPassword)
        passwordSuccessHolder.deferred?.let {
            createAccount(it)
        }
    }

    override fun onNextClicked() {
        if (presenter.showProgress) {
            return 
        }

        val deferredValidatePassword = passwordSuccessHolder.async {
            provider.validatePassword(view.passwordText)
        }

        createAccount(deferredValidatePassword)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun createAccount(validatePassword: Deferred<PasswordSuccess>) {
        coroutineScope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
            presenter.showProgress = true

            try {
                validatePassword.await()
            } catch (e: CancellationException) {
                
                throw e
            } catch (t: Throwable) {
                handlePasswordValidationException(t)
                return@launch
            } finally {
                presenter.showProgress = false
            }

            if (provider.biometricAvailable) {
                showSettings()
            } else {
                presenter.showTos()
            }

            passwordSuccessHolder.deferred = null
        }
    }

    private fun handlePasswordValidationException(t: Throwable) {
        when (t) {
            is CreateAccountConfirmPasswordContract.PasswordMismatchException ->
                notifyPasswordMatchError()
            is CreateAccountBaseContract.NetworkException ->
                notifyNetworkError()
            else ->
                notifyUnknownError()
        }
    }

    

    private fun notifyPasswordMatchError() {
        view.showError(R.string.passwords_do_not_match)
    }

    

    private fun notifyNetworkError() {
        view.showError(R.string.network_error)
    }

    

    private fun notifyUnknownError() {
        view.showError(R.string.error)
    }

    

    private fun showSettings() {
        activity?.hideSoftKeyboard()
        presenter.showSettingsPage()
    }
}