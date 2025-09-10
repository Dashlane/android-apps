package com.dashlane.autofill.phishing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.phishing.PhishingWarningState
import com.dashlane.autofill.phishing.PhishingWarningViewModel

@Composable
fun PhishingWarningScreen(
    viewModel: PhishingWarningViewModel,
    finishWithResult: (Boolean) -> Unit,
    itemId: String,
    formSource: AutoFillFormSource,
) {
    LaunchedEffect(viewModel) {
        viewModel.onViewStarted(
            itemId = itemId,
            webFormSource = formSource as WebDomainFormSource,
        )
        viewModel.state.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is PhishingWarningState.SideEffect.OnResult -> finishWithResult(sideEffect.shouldAutofill)
            }
        }
    }

    val uiState by viewModel.state.viewState.collectAsStateWithLifecycle()

    PhishingWarningDialog(
        state = uiState,
        onTrustWebsite = viewModel::trustAndAllowAutofill,
        onMainActionClick = viewModel::dismiss,
        onDismissRequest = viewModel::dismiss,
    )
}