package com.dashlane.autofill.api.createaccount

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.common.AutofillGeneratePasswordLogger
import com.dashlane.autofill.api.common.GeneratePasswordViewProxy
import com.dashlane.autofill.api.navigation.AutofillBottomSheetNavigator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.util.TextInputAutoCompleteTextView
import com.dashlane.util.Toaster
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.isNotSemanticallyNull
import com.google.android.material.textfield.TextInputEditText

class CreateAccountViewProxy(
    dialogView: View,
    private val toaster: Toaster,
    emailSuggestions: List<String>,
    websitesSuggestions: List<String>,
    defaultConfiguration: PasswordGeneratorCriteria,
    autofillBottomSheetNavigator: AutofillBottomSheetNavigator,
    generateLogger: AutofillGeneratePasswordLogger
) : CreateAccountContract.ViewProxy,
    GeneratePasswordViewProxy<CreateAccountPresenter>(dialogView, defaultConfiguration, generateLogger) {

    private var website: TextInputAutoCompleteTextView? = null
    private var login: TextInputAutoCompleteTextView? = null
    private var password: TextInputEditText? = null
    private var saveButton: Button? = null
    private var backArrow: ImageButton? = null
    private var dashLogo: ImageView? = null

    private val passwordTextWatcher = object : ColorTextWatcher(context) {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            super.onTextChanged(charSequence, start, before, count)
            saveButton?.isEnabled = checkMandatoryFields()
        }
    }

    init {
        website = dialogView.findViewById(R.id.website)
        login = dialogView.findViewById(R.id.login)
        password = dialogView.findViewById(R.id.password)
        saveButton = dialogView.findViewById(R.id.save_button)
        backArrow = dialogView.findViewById(R.id.back_arrow)
        dashLogo = dialogView.findViewById(R.id.dashlogo)

        login?.addTextChangedListener {
            afterTextChanged {
                saveButton?.isEnabled = checkMandatoryFields()
            }
        }
        password?.addTextChangedListener(passwordTextWatcher)

        website?.setAdapter(
            ArrayAdapter<String>(
                context,
                R.layout.autocomplete_textview_adapter,
                websitesSuggestions
            )
        )
        login?.setAdapter(
            ArrayAdapter(
                context,
                R.layout.autocomplete_textview_adapter,
                emailSuggestions
            )
        )
        saveButton?.setOnClickListener {
            presenter.savedButtonClicked()
        }
        backArrow?.setOnClickListener { autofillBottomSheetNavigator.popStack() }
        if (autofillBottomSheetNavigator.hasVisiblePrevious()) {
            backArrow?.visibility = View.VISIBLE
            dashLogo?.visibility = View.GONE
        } else {
            backArrow?.visibility = View.GONE
            dashLogo?.visibility = View.VISIBLE
        }
    }

    override fun getFilledData() =
        FilledData(website?.text.toString(), login?.text.toString(), password?.text?.toString())

    override fun prefillWebsiteFieldAndFocusOnLogin(website: String) {
        this.website?.setText(website)
        login?.requestFocus()
    }

    override fun enableSave(enable: Boolean) {
        saveButton?.isEnabled = enable
    }

    override fun displayError(message: String) {
        toaster.show(message, Toast.LENGTH_SHORT)
    }

    private fun checkMandatoryFields() = login?.text.isNotSemanticallyNull() && password?.text.isNotSemanticallyNull()
}

data class FilledData(val website: String?, val login: String?, val password: String?) {
    val isComplete
        get() = login.isNotSemanticallyNull() && password.isNotSemanticallyNull()
}