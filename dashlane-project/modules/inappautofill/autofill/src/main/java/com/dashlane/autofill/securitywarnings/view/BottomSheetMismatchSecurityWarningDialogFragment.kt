package com.dashlane.autofill.securitywarnings.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.dashlane.autofill.api.R
import com.dashlane.autofill.securitywarnings.data.SecurityWarningAction
import com.dashlane.autofill.securitywarnings.data.SecurityWarningType
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.ui.configureBottomSheet
import com.dashlane.util.getParcelableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetMismatchSecurityWarningDialogFragment : BottomSheetDialogFragment() {

    private var formSource: AutoFillFormSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        formSource = requireArguments().getParcelableCompat(ARG_FORM_SOURCE)

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.security_warning_mismatch_bottom_sheet_dialog,
            container,
            false
        )
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val authentifiantLabel = arguments?.getString(ARG_AUTHENTIFIANT_LABEL) ?: ""
        val formSourceLabel = arguments?.getString(ARG_FORM_SOURCE_LABEL) ?: ""

        val title = view.findViewById<TextView>(R.id.dialog_title)
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox_do_not_show_again)
        val positiveButton = view.findViewById<Button>(R.id.autofill_button)
        val negativeButton = view.findViewById<Button>(R.id.cancel_button)

        title?.text =
            getString(R.string.autofill_mismatch_warning_title, authentifiantLabel, formSourceLabel)
        positiveButton.setOnClickListener {
            setResult(SecurityWarningAction.POSITIVE, checkBox.isChecked)
            dismiss()
        }
        negativeButton.setOnClickListener {
            setResult(SecurityWarningAction.NEGATIVE, false)
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).configureBottomSheet()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        setResult(SecurityWarningAction.CANCEL, false)
        dismiss()
    }

    private fun setResult(action: SecurityWarningAction, checked: Boolean) {
        setFragmentResult(
            SecurityWarningsViewProxy.SECURITY_WARNING_ACTION_RESULT,
            bundleOf(
                SecurityWarningsViewProxy.PARAMS_WARNING_TYPE to SecurityWarningType.Mismatch(
                    formSource,
                    checked
                ),
                SecurityWarningsViewProxy.PARAMS_ACTION to action
            )
        )
    }

    companion object {

        private const val ARG_FORM_SOURCE = "form_source"
        private const val ARG_FORM_SOURCE_LABEL = "formSourceLabel"
        private const val ARG_AUTHENTIFIANT_LABEL = "authentifiantLabel"

        fun create(
            authentifiantLabel: String,
            formSourceLabel: String,
            formSource: ApplicationFormSource?
        ): BottomSheetMismatchSecurityWarningDialogFragment =
            BottomSheetMismatchSecurityWarningDialogFragment().apply {
                arguments = bundleOf(
                    ARG_AUTHENTIFIANT_LABEL to authentifiantLabel,
                    ARG_FORM_SOURCE_LABEL to formSourceLabel,
                    ARG_FORM_SOURCE to formSource
                )
            }
    }
}
