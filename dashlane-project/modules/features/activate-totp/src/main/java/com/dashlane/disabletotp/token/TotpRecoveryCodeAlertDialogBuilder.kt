package com.dashlane.disabletotp.token

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.dashlane.activatetotp.databinding.Dialog2faRecoveryBinding
import com.dashlane.ui.util.DialogHelper

class TotpRecoveryCodeAlertDialogBuilder(val context: Activity) {

    private val binding = Dialog2faRecoveryBinding.inflate(context.layoutInflater)
    private var dialog: AlertDialog? = null

    fun getDialog() = dialog

    fun create(
        title: String,
        message: String,
        hint: String,
        positiveText: String,
        positiveAction: ((String) -> Unit),
        negativeText: String,
        negativeAction: (() -> Unit)
    ) {
        val dialogBuilder = DialogHelper().builder(context).apply {
            setView(binding.root)
            setCancelable(true)
            setOnCancelListener { negativeAction() }
        }

        val alertDialog = dialogBuilder.create()

        binding.title.text = title
        binding.description.text = message
        binding.editRecoveryCode.hint = hint

        binding.positive.apply {
            text = positiveText
            setOnClickListener {
                positiveAction(binding.editRecoveryCode.editText?.text.toString())
            }
        }

        binding.negative.apply {
            text = negativeText
            setOnClickListener {
                negativeAction()
                alertDialog.dismiss()
            }
        }

        this.dialog = alertDialog
    }

    fun setIsError(isError: Boolean, errorMessage: String) {
        binding.editRecoveryCode.error = if (isError) errorMessage else null
    }
}
