package com.dashlane.login.pages.biometric.compose

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.darkColors
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.lock.LockSetting
import com.dashlane.ui.widgets.compose.DashlaneLogo
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getBaseActivity

@Composable
fun LoginBiometricScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginBiometricViewModel,
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onFallback: (LoginBiometricFallback) -> Unit,
    onLockout: (LoginBiometricFallback) -> Unit,
    onLogout: (String?, LoginBiometricFallback) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(userAccountInfo = userAccountInfo, lockSetting = lockSetting)
        viewModel.navigationState.collect { state ->
            when (state) {
                LoginBiometricNavigationState.Cancel -> onCancel()
                is LoginBiometricNavigationState.Fallback -> onFallback(state.fallback)
                is LoginBiometricNavigationState.Lockout -> {
                    state.error.toText(context)?.let { message ->
                        context.getBaseActivity()?.let { activity -> SnackbarUtils.showSnackbar(activity, message) }
                    }
                    onLockout(state.fallback)
                }
                is LoginBiometricNavigationState.Logout -> {
                    state.error.toText(context)?.let { message ->
                        context.getBaseActivity()?.let { activity -> SnackbarUtils.showSnackbar(activity, message) }
                    }
                    onLogout(state.email, state.fallback)
                }
                is LoginBiometricNavigationState.UnlockSuccess -> onSuccess()
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val (title, subtitle) = when {
        uiState.isRecovery -> {
            stringResource(id = R.string.account_recovery_biometric_prompt_title) to
                stringResource(id = R.string.account_recovery_biometric_prompt_description, userAccountInfo.username)
        }
        else -> uiState.lockSetting?.topicLock to (uiState.lockSetting?.subTopicLock ?: userAccountInfo.username)
    }
    if (lockSetting.shouldThemeAsDialog) {
        LoginBiometricInAppContent(modifier = modifier, title = title, email = userAccountInfo.username)
    } else {
        LoginBiometricContent(modifier = modifier, title = title, email = userAccountInfo.username)
    }

    LoginBiometricPrompt(
        allowedAuthenticator = uiState.allowedAuthenticator,
        title = title ?: stringResource(id = R.string.window_biometric_unlock_hardware_module_google_fp_title),
        subtitle = subtitle,
        isBiometricPromptDisplayed = uiState.isBiometricPromptDisplayed,
        negativeButtonText = uiState.fallback.toText(),
        cryptoObject = uiState.cryptoObject,
        onAuthenticationError = viewModel::authenticationError,
        onAuthenticationSucceeded = viewModel::authenticationSuccess,
        onAuthenticationFailed = viewModel::authenticationFailed
    )
}

@Composable
@VisibleForTesting
fun LoginBiometricContent(
    modifier: Modifier = Modifier,
    title: String?,
    email: String
) {
    Column(
        modifier = modifier
            .heightIn(min = 300.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        DashlaneLogo(color = DashlaneTheme.colors.oddityBrand)
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
    }
}

@Composable
@VisibleForTesting
fun LoginBiometricInAppContent(
    modifier: Modifier = Modifier,
    title: String?,
    email: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            DashlaneLogo(color = darkColors.textNeutralCatchy.value)
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = title ?: stringResource(id = R.string.unlock_biometrics),
                style = DashlaneTheme.typography.titleSectionMedium,
                color = darkColors.textNeutralCatchy,
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = email,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = darkColors.textNeutralCatchy,
            )
        }
    }
}

@Composable
private fun LoginBiometricPrompt(
    allowedAuthenticator: Int,
    title: String,
    subtitle: String,
    isBiometricPromptDisplayed: Boolean,
    negativeButtonText: String,
    cryptoObject: BiometricPrompt.CryptoObject?,
    onAuthenticationError: (Int, String) -> Unit,
    onAuthenticationSucceeded: (BiometricPrompt.CryptoObject?) -> Unit,
    onAuthenticationFailed: () -> Unit,
) {
    val context = LocalContext.current.getBaseActivity() ?: return
    val executor = remember { ContextCompat.getMainExecutor(context) }

    val biometricPrompt = remember {
        BiometricPrompt(
            context as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onAuthenticationError(errorCode, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticationSucceeded(result.cryptoObject)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onAuthenticationFailed()
                }
            }
        )
    }

    if (isBiometricPromptDisplayed) {
        LaunchedEffect(cryptoObject) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setAllowedAuthenticators(allowedAuthenticator)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setConfirmationRequired(false)
                .build()

            cryptoObject?.let {
                biometricPrompt.authenticate(promptInfo, it)
            } ?: run {
                biometricPrompt.authenticate(promptInfo)
            }
        }
    } else {
        biometricPrompt.cancelAuthentication()
    }
}

@Composable
private fun LoginBiometricFallback.toText() = when (this) {
    is LoginBiometricFallback.Cancellable -> stringResource(id = R.string.cancel)
    is LoginBiometricFallback.MPLess -> stringResource(id = R.string.biometric_prompt_pin_fallback)
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
        )
    }
}
