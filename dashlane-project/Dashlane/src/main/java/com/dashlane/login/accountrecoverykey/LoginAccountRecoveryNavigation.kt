package com.dashlane.login.accountrecoverykey

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.createaccount.passwordless.pincodesetup.PinSetupScreen
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.LOGIN_KEY
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.changeMasterPasswordDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.biometricsSetupDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.emailTokenDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.enterARKDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.introDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.pinSetupDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.recoveryDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.totpDestination
import com.dashlane.login.accountrecoverykey.enterark.EnterARKScreen
import com.dashlane.login.accountrecoverykey.intro.IntroScreen
import com.dashlane.login.accountrecoverykey.recovery.RecoveryScreen
import com.dashlane.login.pages.token.compose.LoginTokenScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.masterpassword.compose.ChangeMasterPasswordScreen

object LoginAccountRecoveryNavigation {
    const val introDestination = "ark/intro"
    const val emailTokenDestination = "ark/emailToken"
    const val totpDestination = "ark/totp"
    const val enterARKDestination = "ark/enterARK"
    const val recoveryDestination = "ark/recovery"
    const val pinSetupDestination = "ark/pin"
    const val biometricsSetupDestination = "ark/biometric"
    const val changeMasterPasswordDestination = "ark/changeMasterPassword"

    const val LOGIN_KEY = "login"
}

@Suppress("LongMethod")
@Composable
fun LoginAccountRecoveryNavigation(
    mainViewModel: LoginAccountRecoveryKeyViewModel,
    registeredUserDevice: RegisteredUserDevice,
    accountType: UserAccountInfo.AccountType,
    authTicket: String?,
    onSuccess: (LoginStrategy.Strategy?) -> Unit,
    onCancel: () -> Unit,
) {
    val navController = rememberNavController()
    val login = registeredUserDevice.login

    NavHost(
        startDestination = "$introDestination/{$LOGIN_KEY}",
        navController = navController
    ) {
        composable(
            route = "$introDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType; defaultValue = login })
        ) {
            IntroScreen(
                viewModel = hiltViewModel(),
                registeredUserDevice = registeredUserDevice,
                authTicket = authTicket,
                accountType = accountType,
                goToARK = { navController.navigateAndPopup("$enterARKDestination/$login") },
                goToToken = { navController.navigateAndPopup("$emailTokenDestination/$login") },
                goToTOTP = { navController.navigateAndPopup("$totpDestination/$login") },
                onCancel = onCancel
            )
        }

        composable(
            route = "$enterARKDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType; defaultValue = login })
        ) {
            EnterARKScreen(
                viewModel = hiltViewModel(),
                onSuccess = { obfuscatedVaultKey ->
                    mainViewModel.vaultKeyDecrypted(obfuscatedVaultKey)
                    when (accountType) {
                        UserAccountInfo.AccountType.InvisibleMasterPassword -> navController.navigate(pinSetupDestination)
                        UserAccountInfo.AccountType.MasterPassword -> navController.navigate(changeMasterPasswordDestination)
                    }
                }
            )
        }
        composable(
            route = "$emailTokenDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType; defaultValue = login })
        ) {
            LoginTokenScreen(
                viewModel = hiltViewModel(),
                login = it.arguments?.getString(LOGIN_KEY) ?: "",
                goToNext = { registeredUserDevice, authTicket ->
                    mainViewModel.deviceRegistered(registeredUserDevice, authTicket)
                    navController.navigate("$enterARKDestination/$login")
                }
            )
        }
        composable(route = "$totpDestination/{$LOGIN_KEY}", arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType })) {
            LoginTotpScreen(
                viewModel = hiltViewModel(),
                login = it.arguments?.getString(LOGIN_KEY) ?: "",
                goToNext = { registeredUserDevice, authTicket ->
                    mainViewModel.deviceRegistered(registeredUserDevice, authTicket)
                    navController.navigate("$enterARKDestination/$login")
                }
            )
        }
        composable(route = changeMasterPasswordDestination) {
            ChangeMasterPasswordScreen(
                viewModel = hiltViewModel(),
                goToNext = {
                    mainViewModel.masterPasswordChanged(it)
                    navController.navigateAndPopup(recoveryDestination)
                },
                goBack = { navController.popBackStack() }
            )
        }
        composable(route = recoveryDestination) {
            RecoveryScreen(
                viewModel = hiltViewModel(),
                onSuccess = onSuccess,
                onCancel = onCancel
            )
        }
        composable(route = pinSetupDestination) {
            PinSetupScreen(
                viewModel = hiltViewModel(),
                onPinChosen = { pin ->
                    mainViewModel.pinSetup(pin)
                    navController.navigate(biometricsSetupDestination)
                }
            )
        }
        composable(route = biometricsSetupDestination) {
            BiometricsSetupScreen(
                viewModel = hiltViewModel(),
                onSkip = {
                    mainViewModel.onSkipBiometric()
                    navController.navigateAndPopup(recoveryDestination)
                },
                onBiometricsEnabled = {
                    mainViewModel.onEnableBiometrics()
                    navController.navigateAndPopup(recoveryDestination)
                }
            )
        }
    }
}

fun NavController.navigateAndPopup(route: String) {
    navigate(route) {
        currentBackStackEntry?.destination?.route?.let { route ->
            popUpTo(route) {
                inclusive = true
            }
        }
    }
}
