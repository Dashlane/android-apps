package com.dashlane.createaccount.tos

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.createaccount.passwordless.termsandconditions.TermAndConditionsContent
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun CreateAccountTermsAndConditionsScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateAccountTermsAndConditionsViewModel,
    onSuccess: () -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is CreateAccountTermsAndConditionsState.SideEffect.GoToHelp -> onOpenHelpCenterPage(sideEffect.uri)
                CreateAccountTermsAndConditionsState.SideEffect.Success -> onSuccess()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    CreateAccountTermsAndConditionsContent(
        modifier = modifier,
        isTosChecked = uiState.data.isTosChecked,
        isOffersChecked = uiState.data.isOffersChecked,
        isLoading = uiState is CreateAccountTermsAndConditionsState.View.Loading,
        sendEvent = viewModel::sendEvent,
    )
}

@Composable
fun CreateAccountTermsAndConditionsContent(
    modifier: Modifier = Modifier,
    isTosChecked: Boolean,
    isOffersChecked: Boolean,
    isLoading: Boolean,
    sendEvent: (CreateAccountTermsAndConditionsEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        DashlaneLogoLockup(
            modifier = Modifier.padding(bottom = 24.dp),
            height = 40.dp,
        )
        TermAndConditionsContent(
            tosCheck = isTosChecked,
            offersCheck = isOffersChecked,
            isLoading = isLoading,
            onCreateAccountClick = { sendEvent(CreateAccountTermsAndConditionsEvent.NextClicked) },
            onTosCheckChange = { sendEvent(CreateAccountTermsAndConditionsEvent.TosCheckChanged(it)) },
            onOffersCheckChange = { sendEvent(CreateAccountTermsAndConditionsEvent.OffersCheckChanged(it)) },
            onOpenHelpCenterPage = { sendEvent(CreateAccountTermsAndConditionsEvent.HelpClicked(it)) },
        )
    }
}

@Preview
@Composable
private fun CreateAccountTermsAndConditionsContentPreview() {
    DashlanePreview {
        CreateAccountTermsAndConditionsContent(
            isTosChecked = true,
            isOffersChecked = true,
            isLoading = false,
            sendEvent = { },
        )
    }
}