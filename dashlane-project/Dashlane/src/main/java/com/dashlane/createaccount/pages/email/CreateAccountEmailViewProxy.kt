package com.dashlane.createaccount.pages.email

import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.dashlane.R
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy

class CreateAccountEmailViewProxy(
    view: View,
    initialEmail: String
) : BaseViewProxy<CreateAccountEmailContract.Presenter>(view),
    CreateAccountEmailContract.ViewProxy {

    private val emailLayout = findViewByIdEfficient<TextInputLayout>(R.id.view_create_account_email_layout)!!
    private val emailView = findViewByIdEfficient<EditText>(R.id.view_create_account_email)!!
    private val installationIdTextView = findViewByIdEfficient<TextView>(R.id.installation_id_debug)!!

    override var emailText: String
        get() = emailView.text.toString()
        set(value) {
            emailView.setText(value)
        }

    init {
        emailView.setText(initialEmail)
        emailView.addTextChangedListener { afterTextChanged { emailLayout.error = null } }
        emailView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }

        
        DeveloperUtilities.preFillUsername(emailView)
    }

    override fun showConfirmEmailPopup(
        email: String,
        inEuropeanUnion: Boolean,
        country: String?,
        loginSsoIntent: Intent?,
        callback: () -> Unit
    ) {
        DialogHelper().builder(context)
            .setTitle(email)
            .setMessage(R.string.create_account_confirm_unlikely_email)
            .setPositiveButton(R.string.confirm) { _, _ ->
                presenter.onConfirmEmail(email, inEuropeanUnion, country, loginSsoIntent)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> presenter.onCancelConfirmEmail() }
            .create()
            .apply { setOnShowListener { callback() } }
            .show()
    }

    override fun showError(errorResId: Int) {
        val error = context.getString(errorResId)
        emailLayout.error = error
    }

    override fun exposeTrackingInstallationId(id: String?) {
        installationIdTextView.text = id
    }
}
