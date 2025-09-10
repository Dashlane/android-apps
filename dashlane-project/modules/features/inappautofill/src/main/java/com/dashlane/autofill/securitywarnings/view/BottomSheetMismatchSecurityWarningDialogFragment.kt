package com.dashlane.autofill.securitywarnings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.securitywarnings.data.SecurityWarningAction
import com.dashlane.autofill.securitywarnings.data.SecurityWarningType
import com.dashlane.autofill.securitywarnings.ui.MismatchSourceWarningBottomSheet
import com.dashlane.design.theme.DashlaneTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetMismatchSecurityWarningDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    MismatchSourceWarningBottomSheet(
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
        applicationFormSource: ApplicationFormSource?,
        doNotShowAgain: Boolean,
    ) {
        setResult(
            action = SecurityWarningAction.POSITIVE,
            applicationFormSource = applicationFormSource,
            checked = doNotShowAgain,
        )
    }

    private fun onFillDenied(
        applicationFormSource: ApplicationFormSource?,
    ) {
        setResult(
            action = SecurityWarningAction.NEGATIVE,
            applicationFormSource = applicationFormSource,
            checked = false,
        )
    }

    private fun onFillCancelled(
        applicationFormSource: ApplicationFormSource?,
    ) {
        setResult(
            action = SecurityWarningAction.CANCEL,
            applicationFormSource = applicationFormSource,
            checked = false,
        )
    }

    private fun setResult(
        action: SecurityWarningAction,
        applicationFormSource: ApplicationFormSource?,
        checked: Boolean,
    ) {
        setFragmentResult(
            requestKey = SecurityWarningsViewProxy.SECURITY_WARNING_ACTION_RESULT,
            result = bundleOf(
                SecurityWarningsViewProxy.PARAMS_WARNING_TYPE to SecurityWarningType.Mismatch(
                    applicationFormSource,
                    checked,
                ),
                SecurityWarningsViewProxy.PARAMS_ACTION to action,
            ),
        )
        dismiss()
    }

    companion object {
        internal const val ARG_FORM_SOURCE = "form_source"
        internal const val ARG_FORM_SOURCE_LABEL = "formSourceLabel"
        internal const val ARG_ITEM_TO_FILL = "itemToFill"

        fun create(
            authentifiantToFill: AuthentifiantItemToFill,
            formSourceLabel: String,
            formSource: ApplicationFormSource?,
        ): BottomSheetMismatchSecurityWarningDialogFragment =
            BottomSheetMismatchSecurityWarningDialogFragment().apply {
                arguments = bundleOf(
                    ARG_ITEM_TO_FILL to authentifiantToFill,
                    ARG_FORM_SOURCE_LABEL to formSourceLabel,
                    ARG_FORM_SOURCE to formSource,
                )
            }
    }
}