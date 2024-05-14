package com.dashlane.login.pages.secrettransfer.universal.intro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.pages.secrettransfer.qrcode.DashlaneLogo
import com.dashlane.login.pages.secrettransfer.universal.passphrase.PassphraseIdentificationScreen
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.ui.widgets.compose.ContentStepper
import com.dashlane.ui.widgets.compose.GenericErrorContent
import com.dashlane.ui.widgets.compose.LoadingScreen
import com.dashlane.util.launchUrl

@Composable
fun UniversalIntroScreen(
    modifier: Modifier = Modifier,
    viewModel: UniversalIntroViewModel,
    email: String?,
    onCancel: () -> Unit,
    onSuccess: (SecretTransferPayload, RegisteredUserDevice.Remote) -> Unit,
    onGoToHelp: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val onContactSupport = { context.launchUrl("https://support.dashlane.com/hc/requests/new") }

    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is UniversalIntroState.Error,
            is UniversalIntroState.LoadingPassphrase,
            is UniversalIntroState.LoadingAccount,
            is UniversalIntroState.PassphraseVerification,
            is UniversalIntroState.Initial -> Unit
            is UniversalIntroState.GoToHelp -> {
                viewModel.viewNavigated()
                onGoToHelp(email)
            }
            is UniversalIntroState.Cancel -> {
                onCancel()
            }
            is UniversalIntroState.Success -> {
                viewModel.viewNavigated()
                onSuccess(state.secretTransferPayload, state.registeredUserDevice)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(email)
    }

    when (val state = uiState) {
        is UniversalIntroState.GoToHelp,
        is UniversalIntroState.Initial -> UniversalIntroContent(modifier = modifier, onRecoveryClicked = viewModel::helpClicked)
        is UniversalIntroState.LoadingPassphrase -> LoadingScreen(title = stringResource(id = R.string.login_universal_d2d_loading_challenge))
        is UniversalIntroState.Success,
        is UniversalIntroState.LoadingAccount -> LoadingScreen(title = stringResource(id = R.string.login_universal_d2d_loading_account))
        is UniversalIntroState.PassphraseVerification -> {
            PassphraseIdentificationScreen(
                passphrase = state.data.passphrase ?: emptyList()
            )
        }
        is UniversalIntroState.Error -> UniversalIntroErrorContent(error = state.error, onCancel = onCancel, onContactSupport = onContactSupport)
        is UniversalIntroState.Cancel -> Unit
    }
}

@Composable
fun UniversalIntroContent(
    modifier: Modifier = Modifier,
    onRecoveryClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        DashlaneLogo()
        Text(
            text = stringResource(id = R.string.login_universal_d2d_intro_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(
                    top = 72.dp,
                    bottom = 24.dp
                )
        )
        InfoboxMedium(
            title = stringResource(id = R.string.login_universal_d2d_intro_infoxbox),
            mood = Mood.Neutral
        )
        ContentStepper(
            modifier = Modifier.padding(top = 24.dp),
            content = listOf(
                stringResource(id = R.string.login_universal_d2d_intro_step1),
                stringResource(R.string.login_universal_d2d_intro_step2),
                stringResource(id = R.string.login_universal_d2d_intro_step3),
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 32.dp),
            onClick = onRecoveryClicked,
            intensity = Intensity.Quiet,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.login_universal_d2d_intro_recovery_button)
            )
        )
    }
}

@Composable
fun UniversalIntroErrorContent(
    error: UniversalIntroError,
    onCancel: () -> Unit,
    onContactSupport: () -> Unit
) {
    when (error) {
        UniversalIntroError.Generic -> {
            GenericErrorContent(
                title = stringResource(id = R.string.login_universal_d2d_generic_error_title),
                message = stringResource(id = R.string.login_universal_d2d_generic_error_description),
                textPrimary = stringResource(id = R.string.login_universal_d2d_generic_error_primary_button),
                textSecondary = stringResource(id = R.string.login_universal_d2d_generic_error_secondary_button),
                onClickPrimary = onCancel,
                onClickSecondary = onContactSupport
            )
        }
        UniversalIntroError.Timeout -> {
            GenericErrorContent(
                title = stringResource(id = R.string.login_universal_d2d_timeout_error_title),
                message = stringResource(id = R.string.login_universal_d2d_timeout_error_description),
                textPrimary = stringResource(id = R.string.login_universal_d2d_timeout_error_primary_button),
                onClickPrimary = onCancel
            )
        }
    }
}

@Preview
@Composable
fun UniversalIntroContentPreview() {
    DashlanePreview { UniversalIntroContent(onRecoveryClicked = {}) }
}
