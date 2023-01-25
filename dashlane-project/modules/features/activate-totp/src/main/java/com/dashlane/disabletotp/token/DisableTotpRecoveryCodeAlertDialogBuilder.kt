package com.dashlane.disabletotp.token

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.dashlane.activatetotp.R
import com.dashlane.activatetotp.databinding.DialogDisable2faRecoveryBinding
import com.dashlane.ui.util.DialogHelper

class DisableTotpRecoveryCodeAlertDialogBuilder(val context: Activity) {

    private val binding = DialogDisable2faRecoveryBinding.inflate(context.layoutInflater)
    private var dialog: AlertDialog? = null

    fun getDialog() = dialog

    fun create(
        message: String,
        positiveAction: ((String) -> Unit),
        negativeAction: (() -> Unit)
    ) {

        val dialogBuilder = DialogHelper().builder(context).apply {
            setView(binding.root)
            setCancelable(true)
            setOnCancelListener { negativeAction() }
        }

        val alertDialog = dialogBuilder.create()

        binding.title.text = context.getString(R.string.disable_totp_enter_token_recovery_dialog_title)
        binding.description.text = message

        binding.positive.apply {
            text = context.getString(R.string.disable_totp_enter_token_recovery_dialog_backup_button_positive)
            setOnClickListener {
                positiveAction(binding.editRecoveryCode.editText?.text.toString())
            }
        }

        binding.negative.apply {
            text = context.getString(R.string.disable_totp_enter_token_recovery_dialog_backup_button_negative)
            setOnClickListener {
                negativeAction()
                alertDialog.dismiss()
            }
        }

        this.dialog = alertDialog
    }

    fun setIsError(isError: Boolean) {
        binding.editRecoveryCode.error = if (isError) {
            context.getString(R.string.disable_totp_enter_token_recovery_dialog_backup_error)
        } else {
            null
        }
    }
}
