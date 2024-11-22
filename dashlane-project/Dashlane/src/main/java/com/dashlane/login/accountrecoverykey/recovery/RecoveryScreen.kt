package com.dashlane.login.accountrecoverykey.recovery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.LoginStrategy
import com.dashlane.ui.common.compose.components.GenericErrorContent
import com.dashlane.ui.widgets.compose.DashlaneSyncProgress
import kotlinx.coroutines.delay

@Composable
fun RecoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: RecoveryViewModel,
    onSuccess: (LoginStrategy.Strategy?) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler {
        
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RecoveryState.Finish -> {
                delay(1_000) 
                onSuccess(state.strategy)
            }
            else -> Unit
        }
    }

    when (uiState) {
        is RecoveryState.Error -> {
            GenericErrorContent(
                modifier = modifier,
                textPrimary = stringResource(id = R.string.generic_error_retry_button),
                textSecondary = stringResource(id = R.string.generic_error_cancel_button),
                onClickPrimary = viewModel::retry,
                onClickSecondary = onCancel
            )
        }
        else -> {
            RecoveryContent(
                modifier = modifier,
                progress = uiState.progress,
                hasFinishedLoading = uiState is RecoveryState.Finish
            )
        }
    }
}

@Composable
fun RecoveryContent(
    modifier: Modifier = Modifier,
    progress: Int?,
    hasFinishedLoading: Boolean
) {
    DashlaneSyncProgress(
        modifier = modifier.fillMaxSize(),
        color = DashlaneTheme.colors.oddityBrand,
        hasFinishedLoading = hasFinishedLoading,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        progress = progress,
        progressText = stringResource(id = R.string.login_account_recovery_key_enter_progress_title),
        successText = stringResource(id = R.string.login_account_recovery_key_enter_success_title)
    )
}

@Preview
@Composable
private fun RecoveryContentPreview() {
    DashlanePreview {
        RecoveryContent(
            progress = 75,
            hasFinishedLoading = false
        )
    }
}