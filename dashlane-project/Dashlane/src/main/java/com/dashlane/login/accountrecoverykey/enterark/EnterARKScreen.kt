package com.dashlane.login.accountrecoverykey.enterark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.accountrecoverykey.activation.confirm.RecoveryKeyTextField
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.user.UserAccountInfo

@Composable
fun EnterARKScreen(
    modifier: Modifier = Modifier,
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
        modifier = modifier,
        isLoading = uiState is EnterARKState.Loading,
        isError = uiState is EnterARKState.Error,
        accountRecoveryKey = "",
        accountType = uiState.data.accountType,
        onNextClicked = { accountRecoveryKey ->
            viewModel.onNextClicked(accountRecoveryKey)
        },
    )
}

@Suppress("LongMethod")
@Composable
fun EnterARKContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isError: Boolean,
    accountRecoveryKey: String,
    accountType: UserAccountInfo.AccountType,
    onNextClicked: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(accountRecoveryKey) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        if (accountType is UserAccountInfo.AccountType.MasterPassword) {
            Text(
                text = stringResource(id = R.string.login_account_recovery_key_enter_step, 1, 3),
                style = DashlaneTheme.typography.titleSupportingSmall,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier
                    .padding(top = 16.dp),
            )
        }
        Text(
            text = stringResource(id = R.string.login_account_recovery_key_enter_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp),
        )
        Text(
            text = when (accountType) {
                UserAccountInfo.AccountType.InvisibleMasterPassword -> stringResource(
                    id = R.string.login_account_recovery_key_enter_mpless_description,
                )
                UserAccountInfo.AccountType.MasterPassword -> stringResource(id = R.string.login_account_recovery_key_enter_description)
            },
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 32.dp),
        )
        RecoveryKeyTextField(
            label = stringResource(id = R.string.login_account_recovery_key_enter_label),
            value = value,
            isError = isError,
            onValueChange = { value = it },
        )
        Spacer(modifier = Modifier.weight(1.0f))

        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { onNextClicked(value) },
            intensity = Intensity.Catchy,
            enabled = value.isNotEmpty(),
            layout = if (isLoading) {
                ButtonLayout.IndeterminateProgress
            } else {
                ButtonLayout.TextOnly(
                    text = stringResource(id = R.string.login_account_recovery_key_enter_button),
                )
            },
        )
    }
}

@Preview
@Composable
private fun EnterARKContentPreview() {
    DashlanePreview {
        EnterARKContent(
            isLoading = true,
            isError = true,
            accountRecoveryKey = "accountRecoveryKey",
            accountType = UserAccountInfo.AccountType.MasterPassword,
            onNextClicked = {},
        )
    }
}