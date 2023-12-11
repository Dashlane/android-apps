package com.dashlane.masterpassword.compose

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.PasswordFieldFeedback
import com.dashlane.design.component.PasswordStrengthIndicator
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.TextFieldAction
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.masterpassword.tips.MasterPasswordTipsActivity

@Composable
fun ChangeMasterPasswordScreen(
    viewModel: ChangeMasterPasswordViewModel,
    goToNext: (ObfuscatedByteArray) -> Unit,
    goBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ChangeMasterPasswordState.Finish -> {
                viewModel.hasNavigated()
                goToNext(state.newMasterPassword)
            }
            is ChangeMasterPasswordState.NavigateBack -> goBack()
            else -> Unit
        }
    }

    ChangeMasterPasswordContent(
        isConfirming = uiState.data.isConfirming,
        isMatching = uiState.data.isMatching,
        nextIsEnabled = uiState.data.isNextEnabled,
        password = uiState.data.password,
        passwordStrength = uiState.data.passwordStrength,
        onPasswordChange = viewModel::onPasswordChange,
        confirmPassword = uiState.data.confirmPassword,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onNextClicked = viewModel::onNextClicked,
        onTipsClicked = {
            val tipsIntent = Intent(context, MasterPasswordTipsActivity::class.java)
            context.startActivity(tipsIntent)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
fun ChangeMasterPasswordContent(
    modifier: Modifier = Modifier.semantics {
        testTagsAsResourceId = true
    },
    isConfirming: Boolean,
    isMatching: Boolean,
    nextIsEnabled: Boolean,
    password: String,
    passwordStrength: PasswordStrengthIndicator.Strength?,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onNextClicked: () -> Unit,
    onTipsClicked: () -> Unit
) {
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
            Text(
                text = stringResource(id = R.string.login_account_recovery_key_enter_step, if (isConfirming) 3 else 2, 3),
                style = DashlaneTheme.typography.bodyHelperRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = if (isConfirming) stringResource(id = R.string.change_master_password_ark_confirm_title) else stringResource(id = R.string.change_master_password_ark_create_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            Text(
                text = if (isConfirming) stringResource(id = R.string.change_master_password_confirm_description) else stringResource(id = R.string.change_master_password_create_description),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            PasswordField(
                modifier = modifier
                    .testTag("newMP")
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .focusRequester(focusRequester)
                    .onGloballyPositioned { focusRequester.requestFocus() },
                value = password,
                onValueChange = onPasswordChange,
                label = stringResource(id = R.string.change_master_password_create_text_edit_label),
                placeholder = stringResource(id = R.string.change_master_password_create_text_edit_placeholder),
                enabled = !isConfirming,
                actions = passwordFieldActions(),
                feedback = passwordStrength
                    ?.let { PasswordFieldFeedback.Strength(it) }
                    ?: PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_create_text_edit_feedback)),
            )
            if (isConfirming) {
                PasswordField(
                    modifier = modifier
                        .testTag("confirmNewMP")
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned { focusRequester.requestFocus() },
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = stringResource(id = R.string.change_master_password_confirm_text_edit_label),
                    placeholder = stringResource(id = R.string.change_master_password_confirm_text_edit_placeholder),
                    actions = passwordFieldActions(),
                    feedback = confirmPasswordFeedback(confirmPassword, isMatching),
                    isError = !isMatching && confirmPassword.isNotEmpty()
                )
            }
            Spacer(modifier = Modifier.weight(1.0f))
        }
        ButtonMediumBar(
            primaryText = stringResource(id = R.string.change_master_password_create_button_next),
            secondaryText = stringResource(id = R.string.change_master_password_create_button_tips),
            onPrimaryButtonClick = onNextClicked,
            onSecondaryButtonClick = onTipsClicked,
            isPrimaryButtonEnabled = nextIsEnabled
        )
    }
}

@Composable
private fun passwordFieldActions(): TextFieldActions.Password = TextFieldActions.Password(
    hideRevealAction = TextFieldAction.HideReveal(
        contentDescriptionToHide = stringResource(id = R.string.and_accessibility_text_edit_hide),
        contentDescriptionToReveal = stringResource(id = R.string.and_accessibility_text_edit_reveal),
        onClick = { }
    ),
    genericAction = null,
    passwordGeneratorAction = null
)

@Composable
private fun confirmPasswordFeedback(confirmPassword: String, isMatching: Boolean): PasswordFieldFeedback? = when {
    confirmPassword.isEmpty() -> null
    isMatching -> PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_confirm_text_edit_feedback_matching))
    else -> PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_confirm_text_edit_feedback_error))
}

@Preview(showBackground = true)
@Composable
fun ChangeMasterPasswordContentPreview() {
    DashlanePreview {
        ChangeMasterPasswordContent(
            isConfirming = false,
            isMatching = false,
            nextIsEnabled = false,
            password = "password",
            passwordStrength = PasswordStrengthIndicator.Strength.ACCEPTABLE,
            onPasswordChange = { },
            confirmPassword = "confirm password",
            onConfirmPasswordChange = { },
            onNextClicked = {},
            onTipsClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeMasterPasswordContentConfirmingPreview() {
    DashlanePreview {
        ChangeMasterPasswordContent(
            isConfirming = true,
            isMatching = true,
            nextIsEnabled = true,
            password = "password",
            passwordStrength = PasswordStrengthIndicator.Strength.ACCEPTABLE,
            onPasswordChange = { },
            confirmPassword = "confirm password",
            onConfirmPasswordChange = { },
            onNextClicked = {},
            onTipsClicked = {}
        )
    }
}
