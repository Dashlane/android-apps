package com.dashlane.login.pages.pin.compose

import android.content.res.Configuration
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.pages.password.compose.HelpBottomSheetContent
import com.dashlane.pin.setup.SystemLockSetupDialog
import com.dashlane.ui.common.compose.components.pincode.PinKeyboard
import com.dashlane.ui.common.compose.components.pincode.PinTextField
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.animation.shake
import com.dashlane.util.getBaseActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPinScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginPinViewModel,
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    onSuccess: () -> Unit,
    onCancel: (LoginPinFallback) -> Unit,
    onLogout: (String?) -> Unit,
    goToRecovery: (String) -> Unit,
    goToSecretTransfer: (String) -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel) {
        viewModel.viewStarted(userAccountInfo, lockSetting)
        viewModel.navigationState.collect { state ->
            when (state) {
                is LoginPinNavigationState.Cancel -> onCancel(state.fallback)
                is LoginPinNavigationState.GoToRecoveryHelp -> goToRecovery(state.email)
                is LoginPinNavigationState.GoToSecretTransfer -> goToSecretTransfer(state.email)
                is LoginPinNavigationState.GoToSystemLockSetting -> context.getBaseActivity()?.startActivity(state.intent)
                is LoginPinNavigationState.Logout -> {
                    context.getBaseActivity()?.let { activity ->
                        SnackbarUtils.showSnackbar(activity, context.getString(R.string.lock_pincode_force_logout_pin_missed_too_much))
                    }
                    onLogout(state.email)
                }
                LoginPinNavigationState.UnlockSuccess -> onSuccess()
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginPinContent(
        modifier = modifier,
        email = userAccountInfo.username,
        pinCode = uiState.pinCode ?: "",
        attempt = (uiState.error as? LoginPinError.WrongPin)?.attempt,
        fallback = uiState.fallback.toText(),
        isError = uiState.error != null,
        onPinCodeChange = viewModel::onPinUpdated,
        onClickForgot = viewModel::onClickForgot
    )

    if (!uiState.isSystemLockSetup) {
        SystemLockSetupDialog(onConfirm = viewModel::onGoToSystemLockSetting)
    }

    if (uiState.helpDialogShown) {
        val sheetState = rememberModalBottomSheetState(
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                true
            }
        )

        LoginPinBottomSheet(
            title = stringResource(id = R.string.passwordless_pin_error_title),
            firstButtonText = stringResource(id = R.string.passwordless_pin_forgot_use_another_device),
            secondButtonText = stringResource(id = R.string.passwordless_pin_forgot_use_recovery_method),
            sheetState = sheetState,
            onClickFirstButton = viewModel::onClickD2D,
            onClickSecondButton = viewModel::onClickRecovery,
            bottomSheetDismissed = viewModel::bottomSheetDismissed
        )
    }
}

@Composable
@Suppress("LongMethod")
@VisibleForTesting
fun LoginPinContent(
    modifier: Modifier = Modifier.fillMaxSize(),
    email: String,
    pinCode: String,
    attempt: Int?,
    fallback: String,
    isError: Boolean,
    onPinCodeChange: (String) -> Unit,
    onClickForgot: () -> Unit
) {
    val orientation = LocalConfiguration.current.orientation
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(id = R.string.enter_pin),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = attempt?.let { pluralStringResource(id = R.plurals.failed_attempt, count = it, it) } ?: email,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        PinTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp, bottom = 16.dp)
                .shake(isError),
            value = pinCode,
            onValueChange = onPinCodeChange,
            isError = isError,
            errorMessage = null
        )
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            PinKeyboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp),
                value = pinCode,
                onValueChange = onPinCodeChange
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = onClickForgot,
            intensity = Intensity.Supershy,
            layout = ButtonLayout.TextOnly(text = fallback)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginPinBottomSheet(
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
fun LoginPinFallback.toText() = when (this) {
    is LoginPinFallback.Cancellable -> stringResource(id = R.string.cancel)
    is LoginPinFallback.MPLess -> stringResource(id = R.string.passwordless_pin_error_button)
    is LoginPinFallback.SSO -> stringResource(id = R.string.sso_lock_use_sso)
    else -> stringResource(id = R.string.fragment_lock_pin_button_use_master_password)
}

@Preview
@Composable
private fun PinScreenPreview() {
    DashlanePreview {
        LoginPinContent(
            email = "randomemail@provider.com",
            pinCode = "000",
            attempt = 2,
            fallback = stringResource(id = R.string.cancel),
            isError = false,
            onPinCodeChange = {},
            onClickForgot = {}
        )
    }
}