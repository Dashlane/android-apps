package com.dashlane.accountsummary.changelogin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.accountsummary.R
import com.dashlane.accountsummary.common.ChangeEmailContent

@Composable
fun ChangeLoginEmailScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangeLoginEmailViewModel,
    onBack: () -> Unit,
    onNext: (String) -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewModel) {
        viewModel.stateFlow.sideEffect.collect { navState ->
            when (navState) {
                ChangeLoginEmailState.SideEffect.Finish -> onBack()
                is ChangeLoginEmailState.SideEffect.Next -> onNext(navState.newEmail)
            }
        }
    }

    ChangeEmailContent(
        modifier = modifier,
        title = stringResource(id = R.string.change_login_email_title),
        currentEmailLabel = stringResource(id = R.string.change_login_email_previous_field_label),
        newEmailLabel = stringResource(id = R.string.change_login_email_new_field_label),
        currentEmail = uiState.data.currentLoginEmail,
        newEmail = uiState.data.newLoginEmail,
        saveEnabled = uiState.data.isContinueEnabled,
        error = uiState.data.error.toString(),
        onNewEmailChange = { viewModel.sendEvent(ChangeLoginEmailEvent.NewEmailChanged(it)) },
        onSendNewEmailClicked = { viewModel.sendEvent(ChangeLoginEmailEvent.NextClicked) },
    )
}

@Composable
private fun ChangeLoginEmailError?.toString(): String? {
    return when (this) {
        ChangeLoginEmailError.EmailUnavailable -> stringResource(R.string.change_login_email_error_unavailable)
        ChangeLoginEmailError.InvalidEmail -> stringResource(R.string.change_login_email_error_invalid)
        ChangeLoginEmailError.Generic -> stringResource(R.string.error)
        ChangeLoginEmailError.Network -> stringResource(R.string.network_failed_notification)
        null -> null
    }
}