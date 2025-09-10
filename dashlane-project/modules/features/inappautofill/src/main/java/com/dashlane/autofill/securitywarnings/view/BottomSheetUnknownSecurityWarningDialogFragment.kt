package com.dashlane.autofill.securitywarnings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.securitywarnings.data.SecurityWarningAction
import com.dashlane.autofill.securitywarnings.data.SecurityWarningType
import com.dashlane.autofill.securitywarnings.ui.UnknownSourceWarningBottomSheetScreen
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.util.getParcelableCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetUnknownSecurityWarningDialogFragment : BottomSheetDialogFragment() {
    private var formSource: AutoFillFormSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        formSource = requireArguments().getParcelableCompat(ARG_FORM_SOURCE)
        require(formSource != null) {
            "Navigation arguments cannot be parsed as expected"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    UnknownSourceWarningBottomSheetScreen(
                        onDismissRequest = ::onFillCancelled,
                        onAllowClick = ::onFillAllowed,
                        onCancelClick = ::onFillDenied,
                    )
                }
            }
        }
    }

    private fun onFillAllowed() {
        setResult(action = SecurityWarningAction.POSITIVE)
    }

    private fun onFillDenied() {
        setResult(action = SecurityWarningAction.NEGATIVE)
    }

    private fun onFillCancelled() {
        setResult(action = SecurityWarningAction.CANCEL)
    }

    private fun setResult(
        action: SecurityWarningAction,
    ) {
        setFragmentResult(
            requestKey = SecurityWarningsViewProxy.SECURITY_WARNING_ACTION_RESULT,
            result = bundleOf(
                SecurityWarningsViewProxy.PARAMS_WARNING_TYPE to SecurityWarningType.Unknown(formSource),
                SecurityWarningsViewProxy.PARAMS_ACTION to action,
            ),
        )
        dismiss()
    }

    companion object {
        private const val ARG_FORM_SOURCE = "form_source"

        fun create(formSource: AutoFillFormSource): BottomSheetUnknownSecurityWarningDialogFragment =
            BottomSheetUnknownSecurityWarningDialogFragment().apply {
                arguments = bundleOf(ARG_FORM_SOURCE to formSource)
            }
    }
}