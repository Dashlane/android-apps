package com.dashlane.createaccount.email

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.createaccount.ui.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sso.login.SSOContract
import com.dashlane.ui.common.compose.components.ContactAdminSsoErrorDialog
import com.dashlane.ui.common.compose.components.EmailTextField

@Composable
fun CreateAccountEmailScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateAccountEmailViewModel,
    cancel: () -> Unit,
    goToNext: (String, Boolean) -> Unit,
    onHelpCenter: (Uri?) -> Unit,
) {
    val goToSso =
        rememberLauncherForActivityResult(SSOContract(), onResult = { viewModel.sendEvent(CreateAccountEmailEvent.SsoLoginComplete(it)) })

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                CreateAccountEmailState.SideEffect.Cancel -> cancel()
                is CreateAccountEmailState.SideEffect.GoToNext -> goToNext(sideEffect.email, sideEffect.isB2B)
                is CreateAccountEmailState.SideEffect.GoToHelp -> onHelpCenter(null)
                is CreateAccountEmailState.SideEffect.GoToSso -> goToSso.launch(sideEffect.ssoInfo)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    CreateAccountEmailContent(
        modifier = modifier,
        isContinueEnabled = uiState.data.isContinueEnabled,
        isLoading = uiState is CreateAccountEmailState.View.Loading,
        email = uiState.data.email,
        error = uiState.data.error?.toErrorMessage(),
        sendEvent = viewModel::sendEvent,
    )

    if (uiState is CreateAccountEmailState.View.DisplayValidateEmailDialog) {
        CreateAccountEmailConfirmUnlikelyDialog(
            email = uiState.data.email.text,
            sendEvent = viewModel::sendEvent,
        )
    }

    if (uiState is CreateAccountEmailState.View.DisplaySsoAdminErrorDialog) {
        ContactAdminSsoErrorDialog(onCancel = { viewModel.sendEvent(CreateAccountEmailEvent.CancelDialog) })
    }

    if (uiState is CreateAccountEmailState.View.DisplayExpiredVersionErrorDialog) {
        ExpiredVersionErrorDialog(sendEvent = viewModel::sendEvent)
    }
}

@Composable
fun CreateAccountEmailContent(
    modifier: Modifier = Modifier,
    isContinueEnabled: Boolean,
    isLoading: Boolean,
    email: TextFieldValue,
    error: String?,
    sendEvent: (CreateAccountEmailEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, end = 24.dp, start = 24.dp),
    ) {
        DashlaneLogoLockup(
            height = 40.dp,
        )
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(id = R.string.create_account_email_title),
            style = DashlaneTheme.typography.titleSectionLarge,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(id = R.string.create_account_email_subtitle),
            style = DashlaneTheme.typography.bodyStandardRegular,
        )
        Spacer(modifier = Modifier.weight(1f))
        EmailTextField(
            label = stringResource(id = R.string.email_address_login_screen),
            email = email,
            error = error,
            onEmailChange = { sendEvent(CreateAccountEmailEvent.EmailChanged(it)) },
            onClickContinue = { sendEvent(CreateAccountEmailEvent.ContinueClicked) },
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { sendEvent(CreateAccountEmailEvent.ContinueClicked) },
            intensity = Intensity.Catchy,
            enabled = isContinueEnabled && !isLoading,
            layout = if (isLoading) {
                ButtonLayout.IndeterminateProgress
            } else {
                ButtonLayout.TextOnly(text = stringResource(id = R.string.create_account_cta_continue))
            },
        )
    }
}

@Composable
fun CreateAccountEmailConfirmUnlikelyDialog(
    email: String,
    sendEvent: (CreateAccountEmailEvent) -> Unit,
) {
    Dialog(
        title = email,
        description = { Text(text = stringResource(id = R.string.create_account_confirm_unlikely_email)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.confirm)),
        mainActionClick = { sendEvent(CreateAccountEmailEvent.ConfirmEmail) },
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = { sendEvent(CreateAccountEmailEvent.CancelDialog) },
        onDismissRequest = { sendEvent(CreateAccountEmailEvent.CancelDialog) },
    )
}

@Composable
fun ExpiredVersionErrorDialog(
    sendEvent: (CreateAccountEmailEvent) -> Unit,
) {
    Dialog(
        title = stringResource(R.string.expired_version_noupdate_title),
        description = { Text(text = stringResource(id = R.string.expired_version_noupdate_desc)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cta_endoflife_learn_more)),
        mainActionClick = { sendEvent(CreateAccountEmailEvent.HelpClicked) },
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cta_endoflife_close)),
        additionalActionClick = { sendEvent(CreateAccountEmailEvent.CancelDialog) },
        onDismissRequest = { sendEvent(CreateAccountEmailEvent.CancelDialog) },
    )
}

@Composable
fun CreateAccountEmailError.toErrorMessage(): String {
    return stringResource(
        when (this) {
            CreateAccountEmailError.AlreadyExist -> R.string.username_exists_already
            CreateAccountEmailError.EmptyEmail -> R.string.create_account_error_email_empty_description
            CreateAccountEmailError.InvalidEmail -> R.string.create_account_error_email_description
            CreateAccountEmailError.Network -> R.string.unable_to_verify_username_with_dashlane_at_this_time_please_try_again_later
            CreateAccountEmailError.Sso -> R.string.sso_error_not_correct
            CreateAccountEmailError.Generic -> R.string.general_error
            else -> R.string.general_error
        },
    )
}

@Preview
@Composable
private fun CreateAccountContentPreview() {
    DashlanePreview {
        CreateAccountEmailContent(
            email = TextFieldValue("randomemail@provider.com"),
            error = "Error",
            sendEvent = { },
            isContinueEnabled = false,
            isLoading = false,
        )
    }
}

@Preview
@Composable
private fun CreateAccountEmailConfirmUnlikelyDialogPreview() {
    DashlanePreview {
        CreateAccountEmailConfirmUnlikelyDialog(
            email = "randomemail@provider.com",
            sendEvent = { },
        )
    }
}

@Preview
@Composable
private fun ExpiredVersionErrorDialogPreview() {
    DashlanePreview {
        ExpiredVersionErrorDialog(
            sendEvent = { },
        )
    }
}