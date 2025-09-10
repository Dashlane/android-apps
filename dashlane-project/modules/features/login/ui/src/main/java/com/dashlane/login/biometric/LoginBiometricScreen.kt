package com.dashlane.login.biometric

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.lock.LockPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.login.ui.R
import com.dashlane.ui.common.compose.components.BiometricLockPrompt
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.getBaseActivity

@Suppress("LongMethod", "kotlin:S107")
@Composable
fun LoginBiometricScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginBiometricViewModel,
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    isBiometricRecovery: Boolean = false,
    showSnackBar: (String) -> Unit,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onFallback: (LoginBiometricFallback) -> Unit,
    onLockout: (LoginBiometricFallback) -> Unit,
    onLogout: (String?, LoginBiometricFallback) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: context.getBaseActivity() as FragmentActivity
    val executor = remember { ContextCompat.getMainExecutor(context) }

    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.authenticationError(errorCode, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.authenticationSuccess(result.cryptoObject)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.authenticationFailed()
                }
            },
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(
            userAccountInfo = userAccountInfo,
            lockSetting = lockSetting,
            isBiometricRecovery = isBiometricRecovery,
        )
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                LoginBiometricState.SideEffect.Cancel -> onCancel()
                is LoginBiometricState.SideEffect.Fallback -> onFallback(state.fallback)
                is LoginBiometricState.SideEffect.Lockout -> {
                    state.error.toText(context)?.let { message -> showSnackBar(message) }
                    onLockout(state.fallback)
                }
                is LoginBiometricState.SideEffect.Logout -> {
                    state.error.toText(context)?.let { message -> showSnackBar(message) }
                    onLogout(state.email, state.fallback)
                }
                is LoginBiometricState.SideEffect.UnlockSuccess -> onSuccess()
            }
            biometricPrompt.cancelAuthentication()
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    val (title, subtitle) = when {
        uiState.isRecovery -> {
            stringResource(id = R.string.account_recovery_biometric_prompt_title) to
                stringResource(id = R.string.account_recovery_biometric_prompt_description, userAccountInfo.username)
        }
        uiState.lockSetting?.lockPrompt is LockPrompt.ForItem -> {
            if ((uiState.lockSetting?.lockPrompt as? LockPrompt.ForItem)?.isSecureNote == true) {
                stringResource(id = R.string.unlock_message_secure_note_biometrics) to userAccountInfo.username
            } else {
                stringResource(id = R.string.unlock_message_item_biometrics) to userAccountInfo.username
            }
        }
        else -> null to userAccountInfo.username
    }

    if (lockSetting.shouldThemeAsDialog) {
        LoginBiometricInAppContent(
            modifier = modifier,
            title = title,
            email = userAccountInfo.username,
            fallbackText = uiState.fallback.toText(),
            onRetry = viewModel::retry,
            onFallback = viewModel::fallback,
        )
    } else {
        LoginBiometricContent(
            modifier = modifier,
            title = title,
            email = userAccountInfo.username,
            fallbackText = uiState.fallback.toText(),
            onRetry = viewModel::retry,
            onFallback = viewModel::fallback,
        )
    }

    BiometricLockPrompt(
        allowedAuthenticator = uiState.allowedAuthenticator,
        title = title ?: stringResource(id = R.string.window_biometric_unlock_hardware_module_google_fp_title),
        subtitle = subtitle,
        isBiometricPromptDisplayed = uiState.isBiometricPromptDisplayed,
        negativeButtonText = uiState.fallback.toText(),
        cryptoObject = uiState.cryptoObject,
        biometricPrompt = biometricPrompt,
    )
}

@Composable
@VisibleForTesting
fun LoginBiometricContent(
    modifier: Modifier = Modifier,
    title: String?,
    email: String,
    fallbackText: String,
    onRetry: () -> Unit,
    onFallback: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = title ?: stringResource(id = R.string.unlock_biometrics),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = email,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                
                .align(Alignment.CenterHorizontally),
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.generic_error_retry_button)),
            onClick = onRetry,
        )
        ButtonMedium(
            modifier = Modifier
                
                .align(Alignment.CenterHorizontally),
            intensity = Intensity.Supershy,
            layout = ButtonLayout.TextOnly(text = fallbackText),
            onClick = onFallback,
        )
    }
}

@Composable
@VisibleForTesting
fun LoginBiometricInAppContent(
    modifier: Modifier = Modifier,
    title: String?,
    email: String,
    fallbackText: String,
    onRetry: () -> Unit,
    onFallback: () -> Unit,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp),
        ) {
            Image(
                modifier = Modifier.height(40.dp),
                painter = painterResource(com.dashlane.ui.R.drawable.vd_logo_dashlane_lockup),
                colorFilter = ColorFilter.tint(DashlaneTheme.colors.textInverseCatchy.value),
                contentDescription = stringResource(id = com.dashlane.ui.R.string.dashlane_main_app_name),
            )
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = title ?: stringResource(id = R.string.unlock_biometrics),
                style = DashlaneTheme.typography.titleSectionMedium,
                color = DashlaneTheme.colors.textInverseCatchy,
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = email,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textInverseCatchy,
            )
            Spacer(modifier = Modifier.weight(1f))
            ButtonMedium(
                modifier = Modifier
                    
                    .align(Alignment.CenterHorizontally),
                mood = Mood.Neutral,
                intensity = Intensity.Catchy,
                layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.generic_error_retry_button)),
                onClick = onRetry,
            )
            ButtonMedium(
                modifier = Modifier
                    
                    .align(Alignment.CenterHorizontally),
                mood = Mood.Neutral,
                intensity = Intensity.Catchy,
                layout = ButtonLayout.TextOnly(text = fallbackText),
                onClick = onFallback,
            )
        }
    }
}

@Composable
private fun LoginBiometricFallback.toText() = when (this) {
    is LoginBiometricFallback.Cancellable -> stringResource(id = R.string.cancel)
    is LoginBiometricFallback.Pin -> stringResource(id = R.string.biometric_prompt_pin_fallback)
    is LoginBiometricFallback.SSO -> stringResource(id = R.string.sso_lock_use_sso)
    else -> stringResource(id = R.string.fragment_lock_pin_button_use_master_password)
}

private fun LoginBiometricError?.toText(context: Context) = when (this) {
    is LoginBiometricError.Generic -> this.errorMessage
    LoginBiometricError.TooManyAttempt -> context.getString(R.string.lock_fingerprint_force_logout_fingerprint_incorrect_too_much)
    else -> null
}

@Preview
@Composable
private fun LoginBiometricContentPreview() {
    DashlanePreview {
        LoginBiometricContent(
            title = "Unlock with your biometrics",
            email = "randomemail@provider.com",
            fallbackText = "Use Pin",
            onRetry = {},
            onFallback = {},
        )
    }
}

@Preview
@Composable
private fun LoginBiometricInAppContentPreview() {
    DashlanePreview {
        LoginBiometricInAppContent(
            title = "Unlock with your biometrics",
            email = "randomemail@provider.com",
            fallbackText = "Use Pin",
            onRetry = {},
            onFallback = {},
        )
    }
}