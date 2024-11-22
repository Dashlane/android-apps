package com.dashlane.login.pages.token.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.util.isNotSemanticallyNull

@Composable
fun LoginTokenScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginTokenViewModel,
    goToNext: () -> Unit
) {
    val uiState by viewModel.stateFlow.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is LoginTokenState.SideEffect.Success -> goToNext()
            }
        }
    }

    LoginTokenContent(
        modifier = modifier,
        email = uiState.email,
        token = uiState.token ?: "",
        isLoading = uiState.isLoading,
        errorMessage = uiState.error?.toErrorMessage(),
        isHelpDialogDisplayed = uiState.showHelpDialog,
        isTokenError = uiState.error is LoginTokenError.InvalidToken,
        onTokenChange = viewModel::onTokenChange,
        onNext = viewModel::onNext,
        onDialogConfirmClick = viewModel::onDialogConfirmed,
        onHelpClick = viewModel::onHelpClicked,
        onDialogConfirmDismissed = viewModel::onDialogDismissed
    )
}

@Suppress("LongMethod", "kotlin:S107")
@Composable
fun LoginTokenContent(
    modifier: Modifier = Modifier,
    email: String,
    token: String,
    isLoading: Boolean,
    isHelpDialogDisplayed: Boolean,
    errorMessage: String?,
    isTokenError: Boolean,
    onTokenChange: (String) -> Unit,
    onNext: () -> Unit,
    onHelpClick: () -> Unit,
    onDialogConfirmClick: () -> Unit,
    onDialogConfirmDismissed: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            text = email,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.receive_sec_code),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 48.dp)
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!textFieldLoaded) {
                        focusRequester.requestFocus()
                        textFieldLoaded = true
                    }
                }
                .padding(top = 24.dp),
            value = token,
            onValueChange = { newValue: String -> onTokenChange(newValue.filter { char -> char.isDigit() }) },
            label = stringResource(id = R.string.login_token_text_field_label),
            isError = errorMessage != null,
            feedbackText = errorMessage,
            keyboardActions = KeyboardActions(
                onDone = { onNext() }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                autoCorrect = false
            ),
        )
        if (!isTokenError) {
            Text(
                modifier = Modifier.padding(top = 8.dp, start = 16.dp),
                text = errorMessage ?: stringResource(id = R.string.receive_sec_code_expires),
                color = if (errorMessage != null) DashlaneTheme.colors.textDangerQuiet else DashlaneTheme.colors.textNeutralQuiet,
                style = DashlaneTheme.typography.bodyHelperRegular
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            primaryButtonLayout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = stringResource(id = R.string.login_totp_next_button)),
            isPrimaryButtonEnabled = token.isNotSemanticallyNull() && !isLoading,
            onPrimaryButtonClick = onNext,
            secondaryButtonLayout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_token_where_is_cta)),
            onSecondaryButtonClick = onHelpClick
        )
    }

    if (isHelpDialogDisplayed) {
        EmailCodeHelpAlertDialog(
            confirmButtonClick = onDialogConfirmClick,
            dismissButtonClick = onDialogConfirmDismissed
        )
    }
}

@Composable
fun LoginTokenError.toErrorMessage(): String {
    return when (this) {
        LoginTokenError.InvalidToken -> stringResource(id = R.string.token_failed_resend_or_try_later)
        LoginTokenError.Network -> stringResource(id = R.string.cannot_connect_to_server)
        LoginTokenError.Offline -> stringResource(id = R.string.offline)
    }
}

@Composable
fun EmailCodeHelpAlertDialog(
    confirmButtonClick: () -> Unit,
    dismissButtonClick: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.login_token_where_is_popup_title),
        description = {
            Text(text = stringResource(id = R.string.login_token_where_is_popup_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_token_where_is_popup_resend)),
        mainActionClick = confirmButtonClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.login_token_where_is_popup_close)),
        additionalActionClick = dismissButtonClick,
        onDismissRequest = dismissButtonClick
    )
}

@Preview
@Composable
private fun LoginTokenContentPreview() {
    DashlanePreview {
        LoginTokenContent(
            email = "randomemail@provider.com",
            isLoading = true,
            token = "123",
            onTokenChange = {},
            onNext = {},
            onDialogConfirmClick = {},
            onDialogConfirmDismissed = {},
            errorMessage = null,
            isTokenError = false,
            isHelpDialogDisplayed = false,
            onHelpClick = {}
        )
    }
}

@Preview
@Composable
private fun EmailCodeHelpAlertDialogPreview() {
    DashlaneTheme {
        EmailCodeHelpAlertDialog(
            confirmButtonClick = { },
            dismissButtonClick = {}
        )
    }
}
