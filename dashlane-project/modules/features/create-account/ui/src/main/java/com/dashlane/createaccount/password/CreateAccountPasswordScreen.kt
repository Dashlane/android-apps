package com.dashlane.createaccount.password

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.BackPressed
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.BottomSheetDismissed
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.ConfirmPasswordChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.FocusChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.NextClicked
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.PasswordChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.PasswordlessClicked
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.TipsClicked
import com.dashlane.createaccount.password.CreateAccountPasswordState.SideEffect.Cancel
import com.dashlane.createaccount.password.CreateAccountPasswordState.SideEffect.GoToBiometricSetup
import com.dashlane.createaccount.password.CreateAccountPasswordState.SideEffect.GoToPasswordless
import com.dashlane.createaccount.password.CreateAccountPasswordState.SideEffect.GoToTermsAndConditions
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.DisplayPasswordConfirm
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.DisplayTips
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.SavePassword
import com.dashlane.createaccount.ui.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Text
import com.dashlane.design.component.UiPasswordStrength
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.CreatePasswordContent
import com.dashlane.ui.common.compose.components.MasterPasswordTipsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateAccountPasswordViewModel,
    goBack: () -> Unit,
    goToBiometricSetup: () -> Unit,
    goToTermsAndConditions: () -> Unit,
    goToPasswordless: (String) -> Unit,
) {
    BackHandler {
        viewModel.sendEvent(BackPressed)
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                Cancel -> goBack()
                is GoToPasswordless -> goToPasswordless(sideEffect.email)
                GoToBiometricSetup -> goToBiometricSetup()
                GoToTermsAndConditions -> goToTermsAndConditions()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    CreateAccountPasswordContent(
        modifier = modifier,
        isB2B = uiState.data.isB2B,
        isConfirming = uiState is DisplayPasswordConfirm || uiState is SavePassword,
        isMatching = uiState.data.isMatching,
        isContinueEnabled = uiState.data.isContinueEnabled,
        password = uiState.data.password,
        passwordStrength = uiState.data.passwordStrength,
        confirmPassword = uiState.data.confirmPassword,
        sendEvent = viewModel::sendEvent,
    )

    if (uiState is DisplayTips) {
        val sheetState = rememberModalBottomSheetState(
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) viewModel.sendEvent(BottomSheetDismissed)
                true
            },
        )

        ModalBottomSheet(
            onDismissRequest = { viewModel.sendEvent(BottomSheetDismissed) },
            sheetState = sheetState,
        ) {
            MasterPasswordTipsContent()
        }
    }
}


@Suppress("LongMethod", "kotlin:S107")
@Composable
fun CreateAccountPasswordContent(
    modifier: Modifier = Modifier,
    isB2B: Boolean,
    isConfirming: Boolean,
    isMatching: Boolean,
    isContinueEnabled: Boolean,
    password: TextFieldValue,
    passwordStrength: UiPasswordStrength?,
    confirmPassword: TextFieldValue,
    sendEvent: (CreateAccountPasswordEvent) -> Unit,
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
            text = if (isConfirming) {
                stringResource(id = R.string.create_account_confirm_password_title)
            } else {
                stringResource(id = R.string.create_account_choose_password_title)
            },
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        if (isConfirming) {
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                text = stringResource(id = R.string.create_account_confirm_password_subtitle),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
            )
        } else {
            LinkButton(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                onClick = { sendEvent(TipsClicked) },
                text = stringResource(R.string.create_account_choose_password_see_tips),
                destinationType = LinkButtonDestinationType.EXTERNAL,
            )
        }
        Spacer(modifier = Modifier.weight(1.0f))

        CreatePasswordContent(
            passwordFieldLabel = stringResource(id = R.string.create_account_choose_password_hint),
            passwordFieldFeedback = stringResource(id = R.string.create_account_choose_password_tips),
            confirmPasswordFieldLabel = stringResource(id = R.string.create_account_confirm_password_hint),
            password = password,
            confirmPassword = confirmPassword,
            isConfirming = isConfirming,
            isMatching = isMatching,
            passwordStrength = passwordStrength,
            onFocusChanged = { sendEvent(FocusChanged) },
            onPasswordChanged = { sendEvent(PasswordChanged(it)) },
            onConfirmPasswordChanged = { sendEvent(ConfirmPasswordChanged(it)) },
            onNextClicked = { sendEvent(NextClicked) },
        )

        Spacer(modifier = Modifier.weight(1.0f))
        if (isB2B) {
            ButtonMedium(
                modifier = Modifier.align(Alignment.End),
                onClick = { sendEvent(NextClicked) },
                intensity = Intensity.Catchy,
                enabled = isContinueEnabled,
                layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.create_account_cta_continue)),
            )
        } else {
            ButtonMediumBar(
                primaryText = stringResource(id = R.string.create_account_cta_continue),
                secondaryText = stringResource(id = R.string.create_account_cta_passwordless),
                onPrimaryButtonClick = { sendEvent(NextClicked) },
                onSecondaryButtonClick = { sendEvent(PasswordlessClicked) },
                isPrimaryButtonEnabled = isContinueEnabled,
            )
        }
    }
}

@Preview
@Composable
private fun CreateAccountPasswordPreview() {
    DashlanePreview {
        CreateAccountPasswordContent(
            isB2B = false,
            isConfirming = false,
            isMatching = false,
            isContinueEnabled = false,
            password = TextFieldValue("password"),
            passwordStrength = UiPasswordStrength.ACCEPTABLE,
            confirmPassword = TextFieldValue(""),
            sendEvent = { },
        )
    }
}

@Preview
@Composable
private fun CreateAccountPasswordConfirmPreview() {
    DashlanePreview {
        CreateAccountPasswordContent(
            isB2B = true,
            isConfirming = true,
            isMatching = true,
            isContinueEnabled = false,
            password = TextFieldValue("password"),
            passwordStrength = UiPasswordStrength.GOOD,
            confirmPassword = TextFieldValue("confirm password"),
            sendEvent = { },
        )
    }
}