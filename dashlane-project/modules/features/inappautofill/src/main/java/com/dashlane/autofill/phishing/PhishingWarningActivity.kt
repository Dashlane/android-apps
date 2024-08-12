package com.dashlane.autofill.phishing

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.phishing.PhishingWarningViewModel.Companion.EXTRA_PHISHING_HINT_SUMMARY
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Checkbox
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhishingWarningActivity : AutoFillResponseActivity() {

    private val viewModel by viewModels<PhishingWarningViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        autofillUsageLog.onAutoFillWarningClick()
        setContent {
            DashlaneTheme {
                val state by viewModel.uiState.collectAsState()
                LaunchedEffect(key1 = state) {
                    if (state is PhishingWarningViewModel.UiState.DataSaved) {
                        finish()
                    }
                }
                Dialog(
                    title = stringResource(
                        id = if (phishingAttemptLevel == PhishingAttemptLevel.HIGH) {
                            R.string.phishing_warning_title_high
                        } else {
                            R.string.phishing_warning_title_moderate
                        }
                    ),
                    description = {
                        Text(text = stringResource(id = R.string.phishing_warning_content))
                        if (!state.data.isAlreadyRemembered) {
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
                    },
                    mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.phishing_warning_go_back)),
                    mainActionClick = {
                        viewModel.dismiss(origin = PhishingWarningViewModel.PhishingWarningOrigin.PHISHING_WARNING)
                    },
                    onDismissRequest = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        internal fun getAuthIntentSenderForPhishingWarning(
            context: Context,
            summary: AutoFillHintSummary,
            phishingAttemptLevel: PhishingAttemptLevel
        ): IntentSender {
            val intent = createIntent(context, summary, PhishingWarningActivity::class)
            intent.putExtra(EXTRA_PHISHING_ATTEMPT_LEVEL, phishingAttemptLevel)
            intent.putExtra(EXTRA_PHISHING_HINT_SUMMARY, summary)
            return createIntentSender(context, intent)
        }
    }
}