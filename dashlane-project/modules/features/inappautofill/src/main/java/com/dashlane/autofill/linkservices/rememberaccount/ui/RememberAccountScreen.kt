package com.dashlane.autofill.linkservices.rememberaccount.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountState
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountState.SideEffect.FinishWithResult
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountViewModel
import com.dashlane.ui.widgets.compose.IndeterminateLoading

@Composable
fun RememberAccountScreen(
    viewModel: RememberAccountViewModel,
    itemId: String,
    formSource: AutoFillFormSource,
    finishWithResult: (Boolean) -> Unit,
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.onViewStarted(
            itemId = itemId,
            formSource = formSource,
        )
        viewModel.state.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is FinishWithResult -> finishWithResult(sideEffect.shouldAutofill)
            }
        }
    }
    val uiState by viewModel.state.viewState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        RememberAccountState.View.Initial -> LoadingContent()
        is RememberAccountState.View.Loaded -> RememberAccountBottomSheet(
            state = state,
            onAcceptServiceLink = viewModel::onRefuseServiceLink,
            onRefuseServiceLink = viewModel::onAcceptServiceLink,
            onDismiss = viewModel::onCancel,
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        IndeterminateLoading()
    }
}