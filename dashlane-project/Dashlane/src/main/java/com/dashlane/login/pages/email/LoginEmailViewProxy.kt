package com.dashlane.login.pages.email

import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import com.dashlane.R
import com.dashlane.login.pages.LoginBaseSubViewProxy
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.addTextChangedListener
import kotlin.properties.Delegates

class LoginEmailViewProxy(view: View) :
    LoginBaseSubViewProxy<LoginEmailContract.Presenter>(view),
    LoginEmailContract.ViewProxy {

    private val emailAutoCompleteTextView = findViewByIdEfficient<AutoCompleteTextView>(R.id.view_login_email)!!
    private val createAccountButton = findViewByIdEfficient<AppCompatButton>(R.id.btn_create_account)!!
    private val installationIdTextView = findViewByIdEfficient<TextView>(R.id.installation_id_debug)!!
    private val secretTransferButton = findViewByIdEfficient<AppCompatButton>(R.id.btn_login_secret_transfer)!!
    private var dialog: AlertDialog? = null

    private var emailAutoCompleteBackground: Drawable? = null

    init {
        emailAutoCompleteTextView.addTextChangedListener {
            afterTextChanged {
                showError(null)
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

        secretTransferButton.setOnClickListener {
            presenter.showSecretTransferQRPage()
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

    override fun showConfirmationDialog() {
        dialog?.dismiss()
        dialog = DialogHelper().builder(ContextThemeWrapper(context, R.style.Theme_Dashlane_NoActionBar))
            .setMessage(R.string.user_support_file_confirmation_description)
            .setNegativeButton(R.string.cancel) { _, _ -> dialog?.dismiss() }
            .setPositiveButton(R.string.ok) { _, _ -> presenter.userSupportFileConfirmed() }
            .setCancelable(false)
            .show()
    }

    override fun showUploadingDialog() {
        dialog?.dismiss()
        dialog = DialogHelper().builder(ContextThemeWrapper(context, R.style.Theme_Dashlane_NoActionBar))
            .setMessage(R.string.user_support_file_upload_description)
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }

    override fun showUploadFinishedDialog(crashDeviceId: String, copy: (String) -> Unit) {
        dialog?.dismiss()
        dialog = DialogHelper().builder(ContextThemeWrapper(context, R.style.Theme_Dashlane_NoActionBar))
            .setTitle(R.string.user_support_file_finish_title)
            .setMessage(crashDeviceId)
            .setPositiveButton(R.string.user_support_file_copy) { _, _ ->
                copy(crashDeviceId)
                dialog?.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun showUploadFailedDialog() {
        dialog?.dismiss()
        DialogHelper().builder(ContextThemeWrapper(context, R.style.Theme_Dashlane_NoActionBar))
            .setMessage(R.string.user_support_file_fail_title)
            .setNegativeButton(R.string.ok, null)
            .show()
    }
}
