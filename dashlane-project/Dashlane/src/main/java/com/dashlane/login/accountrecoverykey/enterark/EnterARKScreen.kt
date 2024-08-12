package com.dashlane.login.accountrecoverykey.enterark

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.CircularProgressIndicator
import com.dashlane.util.compose.GenericSeparatorVisualTransformation

@Composable
fun EnterARKScreen(
    viewModel: EnterARKViewModel,
    onSuccess: (ObfuscatedByteArray) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EnterARKState.KeyConfirmed -> {
                viewModel.hasNavigated()
                onSuccess(state.obfuscatedVaultKey)
            }

            else -> Unit
        }
    }

    EnterARKContent(
        isLoading = uiState is EnterARKState.Loading,
        isError = uiState is EnterARKState.Error,
        accountRecoveryKey = "",
        accountType = uiState.data.accountType,
        onNextClicked = { accountRecoveryKey ->
            viewModel.onNextClicked(accountRecoveryKey)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
fun EnterARKContent(
    modifier: Modifier = Modifier.semantics {
        testTagsAsResourceId = true
    },
    isLoading: Boolean,
    isError: Boolean,
    accountRecoveryKey: String,
    accountType: UserAccountInfo.AccountType,
    onNextClicked: (String) -> Unit
) {
    var value by rememberSaveable { mutableStateOf(accountRecoveryKey) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            if (accountType is UserAccountInfo.AccountType.MasterPassword) {
                Text(
                    text = stringResource(id = R.string.login_account_recovery_key_enter_step, 1, 3),
                    style = DashlaneTheme.typography.titleSupportingSmall,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    modifier = Modifier
                        .padding(top = 16.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.login_account_recovery_key_enter_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
            )
            Text(
                text = when (accountType) {
                    UserAccountInfo.AccountType.InvisibleMasterPassword -> stringResource(id = R.string.login_account_recovery_key_enter_mpless_description)
                    UserAccountInfo.AccountType.MasterPassword -> stringResource(id = R.string.login_account_recovery_key_enter_description)
                },
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            )
            TextField(
                modifier = modifier
                    .testTag("recoveryKeyTextField")
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .focusRequester(focusRequester)
                    .onGloballyPositioned { focusRequester.requestFocus() },
                value = value,
                isError = isError,
                feedbackText = if (isError) stringResource(id = R.string.login_account_recovery_key_confirm_error) else null,
                keyboardOptions = KeyboardOptions(autoCorrect = false, capitalization = KeyboardCapitalization.Characters),
                visualTransformation = remember { GenericSeparatorVisualTransformation(4, '-') },
                onValueChange = { newValue -> value = newValue.filter { char -> char.isLetterOrDigit() } },
                label = stringResource(id = R.string.login_account_recovery_key_enter_label)
            )
            Spacer(modifier = Modifier.weight(1.0f))
        }
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { onNextClicked(value) },
            intensity = Intensity.Catchy,
            enabled = value.isNotEmpty(),
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.login_account_recovery_key_enter_button)
            )
        )
    }

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun EnterARKContentPreview() {
    DashlanePreview {
        EnterARKContent(
            isLoading = true,
            isError = true,
            accountRecoveryKey = "accountRecoveryKey",
            accountType = UserAccountInfo.AccountType.MasterPassword,
            onNextClicked = {}
        )
    }
}
