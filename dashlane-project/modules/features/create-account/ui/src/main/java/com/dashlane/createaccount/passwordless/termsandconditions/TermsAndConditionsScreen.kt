package com.dashlane.createaccount.passwordless.termsandconditions

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.createaccount.ui.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.TermsAndConditionsDescription

@Composable
fun TermsAndConditionsScreen(
    modifier: Modifier = Modifier,
    viewModel: TermsAndConditionsViewModel,
    onCreateAccount: () -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is TermsAndConditionsState.SideEffect.GoToHelp -> onOpenHelpCenterPage(sideEffect.uri)
                TermsAndConditionsState.SideEffect.Success -> onCreateAccount()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        TermAndConditionsContent(
            tosCheck = uiState.isTosChecked,
            offersCheck = uiState.isOffersChecked,
            isLoading = false,
            onCreateAccountClick = viewModel::onCreateAccount,
            onTosCheckChange = viewModel::updateTos,
            onOffersCheckChange = viewModel::updateOffers,
            onOpenHelpCenterPage = viewModel::onHelp,
        )
    }
}

@Composable
fun ColumnScope.TermAndConditionsContent(
    tosCheck: Boolean,
    offersCheck: Boolean,
    isLoading: Boolean = false,
    onCreateAccountClick: () -> Unit,
    onTosCheckChange: (Boolean) -> Unit,
    onOffersCheckChange: (Boolean) -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit,
) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = stringResource(id = R.string.passwordless_terms_and_conditions_title),
        style = DashlaneTheme.typography.titleSectionLarge,
    )
    TermsAndConditionsDescription(
        offersCheck = offersCheck,
        onOffersCheckChange = onOffersCheckChange,
        tosCheck = tosCheck,
        onTosCheckChange = onTosCheckChange,
        onOpenHelpCenterPage = onOpenHelpCenterPage,
    )
    Spacer(modifier = Modifier.weight(1.0f))
    ButtonMedium(
        modifier = Modifier
            .padding(top = 16.dp)
            .align(Alignment.End),
        enabled = tosCheck,
        onClick = onCreateAccountClick,
        mood = Mood.Brand,
        intensity = Intensity.Catchy,
        layout = if (isLoading) {
            ButtonLayout.IndeterminateProgress
        } else {
            ButtonLayout.TextOnly(text = stringResource(id = R.string.passwordless_account_creation_create_account_button))
        },
    )
}

@Preview
@Composable
private fun TermsAndConditionsScreenPreview() {
    DashlanePreview {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
        ) {
            TermAndConditionsContent(
                tosCheck = true,
                offersCheck = false,
                onCreateAccountClick = {},
                onTosCheckChange = {},
                onOffersCheckChange = {},
                onOpenHelpCenterPage = {},
            )
        }
    }
}