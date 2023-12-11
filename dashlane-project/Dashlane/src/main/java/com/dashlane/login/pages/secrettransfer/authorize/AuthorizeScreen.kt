package com.dashlane.login.pages.secrettransfer.authorize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.account.UserAccountInfo
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferState
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferViewModel
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.ui.widgets.compose.LoadingScreen

@Composable
fun AuthorizeScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginSecretTransferViewModel,
    onCancelled: () -> Unit,
    onSuccess: (UserAccountInfo.AccountType) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.login()
    }

    when (val state = uiState) {
        is LoginSecretTransferState.Error -> GenericErrorContent(
            title = stringResource(id = R.string.login_secret_transfer_login_error_title),
            message = stringResource(id = R.string.login_secret_transfer_login_error_message),
            textPrimary = stringResource(id = R.string.login_secret_transfer_error_button_retry),
            textSecondary = stringResource(id = R.string.login_secret_transfer_error_button_cancel),
            onClickPrimary = viewModel::login,
            onClickSecondary = viewModel::cancelOnError
        )

        is LoginSecretTransferState.Cancelled -> onCancelled()
        is LoginSecretTransferState.Success -> {
            viewModel.hasNavigated()
            onSuccess(state.accountType)
        }

        else -> {
            LoadingScreen(modifier, stringResource(R.string.login_secret_transfer_loading_login_title))
        }
    }
}