package com.dashlane.biometric

import androidx.biometric.BiometricPrompt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.biometric.BiometricSetupError.BiometricNotRegistered
import com.dashlane.biometric.BiometricSetupError.RequireStrongerBiometric
import com.dashlane.biometric.BiometricSetupEvent.AuthenticationError
import com.dashlane.biometric.BiometricSetupEvent.AuthenticationFailed
import com.dashlane.biometric.BiometricSetupEvent.AuthenticationSuccess
import com.dashlane.biometric.BiometricSetupEvent.BiometricSetupPositive
import com.dashlane.biometric.BiometricSetupEvent.DialogDismissed
import com.dashlane.biometric.BiometricSetupEvent.DialogPositive
import com.dashlane.biometric.BiometricSetupEvent.RecoverySetupPositive
import com.dashlane.biometric.BiometricSetupEvent.ViewResumed
import com.dashlane.biometric.ui.R
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.BottomSheetDefaults
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.common.compose.components.BiometricLockPrompt
import com.dashlane.util.getBaseActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod", "kotlin:S3776")
fun BiometricSetupScreen(
    viewModel: BiometricSetupViewModel,
    showToast: (String) -> Unit,
    onCancel: () -> Unit,
    goToSettings: () -> Unit,
    onSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.sendEvent(ViewResumed)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                BiometricSetupState.SideEffect.Cancel -> onCancel()
                BiometricSetupState.SideEffect.Success -> onSuccess()
                BiometricSetupState.SideEffect.GoToSettings -> goToSettings()
                is BiometricSetupState.SideEffect.CancelWithToast -> {
                    if (sideEffect.code != null && sideEffect.message != null) {
                        showToast(getMessageForErrorCode(sideEffect.code, sideEffect.message))
                    } else {
                        showToast(context.getString(R.string.error_during_hardware_authentication))
                    }
                    onCancel()
                }
            }
        }
    }

    val activity = context as? FragmentActivity ?: context.getBaseActivity() as FragmentActivity
    val executor = remember { ContextCompat.getMainExecutor(context) }

    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.sendEvent(AuthenticationError(errorCode, errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.sendEvent(AuthenticationSuccess(result.cryptoObject))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.sendEvent(AuthenticationFailed)
                }
            },
        )
    }

    BiometricLockPrompt(
        allowedAuthenticator = uiState.data.authenticator,
        title = stringResource(R.string.account_recovery_biometric_intro_title),
        subtitle = stringResource(R.string.onboarding_dialog_auth_required_hardware_module_google_fp_title),
        isBiometricPromptDisplayed = uiState.data.showBiometricPrompt,
        negativeButtonText = stringResource(R.string.cancel),
        cryptoObject = uiState.data.cryptoObject,
        biometricPrompt = biometricPrompt,
    )

    when (val error = uiState.data.error) {
        BiometricNotRegistered -> {
            BiometricRegisterDialog(
                onPositiveClick = { viewModel.sendEvent(DialogPositive(error)) },
                onDismiss = { viewModel.sendEvent(DialogDismissed) },
            )
        }
        RequireStrongerBiometric -> {
            BiometricRegisterStrongerDialog(
                onPositiveClick = { viewModel.sendEvent(DialogPositive(error)) },
                onDismiss = { viewModel.sendEvent(DialogDismissed) },
            )
        }
        else -> Unit
    }

    val onDismiss = { viewModel.sendEvent(DialogDismissed) }
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden) onDismiss()
            true
        },
    )
    if (uiState.data.showBiometricRecoverySetupBottomSheet) {
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
            BiometricRecoveryRegisterBottomSheet(
                onPositiveClick = { viewModel.sendEvent(RecoverySetupPositive) },
                onDismiss = { viewModel.sendEvent(DialogDismissed) },
            )
        }
    }
    if (uiState.data.showBiometricSetupBottomSheet) {
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
            BiometricRegisterBottomSheet(
                onPositiveClick = { viewModel.sendEvent(BiometricSetupPositive) },
                onDismiss = { viewModel.sendEvent(DialogDismissed) },
            )
        }
    }
}

@Composable
fun BiometricRegisterDialog(
    onPositiveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.biometric_prompt_registration_required_title),
        description = {
            Text(text = stringResource(id = R.string.biometric_prompt_registration_required_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.biometric_prompt_registration_required_button_continue)),
        mainActionClick = onPositiveClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.biometric_prompt_registration_required_button_cancel)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun BiometricRegisterStrongerDialog(
    onPositiveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.biometric_prompt_strong_registration_required_title),
        description = {
            Text(text = stringResource(id = R.string.biometric_prompt_strong_registration_required_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(
            stringResource(id = R.string.biometric_prompt_strong_registration_required_button_continue),
        ),
        mainActionClick = onPositiveClick,
        additionalActionLayout = ButtonLayout.TextOnly(
            stringResource(id = R.string.biometric_prompt_strong_registration_required_button_cancel),
        ),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun BiometricRecoveryRegisterBottomSheet(
    onPositiveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetContent(
        decoration = BottomSheetDefaults.decoration(IconTokens.tipOutlined, Mood.Brand),
        title = stringResource(id = R.string.account_recovery_intro_title),
        description = stringResource(id = R.string.account_recovery_intro_description),
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.account_recovery_intro_positive_cta),
            secondaryButtonText = stringResource(R.string.account_recovery_intro_negative_cta),
            onPrimaryButtonClick = onPositiveClick,
            onSecondaryButtonClick = onDismiss,
        ),
    )
}

@Composable
fun BiometricRegisterBottomSheet(
    onPositiveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetContent(
        decoration = BottomSheetDefaults.decoration(IconTokens.fingerprintOutlined, Mood.Brand),
        title = stringResource(id = R.string.account_recovery_biometric_intro_title),
        description = stringResource(id = R.string.account_recovery_biometric_intro_description),
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.account_recovery_biometric_intro_positive_cta),
            secondaryButtonText = stringResource(R.string.account_recovery_biometric_intro_negative_cta),
            onPrimaryButtonClick = onPositiveClick,
            onSecondaryButtonClick = onDismiss,
        ),
    )
}

@Preview
@Composable
private fun BiometricRegisterDialogPreview() {
    DashlaneModalBottomSheetPreview {
        BiometricRegisterDialog(onPositiveClick = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun BiometricRegisterStrongerDialogPreview() {
    DashlaneModalBottomSheetPreview {
        BiometricRegisterStrongerDialog(onPositiveClick = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun BiometricRecoveryRegisterBottomSheetPreview() {
    DashlaneModalBottomSheetPreview {
        BiometricRecoveryRegisterBottomSheet(onPositiveClick = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun BiometricRegisterBottomSheetPreview() {
    DashlaneModalBottomSheetPreview {
        BiometricRegisterBottomSheet(onPositiveClick = {}, onDismiss = {})
    }
}

private fun getMessageForErrorCode(code: Int, message: CharSequence): String {
    val messageToUser: String
    val errorMessage = " 0x$code"
    messageToUser = message.toString() + errorMessage
    return messageToUser
}