package com.dashlane.changemasterpassword.warning

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.changemasterpassword.ui.R
import com.dashlane.ui.common.compose.components.WarningContent

@Composable
fun ChangeMasterPasswordWarningScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangeMasterPasswordWarningViewModel,
    origin: ChangeMasterPasswordOrigin,
    goToNext: () -> Unit,
    cancel: () -> Unit,
    logout: () -> Unit,
) {
    BackHandler {
        viewModel.cancel()
    }
    LaunchedEffect(viewModel) {
        viewModel.viewStarted(origin)
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                ChangeMasterPasswordWarningState.SideEffect.Cancel -> cancel()
                ChangeMasterPasswordWarningState.SideEffect.GoToChangeMP -> goToNext()
                ChangeMasterPasswordWarningState.SideEffect.Logout -> logout()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    WarningContent(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_sync_multi_device),
        contentDescription = stringResource(id = R.string.and_accessibility_content_desc_sync_multi_device_logo),
        title = uiState.title?.let { stringResource(id = it) } ?: "",
        description = uiState.description?.let { stringResource(id = it) } ?: "",
        infoBox = stringResource(id = uiState.infoBox),
        primaryButtonText = stringResource(id = uiState.primaryButtonText),
        secondaryButtonText = stringResource(id = uiState.secondaryButtonText),
        onPrimaryButtonClick = viewModel::next,
        onSecondaryButtonClick = viewModel::cancel,
    )
}