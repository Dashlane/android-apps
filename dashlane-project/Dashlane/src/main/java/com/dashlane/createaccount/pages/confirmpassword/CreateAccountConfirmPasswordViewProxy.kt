package com.dashlane.createaccount.pages.confirmpassword

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy

class CreateAccountConfirmPasswordViewProxy(rootView: View) :
    BaseViewProxy<CreateAccountConfirmPasswordContract.Presenter>(rootView),
    CreateAccountConfirmPasswordContract.ViewProxy {

    private val passwordLayout = findViewByIdEfficient<TextInputLayout>(R.id.view_confirm_account_password_layout)!!
    private val passwordView = findViewByIdEfficient<EditText>(R.id.view_confirm_account_password)!!

    override val passwordText: CharSequence
        get() = passwordView.text

    init {
        passwordView.addTextChangedListener {
            afterTextChanged {
                passwordLayout.error = null
            }
        }
        passwordView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }
        passwordLayout.addOnFieldVisibilityToggleListener { presenter.onPasswordVisibilityToggle(it) }
        
        DeveloperUtilities.preFillPassword(passwordView)
    }

    override fun showError(@StringRes errorResId: Int) {
        val error = context.getString(errorResId)
        passwordLayout.error = error
    }

    override fun setRecapPassword(password: String) {
        findViewByIdEfficient<EditText>(R.id.view_recap_password)?.setText(password)
    }
}