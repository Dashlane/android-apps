package com.dashlane.login.pages.password.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.DropdownField
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownItemContent
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.PasswordFieldFeedback
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.lock.LockPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyActivity
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.compose.passwordFieldActions
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.showToaster

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun LoginPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginPasswordViewModel,
    lockSetting: LockSetting,
    onSuccess: (LoginStrategy.Strategy, SsoInfo?) -> Unit,
    onCancel: () -> Unit,
    onFallback: () -> Unit,
    biometricRecovery: () -> Unit,
    changeAccount: (String) -> Unit,
    logout: (String?) -> Unit,
) {
    val context = LocalContext.current
    val goToArk = rememberLauncherForActivityResult(ArkContract(), onResult = viewModel::arkComplete)

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(lockSetting)
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                LoginPasswordState.SideEffect.Cancel -> onCancel()
                LoginPasswordState.SideEffect.Fallback -> onFallback()
                is LoginPasswordState.SideEffect.GoToARK -> goToArk.launch(Pair(state.registeredUserDevice, state.authTicket))
                is LoginPasswordState.SideEffect.LoginSuccess -> onSuccess(state.strategy, state.ssoInfo)
                LoginPasswordState.SideEffect.GoToCannotLoginHelp -> {
                    context.safelyStartBrowserActivity(HelpCenterLink.ARTICLE_CANNOT_LOGIN.newIntent(context = context))
                }
                LoginPasswordState.SideEffect.GoToForgotMPHelp -> {
                    context.safelyStartBrowserActivity(HelpCenterLink.ARTICLE_FORGOT_PASSWORD.newIntent(context = context))
                }
                LoginPasswordState.SideEffect.GoToBiometricRecovery -> biometricRecovery()
                is LoginPasswordState.SideEffect.ChangeAccount -> changeAccount(state.email)
                is LoginPasswordState.SideEffect.Logout -> {
                    val message = when (state.error) {
                        LoginPasswordError.InvalidCredentials -> context.getString(R.string.forced_logout)
                        LoginPasswordError.TooManyInvalidPassword -> context.getString(R.string.lock_force_logout_password_incorrect_too_much)
                        else -> null
                    }
                    message?.let { (context as? Activity)?.showToaster(it, Toast.LENGTH_LONG) }
                    logout(state.email)
                }
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    val title = when {
        lockSetting.isShowMPForRemember -> stringResource(id = R.string.login_enter_mp_remember_title)
        lockSetting.lockPrompt is LockPrompt.ForSettings -> stringResource(id = R.string.please_enter_master_password_to_edit_settings)
        lockSetting.lockPrompt is LockPrompt.ForItem -> {
            if ((lockSetting.lockPrompt as LockPrompt.ForItem).isSecureNote) {
                stringResource(id = R.string.unlock_message_secure_note_master_password)
            } else {
                stringResource(id = R.string.unlock_message_item_master_password)
            }
        }
        lockSetting.isLoggedIn -> stringResource(id = R.string.enter_masterpassword)
        else -> null
    }

    LoginPasswordContent(
        modifier = modifier,
        email = uiState.email ?: "",
        title = title,
        password = uiState.password,
        isLoading = uiState.isLoading,
        isUnlock = uiState.lockSetting?.isLoggedIn == true,
        isCancellable = uiState.lockSetting?.isLockCancelable == true,
        isShowMPForRemember = uiState.lockSetting?.isShowMPForRemember == true,
        error = uiState.error,
        loginHistory = uiState.loginHistory,
        changeAccount = viewModel::changeAccount,
        onPasswordChange = viewModel::passwordChanged,
        onClickPrimary = viewModel::login,
        onClickCancel = viewModel::cancel,
        onClickForgot = viewModel::forgot
    )

    if (uiState.recoveryDialogShown || uiState.helpDialogShown) {
        val sheetState = rememberModalBottomSheetState { sheetValue ->
            if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
            true
        }
        when {
            uiState.recoveryDialogShown -> LoginBottomSheet(
                title = stringResource(id = R.string.login_password_dialog_trouble_login_title),
                firstButtonText = stringResource(id = R.string.login_password_dialog_trouble_biometric_button),
                secondButtonText = stringResource(id = R.string.login_password_dialog_trouble_recovery_key_button),
                sheetState = sheetState,
                onClickFirstButton = viewModel::biometricRecovery,
                onClickSecondButton = viewModel::ark,
                bottomSheetDismissed = viewModel::bottomSheetDismissed
            )
            uiState.helpDialogShown -> LoginBottomSheet(
                title = stringResource(id = R.string.trouble_logging_in),
                firstButtonText = stringResource(id = R.string.login_cannot_login),
                secondButtonText = stringResource(id = R.string.login_forgot_password),
                sheetState = sheetState,
                onClickFirstButton = viewModel::cannotLogin,
                onClickSecondButton = viewModel::forgotMP,
                bottomSheetDismissed = viewModel::bottomSheetDismissed
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
fun LoginPasswordContent(
    modifier: Modifier = Modifier,
    email: String,
    title: String?,
    password: TextFieldValue,
    isLoading: Boolean,
    isUnlock: Boolean,
    isCancellable: Boolean,
    isShowMPForRemember: Boolean,
    error: LoginPasswordError?,
    loginHistory: List<String>,
    changeAccount: (String?) -> Unit,
    onPasswordChange: (TextFieldValue) -> Unit,
    onClickPrimary: () -> Unit,
    onClickCancel: () -> Unit,
    onClickForgot: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }
    val passwordObfuscatedState = rememberSaveable { mutableStateOf(true) }
    val primaryButtonText = when {
        isUnlock -> stringResource(id = R.string.fragment_lock_master_password_button_unlock)
        else -> stringResource(id = R.string.login_password_page_login_button)
    }
    val cancelButton = when {
        isShowMPForRemember -> stringResource(id = R.string.login_enter_mp_later)
        isCancellable -> stringResource(id = R.string.cancel)
        else -> null
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = DashlaneTheme.typography.titleSectionMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
            )
        } else {
            DashlaneLogoLockup(height = 40.dp)
        }
        if (loginHistory.isNotEmpty()) {
            LoginPasswordSwitchAccount(email = email, loginHistory = loginHistory, changeAccount)
        } else {
            Text(
                text = email,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier
                    .padding(top = 24.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PasswordField(
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!textFieldLoaded) {
                        focusRequester.requestFocus()
                        textFieldLoaded = true
                    }
                }
                .padding(top = 64.dp, bottom = 8.dp),
            value = password,
            onValueChange = onPasswordChange,
            isError = error != null,
            obfuscated = passwordObfuscatedState.value,
            feedback = error?.toErrorMessage()?.let { PasswordFieldFeedback.Text(it) },
            label = stringResource(id = R.string.master_password),
            actions = passwordFieldActions(obfuscatedState = passwordObfuscatedState),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            onClick = onClickForgot,
            intensity = Intensity.Supershy,
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_password_forgot_password_button))
        )
        Spacer(modifier = Modifier.weight(1f))
        if (cancelButton != null) {
            ButtonMediumBar(
                primaryButtonLayout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = primaryButtonText),
                isPrimaryButtonEnabled = password.text.isNotSemanticallyNull() && !isLoading,
                onPrimaryButtonClick = onClickPrimary,
                secondaryButtonLayout = ButtonLayout.TextOnly(text = cancelButton),
                onSecondaryButtonClick = onClickCancel
            )
        } else {
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.End)
                    .widthIn(min = 80.dp),
                onClick = onClickPrimary,
                intensity = Intensity.Catchy,
                enabled = password.text.isNotSemanticallyNull() && !isLoading,
                layout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = primaryButtonText)
            )
        }
    }
}

@Composable
fun LoginPasswordSwitchAccount(email: String, loginHistory: List<String>, changeAccount: (String?) -> Unit) {
    DropdownField(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(),
        label = stringResource(id = R.string.login_password_switch_account_label),
        value = email
    ) {
        loginHistory.forEach { login ->
            DropdownItem(
                content = { DropdownItemContent(leadingIcon = null, text = login) },
                onClick = { changeAccount(login) }
            )
        }
        DropdownItem(
            content = { DropdownItemContent(leadingIcon = null, text = stringResource(id = R.string.login_change_account)) },
            onClick = { changeAccount(null) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    title: String,
    firstButtonText: String,
    secondButtonText: String,
    sheetState: SheetState,
    onClickFirstButton: () -> Unit,
    onClickSecondButton: () -> Unit,
    bottomSheetDismissed: () -> Unit
) {
    val configuration = LocalConfiguration.current
    BoxWithConstraints {
        ModalBottomSheet(
            modifier = Modifier
                .width(maxWidth),
            onDismissRequest = bottomSheetDismissed,
            sheetMaxWidth = maxWidth,
            sheetState = sheetState,
            containerColor = DashlaneTheme.colors.backgroundDefault,
            
            windowInsets = WindowInsets(right = Dp(configuration.screenWidthDp - maxWidth.value))
        ) {
            HelpBottomSheetContent(
                title = title,
                firstButtonText = firstButtonText,
                secondButtonText = secondButtonText,
                onClickFirstButton = onClickFirstButton,
                onClickSecondButton = onClickSecondButton
            )
        }
    }
}

@Composable
fun HelpBottomSheetContent(
    title: String,
    firstButtonText: String,
    secondButtonText: String,
    onClickFirstButton: () -> Unit,
    onClickSecondButton: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 64.dp)
    ) {
        Text(
            text = title,
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp),
            onClick = onClickFirstButton,
            intensity = Intensity.Supershy,
            mood = Mood.Neutral,
            layout = ButtonLayout.IconLeading(
                iconToken = IconTokens.feedbackHelpOutlined,
                text = firstButtonText
            )
        )
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp),
            onClick = onClickSecondButton,
            intensity = Intensity.Supershy,
            mood = Mood.Neutral,
            layout = ButtonLayout.IconLeading(
                iconToken = IconTokens.feedbackHelpOutlined,
                text = secondButtonText
            )
        )
    }
}

@Preview
@Composable
private fun LoginPasswordContentPreview() {
    DashlanePreview {
        LoginPasswordContent(
            email = "randomemail@provider.com",
            title = stringResource(id = R.string.enter_masterpassword),
            password = TextFieldValue("password"),
            isLoading = true,
            isUnlock = true,
            isCancellable = true,
            isShowMPForRemember = false,
            error = LoginPasswordError.EmptyPassword,
            loginHistory = listOf("randomemail@provider.com"),
            changeAccount = {},
            onPasswordChange = {},
            onClickPrimary = {},
            onClickCancel = {},
            onClickForgot = {},
        )
    }
}

@Preview
@Composable
private fun HelpBottomSheetContentPreview() {
    DashlanePreview {
        HelpBottomSheetContent(
            title = stringResource(id = R.string.trouble_logging_in),
            firstButtonText = stringResource(id = R.string.login_cannot_login),
            secondButtonText = stringResource(id = R.string.login_forgot_password),
            onClickFirstButton = { },
            onClickSecondButton = { }
        )
    }
}

@Composable
private fun LoginPasswordError.toErrorMessage(): String? {
    return when (this) {
        LoginPasswordError.EmptyPassword -> stringResource(id = R.string.password_empty)
        LoginPasswordError.Generic -> stringResource(id = R.string.error)
        LoginPasswordError.InvalidPassword -> stringResource(id = R.string.password_is_not_correct_please_try_again)
        else -> null
    }
}

class ArkContract : ActivityResultContract<Pair<RegisteredUserDevice, String?>, String?>() {
    override fun createIntent(context: Context, input: Pair<RegisteredUserDevice, String?>): Intent {
        return LoginAccountRecoveryKeyActivity.newIntent(context)
            .apply {
                putExtra(LoginAccountRecoveryKeyActivity.EXTRA_REGISTERED_USER_DEVICE, input.first)
                putExtra(LoginAccountRecoveryKeyActivity.ACCOUNT_TYPE, UserAccountInfo.AccountType.MasterPassword.toString())
                putExtra(LoginAccountRecoveryKeyActivity.AUTH_TICKET, input.second)
            }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        return intent.extras?.let {
            val password =
                BundleCompat.getParcelable(it, LoginAccountRecoveryKeyActivity.ACCOUNT_RECOVERY_PASSWORD_RESULT, ObfuscatedByteArray::class.java)
            password?.decodeUtf8ToString()
        }
    }
}
