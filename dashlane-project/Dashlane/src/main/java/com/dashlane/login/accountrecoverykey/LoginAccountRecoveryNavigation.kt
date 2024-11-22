package com.dashlane.login.accountrecoverykey

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.changemasterpassword.ChangeMasterPasswordScreen
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginStrategy
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.BiometricsSetup
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.ChangeMasterPassword
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.EmailToken
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.EnterArk
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.Intro
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.PinSetup
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.Recovery
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryDestination.Totp
import com.dashlane.login.accountrecoverykey.enterark.EnterARKScreen
import com.dashlane.login.accountrecoverykey.intro.IntroScreen
import com.dashlane.login.accountrecoverykey.recovery.RecoveryScreen
import com.dashlane.login.pages.token.compose.LoginTokenScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.pin.setup.PinSetupScreen
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.compose.navigateAndPopupToStart

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

    mainViewModel.updateAccountType(accountType)

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
    ) { contentPadding ->
        NavHost(
            startDestination = Intro(login),
            navController = navController
        ) {
            composable<Intro> {
                IntroScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    registeredUserDevice = registeredUserDevice,
                    authTicket = authTicket,
                    goToARK = { navController.navigateAndPopupToStart(EnterArk(login)) },
                    goToToken = { navController.navigateAndPopupToStart(EmailToken(login)) },
                    goToTOTP = { navController.navigateAndPopupToStart(Totp(login)) },
                    onCancel = onCancel
                )
            }

            composable<EnterArk> {
                EnterARKScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onSuccess = { obfuscatedVaultKey ->
                        mainViewModel.vaultKeyDecrypted(obfuscatedVaultKey)
                        when (accountType) {
                            UserAccountInfo.AccountType.InvisibleMasterPassword -> navController.navigate(
                                PinSetup
                            )
                            UserAccountInfo.AccountType.MasterPassword -> navController.navigate(
                                ChangeMasterPassword
                            )
                        }
                    }
                )
            }
            composable<EmailToken> {
                LoginTokenScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    goToNext = { navController.navigate(EnterArk(login)) }
                )
            }
            composable<Totp> {
                LoginTotpScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    verificationMode = VerificationMode.OTP2,
                    goToNext = { navController.navigate(EnterArk(login)) }
                )
            }
            composable<ChangeMasterPassword> {
                ChangeMasterPasswordScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    goToNext = {
                        mainViewModel.masterPasswordChanged(it)
                        navController.navigateAndPopupToStart(Recovery)
                    },
                    goBack = { navController.popBackStack() }
                )
            }
            composable<Recovery> {
                RecoveryScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onSuccess = onSuccess,
                    onCancel = onCancel
                )
            }
            composable<PinSetup> {
                PinSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    isCancellable = false,
                    onPinChosen = { pin ->
                        mainViewModel.pinSetup(pin)
                        navController.navigate(BiometricsSetup)
                    }
                )
            }
            composable<BiometricsSetup> {
                val onSkip = {
                    mainViewModel.onSkipBiometric()
                    navController.navigateAndPopupToStart(Recovery)
                }
                BiometricsSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onSkip = onSkip,
                    onBiometricsDisabled = onSkip,
                    onBiometricsEnabled = {
                        mainViewModel.onEnableBiometrics()
                        navController.navigateAndPopupToStart(Recovery)
                    }
                )
            }
        }
    }
}
