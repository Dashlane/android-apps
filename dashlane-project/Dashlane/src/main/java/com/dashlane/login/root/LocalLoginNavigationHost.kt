package com.dashlane.login.root

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginIntents
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.biometric.compose.LoginBiometricFallback
import com.dashlane.login.pages.biometric.compose.LoginBiometricScreen
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryNavigation
import com.dashlane.login.pages.biometric.recovery.biometricRecoveryNavigation
import com.dashlane.login.pages.password.compose.LoginPasswordScreen
import com.dashlane.login.pages.pin.compose.LoginPinFallback
import com.dashlane.login.pages.pin.compose.LoginPinScreen
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation
import com.dashlane.login.pages.secrettransfer.help.RecoveryHelpScreen
import com.dashlane.login.pages.secrettransfer.help.lostkey.LostKeyScreen
import com.dashlane.login.pages.secrettransfer.secretTransferNavigation
import com.dashlane.login.pages.sso.compose.LoginSsoScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.login.root.LocalLoginDestination.LOGIN_KEY
import com.dashlane.login.root.LocalLoginDestination.authenticatorDestination
import com.dashlane.login.root.LocalLoginDestination.biometricDestination
import com.dashlane.login.root.LocalLoginDestination.lostKeyDestination
import com.dashlane.login.root.LocalLoginDestination.otp2Destination
import com.dashlane.login.root.LocalLoginDestination.passwordDestination
import com.dashlane.login.root.LocalLoginDestination.pinDestination
import com.dashlane.login.root.LocalLoginDestination.pinRecoveryDestination
import com.dashlane.login.root.LocalLoginDestination.secretTransferDestination
import com.dashlane.login.root.LocalLoginDestination.ssoDestination
import java.io.Serializable

@Suppress("LongMethod", "kotlin:S3776")
@Composable
fun LocalLoginNavigationHost(
    viewModel: LocalLoginViewModel = hiltViewModel(),
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    startDestination: String,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit,
    onChangeAccount: (String?) -> Unit,
    onLogout: (String?) -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var backgroundState by rememberSaveable { mutableStateOf(LocalLoginBackgroundState(lockSetting.shouldThemeAsDialog, isTransparent = false)) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.localLoginStarted(userAccountInfo, lockSetting)
    }

    LaunchedEffect(navBackStackEntry) {
        backgroundState = when (navBackStackEntry?.destination?.route) {
            pinRecoveryDestination,
            secretTransferDestination,
            BiometricRecoveryNavigation.changeMpDestination,
            BiometricRecoveryNavigation.recoveryDestination,
            otp2Destination -> LocalLoginBackgroundState()
            ssoDestination,
            passwordDestination,
            pinDestination -> LocalLoginBackgroundState(isDialog = lockSetting.shouldThemeAsDialog)
            BiometricRecoveryNavigation.biometricDestination,
            biometricDestination -> LocalLoginBackgroundState(isDialog = false, isTransparent = true)
            else -> backgroundState
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val goToArk: () -> Unit = {
        context.startActivity(
            LoginIntents.createAccountRecoveryKeyIntent(
                context = context,
                registeredUserDevice = uiState.registeredUserDevice ?: throw IllegalStateException("registeredUserDevice cannot be null"),
                accountType = UserAccountInfo.AccountType.InvisibleMasterPassword,
                authTicket = null
            )
        )
        onCancel()
    }

    val modifier = if (lockSetting.shouldThemeAsDialog) {
        Modifier
    } else {
        Modifier.fillMaxHeight()
    }

    LocalLoginContent(
        backgroundState = backgroundState,
        onDialogDismissed = onCancel
    ) {
        NavHost(
            startDestination = startDestination,
            navController = navController
        ) {
            composable(
                route = "$otp2Destination?${LoginDestination.LOGIN_KEY}={${LoginDestination.LOGIN_KEY}}",
                arguments = listOf(
                    navArgument(LOGIN_KEY) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = userAccountInfo.username
                    }
                )
            ) {
                LoginTotpScreen(
                    viewModel = hiltViewModel(),
                    verificationMode = VerificationMode.OTP2,
                    goToNext = { registeredUserDevice, authTicket ->
                        val lockType = viewModel.otpSuccess(registeredUserDevice = registeredUserDevice, authTicket = authTicket)
                        val destination = when {
                            lockSetting.isMasterPasswordReset -> passwordDestination
                            lockType == LockTypeManager.LOCK_TYPE_PIN_CODE -> pinDestination
                            lockType == LockTypeManager.LOCK_TYPE_BIOMETRIC -> biometricDestination
                            else -> passwordDestination
                        }
                        navController.navigate(route = destination)
                    },
                    goToPush = { email -> navController.navigate(route = "$authenticatorDestination/$email") }
                )
            }
            composable(route = pinDestination) {
                LoginPinScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.UNLOCK) },
                    onCancel = { fallback ->
                        when (fallback) {
                            LoginPinFallback.MPLess -> onLogout(null)
                            LoginPinFallback.Cancellable -> onCancel()
                            LoginPinFallback.MP -> navController.navigate(route = passwordDestination)
                            LoginPinFallback.SSO -> navController.navigate(route = ssoDestination)
                        }
                    },
                    onLogout = { email ->
                        navController.popBackStack()
                        onLogout(email)
                    },
                    goToSecretTransfer = { email ->
                        val secretTransferStartDestination = LoginSecretTransferNavigation.chooseTypeDestination
                        navController.navigate(route = "$secretTransferDestination?${LoginSecretTransferNavigation.START_DESTINATION_KEY}=$secretTransferStartDestination&$LOGIN_KEY=$email")
                    },
                    goToRecovery = { navController.navigate(route = pinRecoveryDestination) }
                )
            }
            composable(route = biometricDestination) {
                LoginBiometricScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.UNLOCK) },
                    onCancel = { onCancel() },
                    onFallback = { fallback ->
                        when (fallback) {
                            LoginBiometricFallback.MPLess -> navController.navigate(route = pinDestination)
                            LoginBiometricFallback.Cancellable -> onCancel()
                            LoginBiometricFallback.MP -> navController.navigate(route = passwordDestination)
                            LoginBiometricFallback.SSO -> navController.navigate(route = ssoDestination)
                        }
                    },
                    onLockout = { fallback ->
                        navController.popBackStack()
                        when (fallback) {
                            LoginBiometricFallback.MPLess -> navController.navigate(route = pinDestination)
                            LoginBiometricFallback.Cancellable -> onCancel()
                            LoginBiometricFallback.MP -> navController.navigate(route = passwordDestination)
                            LoginBiometricFallback.SSO -> navController.navigate(route = ssoDestination)
                        }
                    },
                    onLogout = { email, _ ->
                        navController.popBackStack()
                        onLogout(email)
                    }
                )
            }
            composable(route = ssoDestination) {
                LoginSsoScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.UNLOCK) },
                    onCancel = onCancel,
                    changeAccount = onChangeAccount,
                )
            }
            composable(route = passwordDestination) {
                LoginPasswordScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    lockSetting = lockSetting,
                    onSuccess = { strategy, _ -> onSuccess(strategy) },
                    onCancel = onCancel,
                    onFallback = {
                        val destination = when (lockSetting.lockType) {
                            LockTypeManager.LOCK_TYPE_PIN_CODE -> pinDestination
                            LockTypeManager.LOCK_TYPE_BIOMETRIC -> biometricDestination
                            else -> null
                        }
                        destination?.let { navController.navigate(destination) { popUpTo(passwordDestination) { inclusive = true } } } ?: onCancel()
                    },
                    changeAccount = onChangeAccount,
                    logout = onLogout,
                    biometricRecovery = { navController.navigate(BiometricRecoveryNavigation.biometricRecoveryRoute) },
                )
            }
            composable(route = pinRecoveryDestination) {
                RecoveryHelpScreen(
                    viewModel = hiltViewModel(),
                    email = userAccountInfo.username,
                    onStartRecoveryClicked = { goToArk() },
                    onLostKeyClicked = { navController.navigate(lostKeyDestination) }
                )
            }
            composable(route = lostKeyDestination) { LostKeyScreen() }
            secretTransferNavigation(
                route = secretTransferDestination,
                onSuccess = { onSuccess(LoginStrategy.Strategy.NO_STRATEGY) },
                onCancel = onCancel
            )
            biometricRecoveryNavigation(
                navController = navController,
                userAccountInfo = userAccountInfo,
                lockSetting = lockSetting.copy(
                    topicLock = context.getString(R.string.account_recovery_biometric_prompt_title),
                    subTopicLock = context.getString(R.string.account_recovery_biometric_prompt_description, userAccountInfo.username),
                    isLockCancelable = true,
                ),
                onSuccess = onSuccess,
                onCancel = { navController.navigateUp() },
                onLogout = { onLogout(null) }
            )
        }
    }
}

@Composable
fun LocalLoginContent(
    backgroundState: LocalLoginBackgroundState,
    onDialogDismissed: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (backgroundState.isDialog) {
        Dialog(
            onDismissRequest = onDialogDismissed
        ) {
            Surface(
                shape = RoundedCornerShape(size = 12.dp),
                color = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                tonalElevation = 1.dp,
            ) {
                content()
            }
        }
    } else if (backgroundState.isTransparent) {
        content()
    } else {
        Surface(color = DashlaneTheme.colors.containerAgnosticNeutralSupershy) {
            content()
        }
    }
}

data class LocalLoginBackgroundState(
    val isDialog: Boolean = false,
    val isTransparent: Boolean = false,
) : Serializable

object LocalLoginDestination {
    const val passwordDestination = "lock/password"
    const val biometricDestination = "lock/biometric"
    const val pinDestination = "lock/pin"
    const val pinRecoveryDestination = "lock/pin/recovery"
    const val lostKeyDestination = "lock/pin/lostKey"
    const val ssoDestination = "lock/sso"
    const val otp2Destination = "lock/otp2"
    const val authenticatorDestination = "lock/authenticator"
    const val secretTransferDestination = "lock/secretTransfer"

    const val LOGIN_KEY = "login"
}
