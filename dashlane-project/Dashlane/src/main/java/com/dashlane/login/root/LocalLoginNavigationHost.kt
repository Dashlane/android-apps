package com.dashlane.login.root

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockType
import com.dashlane.login.LoginIntents
import com.dashlane.login.LoginStrategy
import com.dashlane.login.pages.biometric.compose.LoginBiometricFallback
import com.dashlane.login.pages.biometric.compose.LoginBiometricScreen
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryDestination
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryDestination.Biometric.toBiometricRecoveryDestination
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryDestination.ChangeMp
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryDestination.Recovery
import com.dashlane.login.pages.biometric.recovery.biometricRecoveryNavigation
import com.dashlane.login.pages.password.compose.LoginPasswordScreen
import com.dashlane.login.pages.pin.compose.LoginPinFallback
import com.dashlane.login.pages.pin.compose.LoginPinScreen
import com.dashlane.login.pages.secrettransfer.help.RecoveryHelpScreen
import com.dashlane.login.pages.secrettransfer.help.lostkey.LostKeyScreen
import com.dashlane.login.pages.secrettransfer.localLoginSecretTransferNavigation
import com.dashlane.login.pages.sso.compose.LoginSsoScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.login.root.LocalLoginDestination.Biometric
import com.dashlane.login.root.LocalLoginDestination.LostKey
import com.dashlane.login.root.LocalLoginDestination.LostKey.toLocalLoginDestination
import com.dashlane.login.root.LocalLoginDestination.Otp2
import com.dashlane.login.root.LocalLoginDestination.Password
import com.dashlane.login.root.LocalLoginDestination.Pin
import com.dashlane.login.root.LocalLoginDestination.PinRecovery
import com.dashlane.login.root.LocalLoginDestination.Sso
import com.dashlane.user.UserAccountInfo
import java.io.Serializable

@Suppress("LongMethod", "kotlin:S3776")
@Composable
fun LocalLoginNavigationHost(
    registeredUserDevice: RegisteredUserDevice,
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    startDestination: LocalLoginDestination,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit,
    onChangeAccount: (String?) -> Unit,
    onLogout: (String?) -> Unit,
    onLogoutMPLess: (String) -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var backgroundState by rememberSaveable { mutableStateOf(LocalLoginBackgroundState(lockSetting.shouldThemeAsDialog, isTransparent = false)) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val biometricRecoveryDestination = navBackStackEntry?.toBiometricRecoveryDestination()
        backgroundState = when (biometricRecoveryDestination) {
            ChangeMp, Recovery -> LocalLoginBackgroundState()
            BiometricRecoveryDestination.Biometric ->
                LocalLoginBackgroundState(isDialog = false, isTransparent = true)
            else -> when (navBackStackEntry?.toLocalLoginDestination()) {
                PinRecovery, LocalLoginDestination.SecretTransfer, Otp2 -> LocalLoginBackgroundState()
                Sso, Password, Pin ->
                    LocalLoginBackgroundState(isDialog = lockSetting.shouldThemeAsDialog)
                Biometric -> LocalLoginBackgroundState(isDialog = false, isTransparent = true)
                else -> backgroundState
            }
        }
    }

    val goToArk: () -> Unit = {
        context.startActivity(
            LoginIntents.createAccountRecoveryKeyIntent(
                context = context,
                registeredUserDevice = registeredUserDevice,
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
    ) { contentPadding ->
        NavHost(
            startDestination = startDestination,
            navController = navController
        ) {
            composable<Otp2> {
                LoginTotpScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    verificationMode = VerificationMode.OTP2,
                    goToNext = { locks ->
                        val destination = when {
                            lockSetting.isMasterPasswordReset -> Password
                            LockType.PinCode in locks -> Pin(userAccountInfo.username)
                            LockType.Biometric in locks -> Biometric
                            else -> Password
                        }
                        navController.navigate(route = destination)
                    },
                )
            }
            composable<Pin> {
                LoginPinScreen(
                    modifier = modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
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
                    goToRecovery = { navController.navigate(route = PinRecovery) }
                )
            }
            composable<Biometric> {
                LoginBiometricScreen(
                    modifier = modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
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
                    }
                )
            }
            composable<Sso> {
                LoginSsoScreen(
                    modifier = modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    userAccountInfo = userAccountInfo,
                    lockSetting = lockSetting,
                    onSuccess = { onSuccess(LoginStrategy.Strategy.Unlock) },
                    onCancel = onCancel,
                    changeAccount = onChangeAccount,
                )
            }
            composable<Password> {
                LoginPasswordScreen(
                    modifier = modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    lockSetting = lockSetting,
                    onSuccess = { strategy, _ -> onSuccess(strategy) },
                    onCancel = onCancel,
                    onFallback = {
                        val destination = when {
                            LockType.Biometric in lockSetting.locks -> Biometric
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
                    biometricRecovery = { navController.navigate(BiometricRecoveryDestination.Biometric) },
                )
            }
            composable<PinRecovery> {
                RecoveryHelpScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    email = userAccountInfo.username,
                    onStartRecoveryClicked = { goToArk() },
                    onLostKeyClicked = { navController.navigate(LostKey) }
                )
            }
            composable<LostKey> {
                LostKeyScreen(
                    modifier = Modifier.padding(contentPadding),
                )
            }
            localLoginSecretTransferNavigation(
                contentPadding = contentPadding,
                onSuccess = { strategy -> onSuccess(strategy) },
                onCancel = onCancel
            )
            biometricRecoveryNavigation(
                navController = navController,
                contentPadding = contentPadding,
                userAccountInfo = userAccountInfo,
                lockSetting = lockSetting.copy(isLockCancelable = true),
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
    content: @Composable (PaddingValues) -> Unit
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
                content(PaddingValues())
            }
        }
    } else if (backgroundState.isTransparent) {
        Scaffold(
            modifier = Modifier.safeDrawingPadding(),
            containerColor = Color.Transparent,
        ) { contentPadding ->
            content(contentPadding)
        }
    } else {
        Scaffold(
            modifier = Modifier.safeDrawingPadding(),
            containerColor = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
        ) { contentPadding ->
            content(contentPadding)
        }
    }
}

data class LocalLoginBackgroundState(
    val isDialog: Boolean = false,
    val isTransparent: Boolean = false,
) : Serializable
