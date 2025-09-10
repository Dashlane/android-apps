package com.dashlane.accountsummary.changecontact

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.accountsummary.R
import com.dashlane.accountsummary.common.ChangeEmailContent
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.SnackbarType

@Composable
fun ChangeContactEmailScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangeContactEmailViewModel,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val uiState by viewModel.uiState.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewModel) {
        viewModel.uiState.sideEffect.collect { navState ->
            when (navState) {
                ChangeContactEmailNavState.Finish -> onBack()
            }
        }
    }

    if (!uiState.isLoading) {
        LaunchedEffect(uiState) {
            if (uiState.isError) {
                snackbarHostState.showSnackbar(context.getString(R.string.network_failed_notification))
            }
        }
        Box {
            ChangeEmailContent(
                modifier = modifier,
                title = stringResource(id = R.string.account_status_change_contact_email),
                currentEmailLabel = stringResource(id = R.string.account_status_change_contact_email_current),
                newEmailLabel = stringResource(id = R.string.account_status_change_contact_email_new),
                currentEmail = uiState.currentContactEmail,
                newEmail = uiState.newContactEmail,
                saveEnabled = uiState.isSaveEnabled,
                onNewEmailChange = viewModel::onNewContactEmailChange,
                onSendNewEmailClicked = viewModel::onSendNewContactEmailClicked,
            )
            DashlaneSnackbarWrapper(type = SnackbarType.ERROR) {
                SnackbarHost(modifier = Modifier.align(Alignment.BottomCenter), hostState = snackbarHostState)
            }
        }
    }
}