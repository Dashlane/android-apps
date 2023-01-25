package com.dashlane.autofill.api.securitywarnings.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.ui.configureBottomSheet
import com.dashlane.util.getParcelableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetUnknownSecurityWarningDialogFragment : BottomSheetDialogFragment() {

    interface Actions {
        fun unknownDialogPositiveAction(domain: Domain)
        fun unknownDialogNegativeAction(domain: Domain)
        fun unknownDialogCancelAction(domain: Domain)
    }

    private lateinit var domain: Domain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val formSource = requireArguments().getParcelableCompat<AutoFillFormSource>(ARG_FORM_SOURCE)
        domain = formSource.getDomain()

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.security_warning_unknown_bottom_sheet_dialog,
            container,
            false
        )
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val positiveButton = view.findViewById<Button>(R.id.autofill_button)
        val negativeButton = view.findViewById<Button>(R.id.cancel_button)

        positiveButton.setOnClickListener {
            getActions()?.unknownDialogPositiveAction(domain)
            dismiss()
        }
        negativeButton.setOnClickListener {
            getActions()?.unknownDialogNegativeAction(domain)
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).configureBottomSheet()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        getActions()?.unknownDialogCancelAction(domain)
        dismiss()
    }

    private fun getActions(): Actions? = this.activity as? Actions

    companion object {

        private const val ARG_FORM_SOURCE = "form_source"

        fun create(formSource: AutoFillFormSource): BottomSheetUnknownSecurityWarningDialogFragment =
            BottomSheetUnknownSecurityWarningDialogFragment().apply {
                arguments = bundleOf(ARG_FORM_SOURCE to formSource)
            }
    }
}
