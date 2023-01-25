package com.dashlane.login.pages.email

import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.dashlane.R
import com.dashlane.login.pages.LoginBaseSubViewProxy
import com.dashlane.util.addTextChangedListener
import kotlin.properties.Delegates

class LoginEmailViewProxy(view: View) : LoginBaseSubViewProxy<LoginEmailContract.Presenter>(view),
    LoginEmailContract.ViewProxy {

    private val emailAutoCompleteTextView = findViewByIdEfficient<AutoCompleteTextView>(R.id.view_login_email)!!
    private val createAccountButton = findViewByIdEfficient<AppCompatButton>(R.id.btn_create_account)!!
    private val installationIdTextView = findViewByIdEfficient<TextView>(R.id.installation_id_debug)!!

    

    private var emailAutoCompleteBackground: Drawable? = null

    init {
        emailAutoCompleteTextView.addTextChangedListener {
            afterTextChanged {
                showError(null)
                if (it.isEmpty()) presenter.onClear()
                presenter.email = it.toString()
            }
        }
        emailAutoCompleteTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }

        
        
        createAccountButton.maxLines = 2

        createAccountButton.setOnClickListener {
            presenter.onCreateAccountClicked()
        }
    }

    override var email by Delegates.observable<String?>(null) { _, _, value ->
        if (value != null) {
            emailText = value
        }
    }

    override var emailText
        get() = emailAutoCompleteTextView.text.toString()
        set(value) {
            if (value != emailText) {
                emailAutoCompleteTextView.setText(value, TextView.BufferType.EDITABLE)
            }
        }

    override fun requestFocus() {
        emailAutoCompleteTextView.requestFocus()
    }

    override fun setSuggestion(suggestions: List<String>?) {
        val adapter = suggestions?.let {
            ArrayAdapter(context, R.layout.autocomplete_textview_adapter, R.id.listTextView, it)
        }
        emailAutoCompleteTextView.setAdapter(adapter)
        emailAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            presenter.onAutoFill()
        }
    }

    override fun exposeTrackingInstallationId(id: String?) {
        installationIdTextView.text = id
    }

    override fun prepareForTransitionStart() {
        emailAutoCompleteBackground = emailAutoCompleteTextView.background
        emailAutoCompleteTextView.background = null
    }

    override fun prepareForTransitionEnd() {
        emailAutoCompleteTextView.background = emailAutoCompleteBackground
        emailAutoCompleteBackground = null
    }
}