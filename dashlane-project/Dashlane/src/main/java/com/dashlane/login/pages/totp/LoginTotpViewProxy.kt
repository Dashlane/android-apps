package com.dashlane.login.pages.totp

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.disabletotp.token.SmsRecoveryCodeDialogState
import com.dashlane.disabletotp.token.TotpRecoveryCodeAlertDialogBuilder
import com.dashlane.disabletotp.token.TotpRecoveryCodeDialogViewModel
import com.dashlane.login.CodeInputViewHelper
import com.dashlane.login.pages.LoginBaseSubViewProxy
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.DeviceUtils
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.getBaseActivity
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates
import kotlinx.coroutines.launch

class LoginTotpViewProxy(view: View) :
    LoginBaseSubViewProxy<LoginTotpContract.Presenter>(view),
    LoginTotpContract.ViewProxy {

    private val smsRecoveryCodeDialogViewModel: TotpRecoveryCodeDialogViewModel
    private val totpLayout = findViewByIdEfficient<TextInputLayout>(R.id.view_login_totp_layout)!!
    private val totpView = findViewByIdEfficient<EditText>(R.id.view_login_totp)!!
    private val totpNfcView = findViewByIdEfficient<ImageView>(R.id.view_totp_u2f_key)!!
    private val btnPush = findViewByIdEfficient<AppCompatButton>(R.id.btn_push)!!
    private val btnRecovery = findViewByIdEfficient<AppCompatButton>(R.id.btn_recovery)!!

    override val totpText: String
        get() = totpView.text.toString()

    override var tokenViewWidth: Int
        get() = totpView.width
        set(value) {
            CodeInputViewHelper.initialize(totpView, value)
        }

    override var showDuoAvailable by Delegates.observable(false) { _, _, _ ->
        updatePushButton()
    }

    override var showAuthenticatorAvailable by Delegates.observable(false) { _, _, _ ->
        updatePushButton()
    }

    override var showU2fAvailable: Boolean
        get() = totpNfcView.visibility == View.VISIBLE
        set(value) {
            if (value) {
                totpNfcView.visibility = View.VISIBLE
            } else {
                totpNfcView.visibility = View.INVISIBLE
            }
        }

    private var u2fDialog: AlertDialog? = null

    private var u2fPresenceDialog: AlertDialog? = null

    private var recoveryCodeDialogBuilder: TotpRecoveryCodeAlertDialogBuilder? = null

    init {
        
        
        btnPush.maxLines = 2

        val activity = context.getBaseActivity() as ComponentActivity

        smsRecoveryCodeDialogViewModel = ViewModelProvider(activity)[TotpRecoveryCodeDialogViewModel::class.java]

        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.CREATED) {
                smsRecoveryCodeDialogViewModel.sharedFlow.collect { state ->
                    when (state) {
                        SmsRecoveryCodeDialogState.Error -> {
                            presenter.notifyUnknownError()
                        }
                        SmsRecoveryCodeDialogState.Success -> {
                            show2FARecoveryEnterCodeDialog(context.getString(R.string.login_totp_enter_token_recovery_dialog_phone_backup_description))
                        }
                    }
                }
            }
        }

        totpView.addTextChangedListener {
            afterTextChanged {
                showError(null)
                if (totpView.text.length == 6) presenter.onCodeCompleted()
            }
        }
        totpView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }
        totpNfcView.setOnClickListener {
            u2fDialog = DialogHelper().builder(context).apply {
                setTitle(R.string.u2f_login_popup_title)
                setMessage(R.string.u2f_login_popup_message)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    
                }
                setCancelable(true)
            }.show()
        }
        btnRecovery.setOnClickListener {
            show2FARecoveryCodeDialog()
        }
    }

    override fun init(savedInstanceState: Bundle?) {
        val width = savedInstanceState?.getInt(STATE_TOTP_VIEW_WIDTH) ?: 0
        tokenViewWidth = width
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val width = tokenViewWidth
        outState.putInt(STATE_TOTP_VIEW_WIDTH, width)
    }

    override fun requestFocus() {
        totpView.requestFocus()
    }

    override fun showError(errorResId: Int, onClick: () -> Unit) {
        u2fPresenceDialog?.dismiss()
        u2fDialog?.dismiss()
        super.showError(errorResId, onClick)
    }

    override fun showU2fKeyNeedsUserPresence() {
        u2fPresenceDialog = DialogHelper().builder(context).apply {
            setTitle(R.string.u2f_login_confirm_presence_popup_title)
            setMessage(R.string.u2f_login_confirm_presence_popup_message)
            setPositiveButton(android.R.string.cancel) { _, _ ->
                
            }
            setCancelable(true)
        }.show()
    }

    override fun showU2fKeyMatched() {
        u2fPresenceDialog?.dismiss()
    }

    override fun showU2fKeyDetected() {
        u2fDialog?.dismiss()
        totpLayout.error = null
        showProgress = true
        DeviceUtils.hideKeyboard(root)
    }

    override fun showRecoveryCodeError() {
        recoveryCodeDialogBuilder?.apply {
            getDialog()?.show()
            setIsError(isError = true, errorMessage = context.getString(R.string.login_totp_enter_token_recovery_dialog_backup_error))
        }
    }

    private fun updatePushButton() {
        btnPush.run {
            when {
                showDuoAvailable -> {
                    setText(R.string.request_duo_challenge_button)
                    setOnClickListener {
                        presenter.onDuoClicked()
                    }
                    isInvisible = false
                }
                showAuthenticatorAvailable -> {
                    setText(R.string.login_totp_use_dashlane_authenticator)
                    setOnClickListener {
                        presenter.onAuthenticatorClicked()
                    }
                    isInvisible = false
                }
                else -> {
                    text = null
                    setOnClickListener(null)
                    isInvisible = true
                }
            }
        }
    }

    private fun show2FARecoveryCodeDialog() {
        DialogHelper()
            .builder(context, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(R.string.login_totp_enter_token_recovery_dialog_title)
            .setMessage(R.string.login_totp_enter_token_recovery_dialog_choice_description)
            .setPositiveButton(R.string.login_totp_enter_token_recovery_dialog_choice_button_positive) { _, _ ->
                show2FARecoveryEnterCodeDialog(context.getString(R.string.login_totp_enter_token_recovery_dialog_backup_description))
            }
            .setNegativeButton(R.string.login_totp_enter_token_recovery_dialog_choice_button_negative) { _, _ ->
                show2FARecoveryPhoneCodeDialog()
            }
            .setCancelable(true)
            .show()
    }

    private fun show2FARecoveryPhoneCodeDialog() {
        DialogHelper()
            .builder(context, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(R.string.disable_totp_enter_token_recovery_dialog_title)
            .setMessage(R.string.login_totp_enter_token_recovery_dialog_phone_description)
            .setPositiveButton(R.string.login_totp_enter_token_recovery_dialog_phone_button_positive) { _, _ ->
                smsRecoveryCodeDialogViewModel.sendRecoveryBySms(email)
            }
            .setNegativeButton(R.string.login_totp_enter_token_recovery_dialog_phone_button_negative) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    private fun show2FARecoveryEnterCodeDialog(message: String) {
        val activity = context.getBaseActivity() ?: return
        recoveryCodeDialogBuilder = TotpRecoveryCodeAlertDialogBuilder(activity).apply {
            create(
                title = context.getString(R.string.disable_totp_enter_token_recovery_dialog_title),
                message = message,
                hint = context.getString(R.string.login_totp_enter_token_recovery_dialog_backup_hint),
                positiveText = context.getString(R.string.login_totp_enter_token_recovery_dialog_backup_button_positive),
                positiveAction = { recoveryCode: String ->
                    recoveryCodeDialogBuilder?.getDialog()?.hide()
                    presenter.onRecoveryCodeInput(recoveryCode)
                },
                negativeText = context.getString(R.string.login_totp_enter_token_recovery_dialog_backup_button_negative),
                negativeAction = { },
            )
        }
        recoveryCodeDialogBuilder?.getDialog()?.apply {
            setOnDismissListener { totpLayout.requestFocus() }
            show()
        }
    }

    companion object {
        const val STATE_TOTP_VIEW_WIDTH = "login_token_width"
    }
}
