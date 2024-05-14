package com.dashlane.login.pages.secrettransfer

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.biometricsSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.pinSetupDestination
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.createaccount.passwordless.pincodesetup.PinSetupScreen
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.LoginIntents
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.LOGIN_KEY
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.totpDestination
import com.dashlane.login.pages.authenticator.compose.LoginDashlaneAuthenticatorScreen
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.EMAIL_KEY
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.authenticator
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.authorizeDestination
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.chooseTypeDestination
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.confirmEmailDestination
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.lostKeyDestination
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.qrCodeDestination
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.recoveryHelpDestination
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation.universalIntroDestination
import com.dashlane.login.pages.secrettransfer.authorize.AuthorizeScreen
import com.dashlane.login.pages.secrettransfer.choosetype.ChooseTypeScreen
import com.dashlane.login.pages.secrettransfer.confirmemail.ConfirmEmailScreen
import com.dashlane.login.pages.secrettransfer.help.RecoveryHelpScreen
import com.dashlane.login.pages.secrettransfer.help.lostkey.LostKeyScreen
import com.dashlane.login.pages.secrettransfer.qrcode.QrCodeScreen
import com.dashlane.login.pages.secrettransfer.universal.intro.UniversalIntroScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.util.compose.navigateAndPopupToStart

object LoginSecretTransferNavigation {
    const val chooseTypeDestination = "secretTransfer/chooseTypeDestination"
    const val universalIntroDestination = "secretTransfer/universal/intro"
    const val recoveryHelpDestination = "secretTransfer/help/recovery"
    const val lostKeyDestination = "secretTransfer/help/lostKey"
    const val qrCodeDestination = "secretTransfer/qrCode"
    const val confirmEmailDestination = "secretTransfer/confirmEmail"
    const val authorizeDestination = "secretTransfer/authorize"
    const val authenticator = "secretTransfer/authenticator"

    const val EMAIL_KEY = "email"
}

@Suppress("LongMethod")
@Composable
fun LoginSecretTransferNavigation(
    secretTransferAnalytics: SecretTransferAnalytics,
    loginSecretTransferViewModel: LoginSecretTransferViewModel = hiltViewModel(),
    startDestination: String,
    email: String?,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val goToArk: (RegisteredUserDevice) -> Unit = { registeredUserDevice ->
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

    NavHost(
        startDestination = startDestination,
        navController = navController

    ) {
        composable(chooseTypeDestination) {
            secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER)
            ChooseTypeScreen(
                viewModel = hiltViewModel(),
                email = email,
                onGoToUniversal = { email -> navController.navigate("$universalIntroDestination?$EMAIL_KEY=$email") },
                onGoToQr = { email -> navController.navigate("$qrCodeDestination?$EMAIL_KEY=$email") },
                onGoToHelp = { email -> navController.navigate("$recoveryHelpDestination?$EMAIL_KEY=$email") }
            )
        }
        composable(
            route = "$universalIntroDestination?$EMAIL_KEY={$EMAIL_KEY}",
            arguments = listOf(
                navArgument(EMAIL_KEY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            UniversalIntroScreen(
                viewModel = hiltViewModel(),
                email = backStackEntry.arguments?.getString(EMAIL_KEY),
                onCancel = onCancel,
                onSuccess = { secretTransferPayload, registeredUserDevice ->
                    loginSecretTransferViewModel.payloadFetched(secretTransferPayload)
                    loginSecretTransferViewModel.deviceRegistered(registeredUserDevice)
                    navController.navigate(authorizeDestination)
                },
                onGoToHelp = { email -> navController.navigate("$recoveryHelpDestination?$EMAIL_KEY=$email") }
            )
        }
        composable(
            route = "$recoveryHelpDestination?$EMAIL_KEY={$EMAIL_KEY}",
            arguments = listOf(
                navArgument(EMAIL_KEY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_ACCOUNT_RECOVERY_KEY)
            RecoveryHelpScreen(
                viewModel = hiltViewModel(),
                email = backStackEntry.arguments?.getString(EMAIL_KEY),
                onStartRecoveryClicked = goToArk,
                onLostKeyClicked = { navController.navigate(lostKeyDestination) }
            )
        }
        composable(route = lostKeyDestination) { LostKeyScreen() }
        composable(
            route = "$qrCodeDestination?$EMAIL_KEY={$EMAIL_KEY}",
            arguments = listOf(
                navArgument(EMAIL_KEY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            QrCodeScreen(
                viewModel = hiltViewModel(),
                email = backStackEntry.arguments?.getString(EMAIL_KEY),
                onQrScanned = { secretTransferPayload ->
                    loginSecretTransferViewModel.payloadFetched(secretTransferPayload)
                    navController.navigate("$confirmEmailDestination/${secretTransferPayload.login}")
                },
                onGoToARK = goToArk,
                onGoToUniversalD2D = { email -> navController.navigateAndPopupToStart(route = "$universalIntroDestination?$EMAIL_KEY=$email") },
                onCancelled = onCancel
            )
        }
        composable(
            route = "$confirmEmailDestination/{$EMAIL_KEY}",
            arguments = listOf(navArgument(EMAIL_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString(EMAIL_KEY)
            val payload = loginSecretTransferViewModel.uiState.value.data.secretTransferPayload
                ?: throw IllegalStateException("SecretTransferPayload cannot be empty")
            ConfirmEmailScreen(
                viewModel = hiltViewModel(),
                secretTransferPayload = payload,
                goToTOTP = { navController.navigate("$totpDestination/$email") },
                goToAuthenticator = { navController.navigate("$authenticator/$email") },
                onCancelled = onCancel,
                onLoginSuccess = { registeredUserDevice ->
                    loginSecretTransferViewModel.deviceRegistered(registeredUserDevice)
                    navController.navigate(authorizeDestination)
                }
            )
        }
        composable(
            route = "$totpDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType })
        ) {
            LoginTotpScreen(
                viewModel = hiltViewModel(),
                login = it.arguments?.getString(LOGIN_KEY) ?: "",
                goToNext = { registeredUserDevice, _ ->
                    loginSecretTransferViewModel.deviceRegistered(
                        registeredUserDevice = registeredUserDevice as? RegisteredUserDevice.Remote
                            ?: throw IllegalStateException("Should always be Remote for SecretTransfer")
                    )
                    navController.navigate(authorizeDestination)
                }
            )
        }
        composable(
            route = "$authenticator/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString(LOGIN_KEY)
            LoginDashlaneAuthenticatorScreen(
                viewModel = hiltViewModel(),
                goToNext = { registeredUserDevice, _ ->
                    loginSecretTransferViewModel.deviceRegistered(
                        registeredUserDevice = registeredUserDevice as? RegisteredUserDevice.Remote
                            ?: throw IllegalStateException("Should always be Remote for SecretTransfer")
                    )
                    navController.navigate(authorizeDestination)
                },
                cancel = {
                    navController.popBackStack()
                    navController.navigate("$totpDestination/$email")
                }
            )
        }
        composable(route = authorizeDestination) {
            AuthorizeScreen(
                viewModel = loginSecretTransferViewModel,
                onCancelled = onCancel,
                onSuccess = { accountType ->
                    when (accountType) {
                        UserAccountInfo.AccountType.InvisibleMasterPassword -> navController.navigate(pinSetupDestination)
                        UserAccountInfo.AccountType.MasterPassword -> onSuccess()
                    }
                }
            )
        }
        composable(route = pinSetupDestination) {
            secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_SET_PIN)
            PinSetupScreen(
                viewModel = hiltViewModel(),
                onPinChosen = { pin ->
                    loginSecretTransferViewModel.pinSetup(pin)
                    navController.navigate(biometricsSetupDestination)
                }
            )
        }
        composable(route = biometricsSetupDestination) {
            secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_SETUP_BIOMETRICS)
            BiometricsSetupScreen(
                viewModel = hiltViewModel(),
                onSkip = {
                    secretTransferAnalytics.completeDeviceTransfer(false)
                    onSuccess()
                },
                onBiometricsDisabled = {
                    secretTransferAnalytics.completeDeviceTransfer(false)
                    onSuccess()
                },
                onBiometricsEnabled = {
                    loginSecretTransferViewModel.onEnableBiometrics()
                    secretTransferAnalytics.completeDeviceTransfer(true)
                    onSuccess()
                }
            )
        }
    }
}