package com.dashlane.autofill.phishing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.phishing.ui.PhishingWarningScreen
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.util.getParcelableCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutofillPhishingWarningFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    parseNavigationArguments(requireArguments()).let { (itemId, formSource) ->
                        PhishingWarningScreen(
                            viewModel = hiltViewModel(),
                            itemId = itemId,
                            formSource = formSource,
                            finishWithResult = ::finishWithResult,
                        )
                    }
                }
            }
        }
    }

    private fun parseNavigationArguments(arguments: Bundle): Pair<String, AutoFillFormSource> {
        val itemId = arguments.getString(EXTRA_PHISHING_ITEM_ID)
        val formSource = arguments.getParcelableCompat(
            name = EXTRA_PHISHING_FORM_SOURCE,
            clazz = AutoFillFormSource::class.java,
        )
        require(itemId != null && formSource != null) { "Navigation arguments cannot be parsed" }

        return itemId to formSource
    }

    private fun finishWithResult(shouldAutofill: Boolean) {
        setFragmentResult(
            PHISHING_WARNING_RESULT,
            bundleOf(PHISHING_WARNING_SHOULD_AUTOFILL to shouldAutofill),
        )
        dismiss()
    }

    companion object {
        const val PHISHING_WARNING_RESULT = "PHISHING_WARNING_RESULT"
        const val PHISHING_WARNING_SHOULD_AUTOFILL = "PHISHING_WARNING_PARAMS_RESULT"
        private const val EXTRA_PHISHING_ITEM_ID = "EXTRA_PHISHING_ITEM_ID"
        private const val EXTRA_PHISHING_FORM_SOURCE = "EXTRA_PHISHING_FORM_SOURCE"

        fun create(
            formSource: AutoFillFormSource,
            itemId: String?,
        ): AutofillPhishingWarningFragment =
            AutofillPhishingWarningFragment().apply {
                arguments = bundleOf(
                    EXTRA_PHISHING_FORM_SOURCE to formSource,
                    EXTRA_PHISHING_ITEM_ID to itemId,
                )
            }
    }
}