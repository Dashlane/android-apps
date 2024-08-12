package com.dashlane.accountrecoverykey.activation.intro

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.user.UserAccountInfo
import com.dashlane.accountrecoverykey.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.GenericInfoContent

@Suppress("kotlin:S1172") 
@Composable
fun AccountRecoveryKeyActivationIntroScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryKeyActivationIntroViewModel,
    onBackPressed: () -> Unit,
    onGenerateKeyClicked: () -> Unit,
    userCanExitFlow: Boolean
) {
    BackHandler {
        viewModel.onBackPressed()
        if (userCanExitFlow) onBackPressed()
    }

    val uiState by viewModel.uiState.collectAsState()

    AccountRecoveryKeyActivationIntroContent(
        modifier = modifier,
        accountType = uiState.data.accountType,
        onPrimaryButtonClicked = onGenerateKeyClicked
    )
}

@Composable
fun AccountRecoveryKeyActivationIntroContent(
    modifier: Modifier = Modifier,
    accountType: UserAccountInfo.AccountType,
    onPrimaryButtonClicked: () -> Unit
) {
    GenericInfoContent(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_recovery_key_outlined),
        title = stringResource(id = R.string.account_recovery_key_activation_intro_title),
        description = when (accountType) {
            UserAccountInfo.AccountType.InvisibleMasterPassword -> stringResource(id = R.string.account_recovery_key_activation_intro_mpless_description)
            UserAccountInfo.AccountType.MasterPassword -> stringResource(id = R.string.account_recovery_key_activation_intro_description)
        },
        textPrimary = stringResource(id = R.string.account_recovery_key_activation_intro_button),
        onClickPrimary = onPrimaryButtonClicked
    )
}

@Composable
fun SkipAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonClick: () -> Unit,
    dismissButtonClick: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.account_recovery_key_activation_skip_alert_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.account_recovery_key_activation_skip_alert_dialog_description))
        },
        onDismissRequest = onDismissRequest,
        mainActionClick = confirmButtonClick,
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_key_activation_skip_alert_dialog_positive_button)),
        additionalActionClick = dismissButtonClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_key_activation_skip_alert_dialog_negative_button))
    )
}

@Preview
@Composable
fun AccountRecoveryKeyActivationIntroContentPreview() {
    DashlanePreview {
        AccountRecoveryKeyActivationIntroContent(
            accountType = UserAccountInfo.AccountType.MasterPassword,
            onPrimaryButtonClicked = {}
        )
    }
}

@Preview
@Composable
fun SkipAlertDialogPreview() {
    DashlanePreview {
        SkipAlertDialog(
            onDismissRequest = {},
            confirmButtonClick = {},
            dismissButtonClick = {}
        )
    }
}
