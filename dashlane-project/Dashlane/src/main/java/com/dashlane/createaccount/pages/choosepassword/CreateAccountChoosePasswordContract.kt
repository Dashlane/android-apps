package com.dashlane.createaccount.pages.choosepassword

import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.passwordstrength.PasswordStrength
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.Deferred

interface CreateAccountChoosePasswordContract {

    interface ViewProxy : Base.IView {

        val passwordText: CharSequence

        fun showPasswordStrength(strengthDeferred: Deferred<PasswordStrength?>?)

        fun showError(@StringRes errorResId: Int)

        fun setTitle(@StringRes resId: Int)
    }

    interface Presenter : CreateAccountBaseContract.Presenter {

        val rootView: ConstraintLayout

        fun onPasswordChanged(password: CharSequence)

        fun notifyPasswordInsufficient(strength: PasswordStrength?)

        fun notifyPasswordEmpty(strength: PasswordStrength?)

        fun notifySuccess(username: String, password: ObfuscatedByteArray)

        fun onShowTipsClicked()
    }

    interface DataProvider : CreateAccountBaseContract.DataProvider {

        suspend fun validatePassword(password: CharSequence)

        fun getPasswordStrengthAsync(password: CharSequence): Deferred<PasswordStrength?>
    }
}