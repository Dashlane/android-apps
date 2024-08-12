package com.dashlane.login.accountrecoverykey.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.ui.widgets.compose.IndeterminateLoading

@Composable
fun IntroScreen(
    viewModel: IntroViewModel,
    registeredUserDevice: RegisteredUserDevice,
    authTicket: String?,
    goToARK: () -> Unit,
    goToTOTP: () -> Unit,
    goToToken: () -> Unit,
    onCancel: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.arkFlowStarted(registeredUserDevice, authTicket)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is IntroState.GoToARK -> {
                viewModel.hasNavigated()
                goToARK()
            }
            is IntroState.GoToTOTP -> {
                viewModel.hasNavigated()
                goToTOTP()
            }
            is IntroState.GoToToken -> {
                viewModel.hasNavigated()
                goToToken()
            }

            else -> Unit
        }
    }

    when (uiState) {
        IntroState.Error -> {
            GenericErrorContent(
                textPrimary = stringResource(id = R.string.generic_error_retry_button),
                textSecondary = stringResource(id = R.string.generic_error_cancel_button),
                onClickPrimary = { viewModel.retry(registeredUserDevice) },
                onClickSecondary = onCancel
            )
        }
        IntroState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IndeterminateLoading(
                    modifier = Modifier.size(56.dp)
                )
            }
        }
        else -> Unit
    }
}