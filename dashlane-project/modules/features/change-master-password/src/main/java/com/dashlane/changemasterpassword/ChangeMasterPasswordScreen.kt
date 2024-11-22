package com.dashlane.changemasterpassword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.changemasterpassword.tips.MasterPasswordTipsContent
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.PasswordFieldFeedback
import com.dashlane.design.component.PasswordStrengthIndicator
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.util.compose.passwordFieldActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeMasterPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangeMasterPasswordViewModel,
    hasSteps: Boolean = true,
    goToNext: (ObfuscatedByteArray) -> Unit,
    goBack: () -> Unit
) {
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is ChangeMasterPasswordState.SideEffect.Finish -> goToNext(state.newMasterPassword)
                ChangeMasterPasswordState.SideEffect.NavigateBack -> goBack()
            }
        }
    }

    ChangeMasterPasswordContent(
        modifier = modifier,
        isConfirming = uiState.isConfirming,
        isMatching = uiState.isMatching,
        nextIsEnabled = uiState.isNextEnabled,
        hasSteps = hasSteps,
        password = uiState.password,
        passwordStrength = uiState.passwordStrength,
        onPasswordChange = viewModel::onPasswordChange,
        confirmPassword = uiState.confirmPassword,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onNextClicked = viewModel::onNextClicked,
        onTipsClicked = viewModel::tipsClicked
    )

    if (uiState.isTipsShown) {
        val sheetState = rememberModalBottomSheetState(
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                true
            }
        )

        ModalBottomSheet(
            containerColor = DashlaneTheme.colors.backgroundDefault,
            onDismissRequest = viewModel::bottomSheetDismissed,
            sheetState = sheetState
        ) {
            MasterPasswordTipsContent()
        }
    }
}

@Suppress("LongMethod")
@Composable
fun ChangeMasterPasswordContent(
    modifier: Modifier = Modifier,
    isConfirming: Boolean,
    isMatching: Boolean,
    nextIsEnabled: Boolean,
    hasSteps: Boolean,
    password: String,
    passwordStrength: PasswordStrengthIndicator.Strength?,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onNextClicked: () -> Unit,
    onTipsClicked: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val modifierWithoutFocus = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    val modifierWithFocus = modifierWithoutFocus
        .focusRequester(focusRequester)
        .onGloballyPositioned { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        if (hasSteps) {
            Text(
                text = stringResource(id = R.string.change_master_password_create_step, if (isConfirming) 3 else 2, 3),
                style = DashlaneTheme.typography.bodyHelperRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
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
        val newMPObfuscatedState = rememberSaveable { mutableStateOf(true) }
        PasswordField(
            modifier = if (isConfirming) modifierWithoutFocus else modifierWithFocus,
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(id = R.string.change_master_password_create_text_edit_label),
            placeholder = stringResource(id = R.string.change_master_password_create_text_edit_placeholder),
            readOnly = isConfirming,
            obfuscated = newMPObfuscatedState.value,
            actions = passwordFieldActions(obfuscatedState = newMPObfuscatedState),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            feedback = passwordStrength
                ?.let { PasswordFieldFeedback.Strength(it) }
                ?: PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_create_text_edit_feedback)),
        )
        if (isConfirming) {
            val confirmationObfuscatedState = rememberSaveable { mutableStateOf(true) }
            PasswordField(
                modifier = modifierWithFocus,
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(id = R.string.change_master_password_confirm_text_edit_label),
                placeholder = stringResource(id = R.string.change_master_password_confirm_text_edit_placeholder),
                obfuscated = confirmationObfuscatedState.value,
                actions = passwordFieldActions(obfuscatedState = confirmationObfuscatedState),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                feedback = confirmPasswordFeedback(confirmPassword, isMatching),
                isError = !isMatching && confirmPassword.isNotEmpty()
            )
        }
        Spacer(modifier = Modifier.weight(1.0f))
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
private fun confirmPasswordFeedback(
    confirmPassword: String,
    isMatching: Boolean
): PasswordFieldFeedback? = when {
    confirmPassword.isEmpty() -> null
    isMatching -> PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_confirm_text_edit_feedback_matching))
    else -> PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_confirm_text_edit_feedback_error))
}

@Preview
@Composable
private fun ChangeMasterPasswordContentPreview() {
    DashlanePreview {
        ChangeMasterPasswordContent(
            isConfirming = false,
            isMatching = false,
            nextIsEnabled = false,
            hasSteps = true,
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

@Preview
@Composable
private fun ChangeMasterPasswordContentConfirmingPreview() {
    DashlanePreview {
        ChangeMasterPasswordContent(
            isConfirming = true,
            isMatching = true,
            nextIsEnabled = true,
            hasSteps = false,
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
