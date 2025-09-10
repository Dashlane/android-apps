package com.dashlane.changemasterpassword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.changemasterpassword.ui.R
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Text
import com.dashlane.design.component.UiPasswordStrength
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.CreatePasswordContent
import com.dashlane.ui.common.compose.components.MasterPasswordTipsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeMasterPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: ChangeMasterPasswordViewModel,
    origin: ChangeMasterPasswordOrigin,
    hasSteps: Boolean = true,
    goToNext: () -> Unit,
    goBack: () -> Unit,
) {
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(origin)
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                ChangeMasterPasswordState.SideEffect.Finish -> goToNext()
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
        onTipsClicked = viewModel::tipsClicked,
        onFocusChange = viewModel::onFocusChanged,
    )

    if (uiState.isTipsShown) {
        val sheetState = rememberModalBottomSheetState(
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                true
            },
        )

        ModalBottomSheet(
            onDismissRequest = viewModel::bottomSheetDismissed,
            sheetState = sheetState,
        ) {
            MasterPasswordTipsContent()
        }
    }
}

@Suppress("LongMethod", "kotlin:S107") 
@Composable
fun ChangeMasterPasswordContent(
    modifier: Modifier = Modifier,
    isConfirming: Boolean,
    isMatching: Boolean,
    nextIsEnabled: Boolean,
    hasSteps: Boolean,
    password: TextFieldValue,
    passwordStrength: UiPasswordStrength?,
    onPasswordChange: (TextFieldValue) -> Unit,
    confirmPassword: TextFieldValue,
    onConfirmPasswordChange: (TextFieldValue) -> Unit,
    onNextClicked: () -> Unit,
    onFocusChange: () -> Unit,
    onTipsClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        if (hasSteps) {
            Text(
                text = stringResource(id = R.string.change_master_password_create_step, if (isConfirming) 3 else 2, 3),
                style = DashlaneTheme.typography.bodyHelperRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        Text(
            text = if (isConfirming) {
                stringResource(
                    id = R.string.change_master_password_ark_confirm_title,
                )
            } else {
                stringResource(id = R.string.change_master_password_ark_create_title)
            },
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        Text(
            text = if (isConfirming) {
                stringResource(
                    id = R.string.change_master_password_confirm_description,
                )
            } else {
                stringResource(id = R.string.change_master_password_create_description)
            },
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier.padding(bottom = 32.dp),
        )
        CreatePasswordContent(
            passwordFieldLabel = stringResource(id = R.string.change_master_password_create_text_edit_label),
            passwordFieldFeedback = stringResource(id = R.string.change_master_password_create_text_edit_feedback),
            confirmPasswordFieldLabel = stringResource(id = R.string.change_master_password_confirm_text_edit_label),
            password = password,
            confirmPassword = confirmPassword,
            isConfirming = isConfirming,
            isMatching = isMatching,
            passwordStrength = passwordStrength,
            onFocusChanged = onFocusChange,
            onPasswordChanged = onPasswordChange,
            onConfirmPasswordChanged = onConfirmPasswordChange,
            onNextClicked = onNextClicked,
        )
        Spacer(modifier = Modifier.weight(1.0f))
        ButtonMediumBar(
            primaryText = stringResource(id = R.string.change_master_password_create_button_next),
            secondaryText = stringResource(id = R.string.change_master_password_create_button_tips),
            onPrimaryButtonClick = onNextClicked,
            onSecondaryButtonClick = onTipsClicked,
            isPrimaryButtonEnabled = nextIsEnabled,
        )
    }
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
            password = TextFieldValue("password"),
            passwordStrength = UiPasswordStrength.ACCEPTABLE,
            onPasswordChange = { },
            confirmPassword = TextFieldValue("confirm password"),
            onConfirmPasswordChange = { },
            onNextClicked = {},
            onTipsClicked = {},
            onFocusChange = {},
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
            password = TextFieldValue("password"),
            passwordStrength = UiPasswordStrength.ACCEPTABLE,
            onPasswordChange = { },
            confirmPassword = TextFieldValue("confirm password"),
            onConfirmPasswordChange = { },
            onNextClicked = {},
            onTipsClicked = {},
            onFocusChange = {},
        )
    }
}