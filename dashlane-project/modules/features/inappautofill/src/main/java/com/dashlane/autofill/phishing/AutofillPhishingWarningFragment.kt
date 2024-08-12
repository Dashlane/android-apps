package com.dashlane.autofill.phishing

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.phishing.PhishingWarningViewModel.Companion.EXTRA_PHISHING_HINT_SUMMARY
import com.dashlane.autofill.phishing.PhishingWarningViewModel.Companion.EXTRA_PHISHING_ITEM_ID
import com.dashlane.autofill.phishing.PhishingWarningViewModel.Companion.EXTRA_PHISHING_ITEM_WEBSITE
import com.dashlane.autofill.phishing.PhishingWarningViewModel.Companion.EXTRA_PHISHING_LEVEL
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Checkbox
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutofillPhishingWarningFragment : DialogFragment() {

    private val viewModel by viewModels<PhishingWarningViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    val state by viewModel.uiState.collectAsState()
                    LaunchedEffect(key1 = state) {
                        if (state is PhishingWarningViewModel.UiState.DataSaved) {
                            setResult(Activity.RESULT_OK)
                        }
                    }
                    PhishingWarningDialog(state)
                }
            }
        }
    }

    @Composable
    fun PhishingWarningDialog(state: PhishingWarningViewModel.UiState) {
        Dialog(
            title = stringResource(id = R.string.autofill_incorrect_warning_title),
            description = {
                DialogBody(state)
            },
            additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.phishing_warning_trust)),
            additionalActionClick = {
                viewModel.trustWebsite()
            },
            mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.phishing_warning_not_trust)),
            mainActionClick = {
                viewModel.dismiss(origin = PhishingWarningViewModel.PhishingWarningOrigin.AUTOFILL_PHISHING_WARNING)
                setResult(Activity.RESULT_CANCELED)
            },
            onDismissRequest = {
                viewModel.dismiss(origin = PhishingWarningViewModel.PhishingWarningOrigin.AUTOFILL_PHISHING_WARNING)
                setResult(Activity.RESULT_CANCELED)
            }
        )
    }

    @Composable
    fun DialogBody(state: PhishingWarningViewModel.UiState) {
        Column {
            Text(
                text = if (state.data.phishingAttemptLevel == PhishingAttemptLevel.HIGH) {
                    stringResource(id = R.string.phishing_warning_trust_description_high)
                } else {
                    stringResource(id = R.string.phishing_warning_trust_description)
                }
            )
            state.data.itemWebsite?.let { itemWebsite ->
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    label = stringResource(id = R.string.phishing_warning_trusted_url),
                    value = itemWebsite,
                    readOnly = true,
                    onValueChange = {}
                )
            }
            state.data.website?.let { website ->
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    label = stringResource(id = R.string.phishing_warning_current_url),
                    value = website,
                    readOnly = true,
                    onValueChange = {}
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(top = 16.dp)
                    .clickable {
                        viewModel.toggleRemember()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    modifier = Modifier
                        .padding(end = 4.dp),
                    checked = state.data.rememberChecked
                )
                Text(text = stringResource(id = R.string.phishing_do_not_warn_me_again))
            }
        }
    }

    private fun setResult(result: Int) {
        setFragmentResult(
            PHISHING_WARNING_RESULT,
            bundleOf(PHISHING_WARNING_PARAMS_RESULT to result)
        )
        dismiss()
    }

    companion object {
        const val PHISHING_WARNING_RESULT = "PHISHING_WARNING_RESULT"
        const val PHISHING_WARNING_PARAMS_RESULT = "PHISHING_WARNING_PARAMS_RESULT"

        fun create(
            summary: AutoFillHintSummary?,
            itemWebsite: String?,
            itemId: String?,
            phishingAttemptLevel: PhishingAttemptLevel
        ): AutofillPhishingWarningFragment =
            AutofillPhishingWarningFragment().apply {
                arguments = bundleOf(
                    EXTRA_PHISHING_HINT_SUMMARY to summary,
                    EXTRA_PHISHING_ITEM_WEBSITE to itemWebsite,
                    EXTRA_PHISHING_ITEM_ID to itemId,
                    EXTRA_PHISHING_LEVEL to phishingAttemptLevel,
                )
            }
    }
}