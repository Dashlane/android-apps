package com.dashlane.login.pages.secrettransfer.authorize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferState
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferViewModel
import com.dashlane.ui.common.compose.components.LoadingScreen
import com.dashlane.ui.widgets.compose.GenericErrorContent

@Composable
fun AuthorizeScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginSecretTransferViewModel,
    onCancelled: () -> Unit,
    onSuccess: (UserAccountInfo.AccountType) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.login()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginSecretTransferState.ConfirmEmail,
            is LoginSecretTransferState.Error,
            is LoginSecretTransferState.Initial,
            is LoginSecretTransferState.LoadingLogin -> Unit
            is LoginSecretTransferState.Cancelled -> {
                viewModel.hasNavigated()
                onCancelled()
            }
            is LoginSecretTransferState.Success -> {
                viewModel.hasNavigated()
                onSuccess(state.accountType)
            }
        }
    }

    when (uiState) {
        is LoginSecretTransferState.Error -> GenericErrorContent(
            title = stringResource(id = R.string.login_secret_transfer_login_error_title),
            message = stringResource(id = R.string.login_secret_transfer_login_error_message),
            textPrimary = stringResource(id = R.string.login_secret_transfer_error_button_retry),
            textSecondary = stringResource(id = R.string.login_secret_transfer_error_button_cancel),
            onClickPrimary = viewModel::login,
            onClickSecondary = viewModel::cancelOnError
        )
        else -> {
            LoadingScreen(modifier, stringResource(R.string.login_secret_transfer_loading_login_title))
        }
    }
}