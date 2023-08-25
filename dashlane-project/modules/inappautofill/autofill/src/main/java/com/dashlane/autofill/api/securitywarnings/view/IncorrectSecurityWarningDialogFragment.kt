package com.dashlane.autofill.api.securitywarnings.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.securitywarnings.data.SecurityWarningAction
import com.dashlane.autofill.api.securitywarnings.data.SecurityWarningType
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.getParcelableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog

class IncorrectSecurityWarningDialogFragment : DialogFragment() {

    private var formSource: AutoFillFormSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        formSource = requireArguments().getParcelableCompat(ARG_FORM_SOURCE)

        setStyle(STYLE_NO_FRAME, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogContext = context ?: return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogHelper = DialogHelper()

        return dialogHelper.builder(dialogContext, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog).apply {
            setIcon(R.drawable.ic_warning_outlined)

            setTitle(R.string.autofill_incorrect_warning_title)

            setView(R.layout.security_warning_incorrect_modal_dialog)

            setPositiveButton(R.string.autofill_warning_accept) { dialogInterface, _ ->
                val dialog: AlertDialog = dialogInterface as AlertDialog
                val isChecked = dialog.findViewById<CheckBox>(R.id.checkbox_do_not_show_again)?.isChecked ?: false
                setResult(SecurityWarningAction.POSITIVE, isChecked)
            }

            setNegativeButton(R.string.autofill_warning_cancel) { _, _ ->
                setResult(SecurityWarningAction.NEGATIVE, false)
            }
        }.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        setResult(SecurityWarningAction.CANCEL, false)
    }

    private fun setResult(action: SecurityWarningAction, isChecked: Boolean) {
        setFragmentResult(
            SecurityWarningsViewProxy.SECURITY_WARNING_ACTION_RESULT,
            bundleOf(
                SecurityWarningsViewProxy.PARAMS_WARNING_TYPE to SecurityWarningType.Incorrect(
                    formSource,
                    isChecked
                ),
                SecurityWarningsViewProxy.PARAMS_ACTION to action
            )
        )
    }

    companion object {

        private const val ARG_FORM_SOURCE = "form_source"

        fun create(formSource: AutoFillFormSource): IncorrectSecurityWarningDialogFragment =
            IncorrectSecurityWarningDialogFragment().apply {
                arguments = bundleOf(ARG_FORM_SOURCE to formSource)
            }
    }
}
