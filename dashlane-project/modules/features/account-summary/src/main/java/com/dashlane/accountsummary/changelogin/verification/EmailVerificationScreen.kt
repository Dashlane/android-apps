package com.dashlane.accountsummary.changelogin.verification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.accountsummary.R
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.Loading
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.Success
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.TopBarState

@Composable
fun EmailVerificationScreen(
    modifier: Modifier = Modifier,
    updateTopBarState: (topBarState: TopBarState) -> Unit,
    viewModel: EmailVerificationViewModel,
    onSuccess: () -> Unit,
    onSuccessLogout: () -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState is Success) {
        viewModel.sendEvent(EmailVerificationEvent.BackPressed)
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is EmailVerificationState.SideEffect.Success -> {
                    if (sideEffect.isSessionValid) onSuccess() else onSuccessLogout()
                }
            }
        }
    }

    when (val state = uiState) {
        is Success -> {
            updateTopBarState(TopBarState(visible = false, backEnabled = false))
            EmailVerificationSuccessContent(
                modifier = modifier,
                isSessionValid = state.isSessionValid,
                onSuccess = { viewModel.sendEvent(EmailVerificationEvent.SuccessClicked) },
            )
        }
        else -> {
            updateTopBarState(
                TopBarState(visible = true, backEnabled = true, title = stringResource(R.string.account_status_settings_title)),
            )
            EmailVerificationContent(
                modifier = modifier,
                newEmail = state.data.newEmail,
                token = state.data.token,
                isLoading = state is Loading,
                isContinueEnabled = state.data.isContinueEnabled,
                onHelp = { viewModel.sendEvent(EmailVerificationEvent.HelpClicked) },
                onNext = { viewModel.sendEvent(EmailVerificationEvent.NextClicked) },
                onTokenChange = { viewModel.sendEvent(EmailVerificationEvent.TokenChanged(it)) },
                error = state.data.error.toString(),
            )
        }
    }

    if (uiState.data.showHelpDialog) {
        EmailVerificationHelpAlertDialog(
            confirmButtonClick = { viewModel.sendEvent(EmailVerificationEvent.HelpDialogConfirmed) },
            dismissButtonClick = { viewModel.sendEvent(EmailVerificationEvent.HelpDialogDismissed) },
        )
    }
}


@SuppressWarnings("LongMethod", "kotlin:S107")
@Composable
fun EmailVerificationContent(
    modifier: Modifier = Modifier,
    newEmail: String,
    token: TextFieldValue?,
    isLoading: Boolean,
    isContinueEnabled: Boolean,
    error: String?,
    onHelp: () -> Unit,
    onNext: () -> Unit,
    onTokenChange: (TextFieldValue) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 20.dp),
            text = stringResource(R.string.change_login_email_verification_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.change_login_email_verification_description, newEmail),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        TextField(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!textFieldLoaded) {
                        focusRequester.requestFocus()
                        textFieldLoaded = true
                    }
                }
                .fillMaxWidth(),
            onValueChange = onTokenChange,
            label = stringResource(R.string.change_login_email_verification_field_label),
            value = token ?: TextFieldValue(""),
            isError = error != null,
            feedbackText = error,
            keyboardActions = if (isContinueEnabled) {
                KeyboardActions(
                    onDone = { onNext() },
                )
            } else {
                KeyboardActions.Default
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                autoCorrectEnabled = false,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            primaryButtonLayout = if (isLoading) {
                ButtonLayout.IndeterminateProgress
            } else {
                ButtonLayout.TextOnly(text = stringResource(id = R.string.change_login_email_verification_next_button))
            },
            onPrimaryButtonClick = onNext,
            isPrimaryButtonEnabled = isContinueEnabled,
            secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(id = R.string.change_login_email_verification_help_button)),
            onSecondaryButtonClick = onHelp,
        )
    }
}

@Composable
fun EmailVerificationSuccessContent(
    modifier: Modifier = Modifier,
    isSessionValid: Boolean,
    onSuccess: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        ExpressiveIcon(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            icon = IconTokens.feedbackSuccessOutlined,
            size = ExpressiveIconSize.XLarge,
            mood = Mood.Positive,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.change_login_email_verification_success_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .semantics { heading() }
                .fillMaxWidth(),
        )
        if (!isSessionValid) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.change_login_email_verification_success_description),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = onSuccess,
            layout = if (isSessionValid) {
                ButtonLayout.TextOnly(text = stringResource(id = R.string.ok))
            } else {
                ButtonLayout.TextOnly(text = stringResource(id = R.string.change_login_email_verification_success_button))
            },
            intensity = Intensity.Catchy,
            mood = Mood.Brand,
        )
    }
}

@Composable
private fun EmailVerificationError?.toString(): String? {
    return when (this) {
        EmailVerificationError.Generic -> stringResource(R.string.error)
        EmailVerificationError.InvalidToken -> stringResource(R.string.change_login_email_verification_error_message)
        EmailVerificationError.Network -> stringResource(R.string.network_failed_notification)
        else -> null
    }
}

@Composable
fun EmailVerificationHelpAlertDialog(
    confirmButtonClick: () -> Unit,
    dismissButtonClick: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.change_login_email_verification_help_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.change_login_email_verification_help_dialog_description))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.change_login_email_verification_help_dialog_confirm_button)),
        mainActionClick = confirmButtonClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = dismissButtonClick,
        onDismissRequest = dismissButtonClick,
    )
}

@Preview
@Composable
private fun EmailVerificationContentPreview() {
    DashlanePreview {
        EmailVerificationContent(
            newEmail = "randomemail@provider.com",
            token = TextFieldValue("token"),
            isLoading = false,
            isContinueEnabled = true,
            error = null,
            onHelp = {},
            onNext = {},
            onTokenChange = {},
        )
    }
}

@Preview
@Composable
private fun EmailVerificationSuccessContentPreview() {
    DashlanePreview { EmailVerificationSuccessContent(isSessionValid = false, onSuccess = {}) }
}

@Preview
@Composable
private fun EmailVerificationSuccessContentValidSessionPreview() {
    DashlanePreview { EmailVerificationSuccessContent(isSessionValid = true, onSuccess = {}) }
}

@Preview
@Composable
private fun EmailVerificationHelpAlertDialogPreview() {
    DashlanePreview {
        EmailVerificationHelpAlertDialog(
            confirmButtonClick = {},
            dismissButtonClick = {},
        )
    }
}