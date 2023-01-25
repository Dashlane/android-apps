package com.dashlane.createaccount.pages.choosepassword

import android.content.Intent
import androidx.constraintlayout.widget.ConstraintLayout
import com.dashlane.R
import com.dashlane.createaccount.CreateAccountPresenter
import com.dashlane.createaccount.pages.CreateAccountBasePresenter
import com.dashlane.masterpassword.tips.MasterPasswordTipsActivity
import com.dashlane.util.clearTop
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.passwordstrength.PasswordStrength
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateAccountChoosePasswordPresenter(val presenter: CreateAccountPresenter) :
    CreateAccountBasePresenter<CreateAccountChoosePasswordContract.DataProvider, CreateAccountChoosePasswordContract.ViewProxy>(),
    CreateAccountChoosePasswordContract.Presenter {

    override fun onPasswordVisibilityToggle(passwordShown: Boolean) = provider.passwordVisibilityToggled(passwordShown)

    override val nextEnabled: Boolean = true

    override val rootView: ConstraintLayout
        get() = presenter.rootView

    override fun onVisibilityChanged(visible: Boolean) {
        if (!visible) {
            view.showPasswordStrength(null)
        } else {
            updateTitle()
        }
    }

    override fun onPasswordChanged(password: CharSequence) {
        if (visible) {
            view.showPasswordStrength(password.takeIf { it.isNotEmpty() }?.let { provider.getPasswordStrengthAsync(it) })
        }
    }

    override fun notifyPasswordInsufficient(strength: PasswordStrength?) {
        view.showPasswordStrength(CompletableDeferred(strength))
        view.showError(R.string.password_creation_not_strong_enough)
    }

    override fun notifyPasswordEmpty(strength: PasswordStrength?) {
        view.showPasswordStrength(CompletableDeferred(strength))
    }

    override fun notifySuccess(username: String, password: ObfuscatedByteArray) {
        view.showPasswordStrength(null)
        presenter.showConfirmPasswordPage(username, password)
    }

    override fun onNextClicked() {
        coroutineScope.launch(Dispatchers.Main) {
            provider.validatePassword(view.passwordText)
        }
    }

    override fun onShowTipsClicked() {
        provider.logger.logShowPasswordTips()
        val tipsIntent = Intent(context, MasterPasswordTipsActivity::class.java).clearTop()
        context?.startActivity(tipsIntent)
    }

    private fun updateTitle() {
        view.setTitle(R.string.create_account_choose_password_title)
    }
}