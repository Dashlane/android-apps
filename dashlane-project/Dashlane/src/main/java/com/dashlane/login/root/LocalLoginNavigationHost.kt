package com.dashlane.login.root

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dashlane.design.component.BaseDialog
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockType
import com.dashlane.login.LocalLoginDestination
import com.dashlane.login.LocalLoginDestination.Biometric
import com.dashlane.login.LocalLoginDestination.LostKey
import com.dashlane.login.LocalLoginDestination.LostKey.toLocalLoginDestination
import com.dashlane.login.LocalLoginDestination.Otp2
import com.dashlane.login.LocalLoginDestination.Password
import com.dashlane.login.LocalLoginDestination.Pin
import com.dashlane.login.LocalLoginDestination.PinRecovery
import com.dashlane.login.LocalLoginDestination.Sso
import com.dashlane.login.LoginStrategy
import com.dashlane.login.biometric.LoginBiometricFallback
import com.dashlane.login.biometric.LoginBiometricScreen
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.ChangeMp
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Recovery
import com.dashlane.login.biometric.recovery.biometricRecoveryNavigation
import com.dashlane.login.biometric.recovery.toBiometricRecoveryDestination
import com.dashlane.login.pages.secrettransfer.help.RecoveryHelpScreen
import com.dashlane.login.pages.secrettransfer.help.lostkey.LostKeyScreen
import com.dashlane.login.pages.secrettransfer.localLoginSecretTransferNavigation
import com.dashlane.login.password.LoginPasswordScreen
import com.dashlane.login.pin.LoginPinFallback
import com.dashlane.login.pin.LoginPinScreen
import com.dashlane.login.sso.LoginSsoScreen
import com.dashlane.login.totp.LoginTotpScreen
import com.dashlane.user.UserAccountInfo
import java.io.Serializable


@Suppress("LongMethod", "kotlin:S3776", "kotlin:S107")
@Composable
fun LocalLoginNavigationHost(
    contentPadding: PaddingValues = PaddingValues(),
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    startDestination: LocalLoginDestination,
    showSnackBar: (String) -> Unit,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit,
    onChangeAccount: (String?) -> Unit,
    onLogout: (String?) -> Unit,
    onLogoutMPLess: (String) -> Unit,
    goToArk: (UserAccountInfo.AccountType) -> Unit,
) {
    val navController = rememberNavController()
    var backgroundState by rememberSaveable {
        mutableStateOf(LocalLoginBackgroundState(lockSetting.shouldThemeAsDialog, isTransparent = false))
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val biometricRecoveryDestination = navBackStackEntry?.toBiometricRecoveryDestination()
        backgroundState = when (biometricRecoveryDestination) {
            ChangeMp, Recovery -> LocalLoginBackgroundState()
            BiometricRecoveryDestination.Biometric -> LocalLoginBackgroundState(isDialog = false, isTransparent = true)
            else -> when (navBackStackEntry?.toLocalLoginDestination()) {
                PinRecovery, LocalLoginDestination.SecretTransfer, Otp2 -> LocalLoginBackgroundState()
                Sso, Password, Pin -> LocalLoginBackgroundState(isDialog = lockSetting.shouldThemeAsDialog)
                Biometric -> LocalLoginBackgroundState(isDialog = false, isTransparent = true)
                else -> backgroundState
            }
        }
    }

    val modifier = if (lockSetting.shouldThemeAsDialog) {
        Modifier
    } else {
        Modifier.fillMaxHeight()
    }

    LocalLoginContent(
        contentPadding = contentPadding,
        backgroundState = backgroundState,
        onDialogDismissed = onCancel,
    ) {
        NavHost(
            startDestination = startDestination,
            navController = navController,
        ) {
            composable<Otp2> {
                LoginTotpScreen(
                    viewModel = hiltViewModel(),
                    verificationMode = VerificationMode.OTP2,
                    goToNext = { locks ->
                        val destination = when {
                            lockSetting.isMasterPasswordReset -> Password
                            LockType.PinCode in locks -> Pin(userAccountInfo.username)
                            LockType.Biometric in locks -> Biometric(userAccountInfo.username)
                            else -> Password
                        }
                        navController.navigate(route = destination)
                    },
                )
            }
            composable<Pin> {
                LoginPinScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    showSnackBar = showSnackBar,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.Unlock) },
                    onCancel = { fallback ->
                        when (fallback) {
                            LoginPinFallback.MPLess -> onLogout(null)
                            LoginPinFallback.Cancellable -> onCancel()
                            LoginPinFallback.MP -> navController.navigate(route = Password)
                            LoginPinFallback.SSO -> navController.navigate(route = Sso)
                        }
                    },
                    onLogout = { email, isMPLess ->
                        navController.popBackStack()
                        if (isMPLess) onLogoutMPLess(email) else onLogout(email)
                    },
                    goToSecretTransfer = { email ->
                        navController.navigate(route = LocalLoginDestination.SecretTransfer(email))
                    },
                    goToRecovery = { navController.navigate(route = PinRecovery) },
                )
            }
            composable<Biometric> {
                LoginBiometricScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    showSnackBar = showSnackBar,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.Unlock) },
                    onCancel = { onCancel() },
                    onFallback = { fallback ->
                        when (fallback) {
                            LoginBiometricFallback.Cancellable -> onCancel()
                            LoginBiometricFallback.Pin -> navController.navigate(route = Pin(userAccountInfo.username))
                            LoginBiometricFallback.Password -> navController.navigate(route = Password)
                            LoginBiometricFallback.SSO -> navController.navigate(route = Sso)
                        }
                    },
                    onLockout = { fallback ->
                        navController.popBackStack()
                        when (fallback) {
                            LoginBiometricFallback.Cancellable -> onCancel()
                            LoginBiometricFallback.Pin -> navController.navigate(route = Pin(userAccountInfo.username))
                            LoginBiometricFallback.Password -> navController.navigate(route = Password)
                            LoginBiometricFallback.SSO -> navController.navigate(route = Sso)
                        }
                    },
                    onLogout = { email, _ ->
                        navController.popBackStack()
                        onLogout(email)
                    },
                )
            }
            composable<Sso> {
                LoginSsoScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    showSnackBar = showSnackBar,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.Unlock) },
                    onCancel = onCancel,
                    changeAccount = onChangeAccount,
                )
            }
            composable<Password> {
                LoginPasswordScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    lockSetting = lockSetting,
                    onSuccess = { strategy, _ -> onSuccess(strategy) },
                    onCancel = onCancel,
                    onFallback = {
                        val destination = when {
                            LockType.Biometric in lockSetting.locks -> Biometric(userAccountInfo.username)
                            LockType.PinCode in lockSetting.locks -> Pin(userAccountInfo.username)
                            else -> null
                        }
                        destination?.let {
                            navController.navigate(destination) {
                                popUpTo(Password) {
                                    inclusive = true
                                }
                            }
                        } ?: onCancel()
                    },
                    changeAccount = onChangeAccount,
                    logout = onLogout,
                    biometricRecovery = { navController.navigate(BiometricRecoveryDestination.Biometric(userAccountInfo.username)) },
                    goToArk = goToArk,
                )
            }
            composable<PinRecovery> {
                RecoveryHelpScreen(
                    viewModel = hiltViewModel(),
                    email = userAccountInfo.username,
                    onStartRecoveryClicked = { goToArk(UserAccountInfo.AccountType.InvisibleMasterPassword) },
                    onLostKeyClicked = { navController.navigate(LostKey) },
                )
            }
            composable<LostKey> {
                LostKeyScreen()
            }
            localLoginSecretTransferNavigation(
                goToArk = goToArk,
                onSuccess = { strategy -> onSuccess(strategy) },
                onCancel = onCancel,
            )
            biometricRecoveryNavigation(
                navController = navController,
                contentPadding = contentPadding,
                userAccountInfo = userAccountInfo,
                lockSetting = lockSetting.copy(isLockCancelable = true),
                showSnackBar = showSnackBar,
                onSuccess = onSuccess,
                onCancel = { navController.navigateUp() },
                onLogout = { onLogout(null) },
            )
        }
    }
}

@Composable
fun LocalLoginContent(
    contentPadding: PaddingValues,
    backgroundState: LocalLoginBackgroundState,
    onDialogDismissed: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (backgroundState.isDialog) {
        BaseDialog(
            onDismissRequest = onDialogDismissed,
            contentPadding = contentPadding,
        ) {
            content()
        }
    } else if (backgroundState.isTransparent) {
        Surface(
            modifier = Modifier.padding(contentPadding),
            color = Color.Transparent,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = Modifier.padding(contentPadding),
            color = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
        ) {
            content()
        }
    }
}

data class LocalLoginBackgroundState(
    val isDialog: Boolean = false,
    val isTransparent: Boolean = false,
) : Serializable