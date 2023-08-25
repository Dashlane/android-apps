package com.dashlane.autofill.api.changepassword

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.common.AutofillGeneratePasswordLogger
import com.dashlane.autofill.api.common.GeneratePasswordViewProxy
import com.dashlane.autofill.api.navigation.AutofillBottomSheetNavigator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.util.Toaster
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.isNotSemanticallyNull
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordViewProxy(
    dialogView: View,
    private val defaultUsername: String?,
    private val toaster: Toaster,
    defaultCriteria: PasswordGeneratorCriteria,
    autofillBottomSheetNavigator: AutofillBottomSheetNavigator,
    generateLogger: AutofillGeneratePasswordLogger
) : ChangePasswordContract.ViewProxy,
    GeneratePasswordViewProxy<ChangePasswordPresenter>(dialogView, defaultCriteria, generateLogger) {

    private var login: Spinner? = null
    private var password: TextInputEditText? = null
    private var useButton: Button? = null
    private var backArrow: ImageButton? = null
    private var dashLogo: ImageView? = null

    private val passwordTextWatcher = object : ColorTextWatcher(context) {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            super.onTextChanged(charSequence, start, before, count)
            useButton?.isEnabled = checkMandatoryFields()
        }
    }

    init {
        login = dialogView.findViewById(R.id.login)
        password = dialogView.findViewById(R.id.password)
        useButton = dialogView.findViewById(R.id.save_button)
        backArrow = dialogView.findViewById(R.id.back_arrow)
        dashLogo = dialogView.findViewById(R.id.dashlogo)
        password?.addTextChangedListener(passwordTextWatcher)
        useButton?.setOnClickListener {
            presenter.useButtonClicked()
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
        FilledData(login?.selectedItem.toString(), password?.text?.toString())

    override fun prefillLogin(logins: List<String>) {
        login?.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, logins).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        defaultUsername?.let {
            val index = logins.indexOfFirst { it.equals(defaultUsername, ignoreCase = true) }
            if (index > -1) login?.setSelection(index)
        }
        if (presenter.lastGeneratedPassword == null) generatorConfiguration?.let { presenter.generatePassword(it.getConfiguration()) }
    }

    override fun enableUse(enable: Boolean) {
        useButton?.isEnabled = enable
    }

    override fun displayError(message: String) {
        toaster.show(message, Toast.LENGTH_SHORT)
    }

    private fun checkMandatoryFields() =
        login?.selectedItem.toString().isNotSemanticallyNull() && password?.text.isNotSemanticallyNull()
}

data class FilledData(val login: String, val password: String?) {
    val isComplete
        get() = login.isNotSemanticallyNull() && password.isNotSemanticallyNull()
}