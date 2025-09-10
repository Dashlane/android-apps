package com.dashlane.login.root

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.data.login.SsoInfo
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockSetting
import com.dashlane.login.LoginDestination.Email
import com.dashlane.login.LoginDestination.Otp
import com.dashlane.login.LoginDestination.Password
import com.dashlane.login.LoginDestination.SecretTransfer
import com.dashlane.login.LoginDestination.Token
import com.dashlane.login.LoginStrategy
import com.dashlane.login.email.LoginEmailScreen
import com.dashlane.login.pages.secrettransfer.remoteLoginSecretTransferNavigation
import com.dashlane.login.password.LoginPasswordScreen
import com.dashlane.login.token.LoginTokenScreen
import com.dashlane.login.totp.LoginTotpScreen
import com.dashlane.user.UserAccountInfo


@Suppress("LongMethod", "kotlin:S107")
@Composable
fun LoginNavigationHost(
    contentPadding: PaddingValues = PaddingValues(),
    email: String?,
    allowSkipEmail: Boolean,
    lockSetting: LockSetting,
    goToCreateAccount: (String, Boolean) -> Unit,
    endOfLife: () -> Unit,
    onSuccess: (LoginStrategy.Strategy?, SsoInfo?) -> Unit,
    goToArk: (RegisteredUserDevice?, UserAccountInfo.AccountType) -> Unit,
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.padding(contentPadding),
        color = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
    ) {
        NavHost(
            startDestination = Email(login = email, allowSkipEmail = allowSkipEmail),
            navController = navController,
        ) {
            composable<Email> {
                LoginEmailScreen(
                    viewModel = hiltViewModel(),
                    goToCreateAccount = goToCreateAccount,
                    goToOTP = { secondFactor -> navController.navigate(route = Otp(secondFactor.login)) },
                    goToPassword = { navController.navigate(route = Password) },
                    goToSecretTransfer = { email, showQrCode ->
                        navController.navigate(
                            route = SecretTransfer(login = email, showQrCode = showQrCode),
                        )
                    },
                    goToToken = { secondFactor -> navController.navigate(route = Token(secondFactor.login)) },
                    ssoSuccess = {
                        
                        onSuccess(null, null)
                    },
                    endOfLife = endOfLife,
                )
            }
            composable<Otp> {
                LoginTotpScreen(
                    viewModel = hiltViewModel(),
                    verificationMode = VerificationMode.OTP1,
                    goToNext = { navController.navigate(route = Password) },
                )
            }
            composable<Token> {
                LoginTokenScreen(
                    viewModel = hiltViewModel(),
                    goToNext = { navController.navigate(route = Password) },
                )
            }
            composable<Password> {
                LoginPasswordScreen(
                    modifier = Modifier
                        .fillMaxHeight(),
                    viewModel = hiltViewModel(),
                    lockSetting = lockSetting,
                    onSuccess = onSuccess,
                    onCancel = {
                        
                    },
                    onFallback = {
                        
                    },
                    changeAccount = { email ->
                        val allowSkipEmail = true
                        navController.navigate(Email(email, allowSkipEmail)) {
                            popUpTo(Email(email, allowSkipEmail))
                        }
                    },
                    biometricRecovery = {
                        
                    },
                    logout = { email ->
                        navController.navigate(Email(email, allowSkipEmail)) {
                            popUpTo(Email(email, allowSkipEmail))
                        }
                    },
                    goToArk = { accountType -> goToArk(null, accountType) },
                )
            }
            remoteLoginSecretTransferNavigation(
                goToArk = { registeredUserDevice, accountType -> goToArk(registeredUserDevice, accountType) },
                onSuccess = { strategy -> onSuccess(strategy, null) },
                onCancel = { navController.popBackStack(Email(email, allowSkipEmail), false) },
            )
        }
    }
}