package com.dashlane.login.pages.secrettransfer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.createaccount.passwordless.MplessDestination.BiometricsSetup
import com.dashlane.createaccount.passwordless.MplessDestination.PinSetup
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginIntents
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.Totp
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.Authorize
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.ChooseType
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.ConfirmEmail
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.LostKey
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.QrCode
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.RecoveryHelp
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferDestination.UniversalIntro
import com.dashlane.login.pages.secrettransfer.authorize.AuthorizeScreen
import com.dashlane.login.pages.secrettransfer.choosetype.ChooseTypeScreen
import com.dashlane.login.pages.secrettransfer.confirmemail.ConfirmEmailScreen
import com.dashlane.login.pages.secrettransfer.help.RecoveryHelpScreen
import com.dashlane.login.pages.secrettransfer.help.lostkey.LostKeyScreen
import com.dashlane.login.pages.secrettransfer.universal.intro.UniversalIntroScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.login.root.LocalLoginDestination
import com.dashlane.login.root.LoginDestination
import com.dashlane.pin.setup.PinSetupScreen
import com.dashlane.secrettransfer.qrcode.QrCodeScreen
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.compose.navigateAndPopupToStart

@Suppress("LongMethod")
@Composable
fun LoginSecretTransferNavigation(
    loginSecretTransferViewModel: LoginSecretTransferViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    startDestination: LoginSecretTransferDestination,
    email: String?,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
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

    val onSuccessWithLog: (LoginStrategy.Strategy) -> Unit = { strategy ->
        loginSecretTransferViewModel.logPageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SUCCESS)
        onSuccess(strategy)
    }

    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        composable<ChooseType> {
            loginSecretTransferViewModel.logPageView(AnyPage.LOGIN_DEVICE_TRANSFER)
            ChooseTypeScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                email = email,
                onGoToUniversal = { email -> navController.navigate(UniversalIntro(email)) },
                onGoToQr = { email -> navController.navigate(QrCode(email)) },
                onGoToHelp = { email -> navController.navigate(RecoveryHelp(email)) }
            )
        }
        composable<UniversalIntro> { backStackEntry ->
            UniversalIntroScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                email = backStackEntry.toRoute<UniversalIntro>().email,
                onCancel = onCancel,
                onSuccess = { secretTransferPayload ->
                    loginSecretTransferViewModel.payloadFetched(secretTransferPayload)
                    navController.navigate(Authorize)
                },
                onGoToHelp = { email -> navController.navigate(RecoveryHelp(email)) }
            )
        }
        composable<RecoveryHelp> { backStackEntry ->
            loginSecretTransferViewModel.logPageView(AnyPage.LOGIN_DEVICE_TRANSFER_ACCOUNT_RECOVERY_KEY)
            RecoveryHelpScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                email = backStackEntry.toRoute<RecoveryHelp>().email,
                onStartRecoveryClicked = goToArk,
                onLostKeyClicked = { navController.navigate(LostKey) }
            )
        }
        composable<LostKey> { LostKeyScreen() }
        composable<QrCode> { backStackEntry ->
            QrCodeScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                email = backStackEntry.toRoute<QrCode>().email,
                onQrScanned = { secretTransferPayload ->
                    loginSecretTransferViewModel.payloadFetched(secretTransferPayload)
                    navController.navigate(ConfirmEmail(secretTransferPayload.login))
                },
                onGoToARK = goToArk,
                onGoToUniversalD2D = { email ->
                    navController.navigateAndPopupToStart(UniversalIntro(email))
                },
                onCancelled = onCancel
            )
        }
        composable<ConfirmEmail> { backStackEntry ->
            val confirmEmail = backStackEntry.toRoute<ConfirmEmail>().email
            val payload = loginSecretTransferViewModel.uiState.value.data.secretTransferPayload
                ?: throw IllegalStateException("SecretTransferPayload cannot be empty")
            ConfirmEmailScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                secretTransferPayload = payload,
                goToTOTP = { navController.navigate(Totp(confirmEmail)) },
                onCancelled = onCancel,
                onLoginSuccess = { navController.navigate(Authorize) }
            )
        }
        composable<Totp> {
            LoginTotpScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                verificationMode = VerificationMode.OTP2,
                goToNext = { navController.navigate(Authorize) }
            )
        }
        composable<Authorize> {
            AuthorizeScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = loginSecretTransferViewModel,
                onCancelled = onCancel,
                onSuccess = { accountType ->
                    when (accountType) {
                        UserAccountInfo.AccountType.InvisibleMasterPassword ->
                            navController.navigate(PinSetup)
                        UserAccountInfo.AccountType.MasterPassword ->
                            onSuccessWithLog(LoginStrategy.Strategy.NoStrategy)
                    }
                }
            )
        }
        composable<PinSetup> {
            loginSecretTransferViewModel.logPageView(AnyPage.LOGIN_DEVICE_TRANSFER_SET_PIN)
            PinSetupScreen(
                viewModel = hiltViewModel(),
                isCancellable = false,
                onPinChosen = { pin ->
                    loginSecretTransferViewModel.pinSetup(pin)
                    navController.navigate(BiometricsSetup)
                }
            )
        }
        composable<BiometricsSetup> {
            loginSecretTransferViewModel.logPageView(AnyPage.LOGIN_DEVICE_TRANSFER_SETUP_BIOMETRICS)
            BiometricsSetupScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                onSkip = {
                    loginSecretTransferViewModel.logCompleteTransfer(biometricsEnabled = false)
                    onSuccessWithLog(LoginStrategy.Strategy.MplessD2D)
                },
                onBiometricsDisabled = {
                    loginSecretTransferViewModel.logCompleteTransfer(biometricsEnabled = false)
                    onSuccessWithLog(LoginStrategy.Strategy.MplessD2D)
                },
                onBiometricsEnabled = {
                    loginSecretTransferViewModel.onEnableBiometrics()
                    loginSecretTransferViewModel.logCompleteTransfer(biometricsEnabled = true)
                    onSuccessWithLog(LoginStrategy.Strategy.MplessD2D)
                }
            )
        }
    }
}

fun NavGraphBuilder.remoteLoginSecretTransferNavigation(
    contentPadding: PaddingValues = PaddingValues(),
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit
) {
    composable<LoginDestination.SecretTransfer> { backStackEntry ->
        val args = backStackEntry.toRoute<LoginDestination.SecretTransfer>()
        LoginSecretTransferNavigation(
            startDestination = if (args.showQrCode) QrCode(null) else ChooseType,
            contentPadding = contentPadding,
            email = args.login,
            onSuccess = onSuccess,
            onCancel = onCancel
        )
    }
}

fun NavGraphBuilder.localLoginSecretTransferNavigation(
    contentPadding: PaddingValues = PaddingValues(),
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit
) {
    composable<LocalLoginDestination.SecretTransfer> { backStackEntry ->
        val args = backStackEntry.toRoute<LocalLoginDestination.SecretTransfer>()
        LoginSecretTransferNavigation(
            startDestination = ChooseType,
            contentPadding = contentPadding,
            email = args.login,
            onSuccess = onSuccess,
            onCancel = onCancel
        )
    }
}