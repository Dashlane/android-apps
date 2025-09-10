package com.dashlane.autofill.securitywarnings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.securitywarnings.data.SecurityWarningAction
import com.dashlane.autofill.securitywarnings.data.SecurityWarningType
import com.dashlane.autofill.securitywarnings.ui.UnsecureSourceWarningDialog
import com.dashlane.design.theme.DashlaneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncorrectSecurityWarningDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    UnsecureSourceWarningDialog(
                        viewModel = hiltViewModel(),
                        onFillCancelled = ::onFillCancelled,
                        onFillAllowed = ::onFillAllowed,
                        onFillDenied = ::onFillDenied,
                    )
                }
            }
        }
    }

    private fun onFillAllowed(
        formSource: AutoFillFormSource,
        isChecked: Boolean,
    ) {
        setResult(
            action = SecurityWarningAction.POSITIVE,
            formSource = formSource,
            isChecked = isChecked,
        )
    }

    private fun onFillDenied(
        formSource: AutoFillFormSource,
    ) {
        setResult(
            action = SecurityWarningAction.NEGATIVE,
            formSource = formSource,
            isChecked = false,
        )
    }

    private fun onFillCancelled(
        formSource: AutoFillFormSource,
    ) {
        setResult(
            action = SecurityWarningAction.CANCEL,
            formSource = formSource,
            isChecked = false,
        )
    }

    private fun setResult(
        action: SecurityWarningAction,
        formSource: AutoFillFormSource,
        isChecked: Boolean,
    ) {
        setFragmentResult(
            requestKey = SecurityWarningsViewProxy.SECURITY_WARNING_ACTION_RESULT,
            result = bundleOf(
                SecurityWarningsViewProxy.PARAMS_WARNING_TYPE to
                    SecurityWarningType.Incorrect(
                        formSource,
                        isChecked,
                    ),
                SecurityWarningsViewProxy.PARAMS_ACTION to action,
            ),
        )
        dismiss()
    }

    companion object {
        internal const val ARG_FORM_SOURCE = "form_source"

        fun create(formSource: AutoFillFormSource): IncorrectSecurityWarningDialogFragment =
            IncorrectSecurityWarningDialogFragment().apply {
                arguments = bundleOf(ARG_FORM_SOURCE to formSource)
            }
    }
}