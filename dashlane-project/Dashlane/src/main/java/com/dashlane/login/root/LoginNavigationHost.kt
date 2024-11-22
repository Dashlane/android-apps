package com.dashlane.login.root

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockSetting
import com.dashlane.login.LoginStrategy
import com.dashlane.login.pages.email.compose.LoginEmailScreen
import com.dashlane.login.pages.password.compose.LoginPasswordScreen
import com.dashlane.login.pages.secrettransfer.remoteLoginSecretTransferNavigation
import com.dashlane.login.pages.token.compose.LoginTokenScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.login.root.LoginDestination.Email
import com.dashlane.login.root.LoginDestination.Otp
import com.dashlane.login.root.LoginDestination.Password
import com.dashlane.login.root.LoginDestination.SecretTransfer
import com.dashlane.login.root.LoginDestination.Token

@Suppress("LongMethod")
@Composable
fun LoginNavigationHost(
    email: String?,
    allowSkipEmail: Boolean,
    lockSetting: LockSetting,
    goToCreateAccount: (String, Boolean) -> Unit,
    endOfLife: () -> Unit,
    onSuccess: (LoginStrategy.Strategy?, SsoInfo?) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = DashlaneTheme.colors.containerAgnosticNeutralSupershy
    ) { contentPadding ->
        NavHost(
            startDestination = Email(login = email, allowSkipEmail = allowSkipEmail),
            navController = navController
        ) {
            composable<Email> {
                LoginEmailScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    goToCreateAccount = goToCreateAccount,
                    goToOTP = { secondFactor -> navController.navigate(route = Otp(secondFactor.login)) },
                    goToPassword = { navController.navigate(route = Password) },
                    goToSecretTransfer = { email, showQrCode ->
                        navController.navigate(
                            route = SecretTransfer(login = email, showQrCode = showQrCode)
                        )
                    },
                    goToToken = { secondFactor -> navController.navigate(route = Token(secondFactor.login)) },
                    ssoSuccess = {
                        
                        onSuccess(null, null)
                    },
                    endOfLife = endOfLife
                )
            }
            composable<Otp> {
                LoginTotpScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    verificationMode = VerificationMode.OTP1,
                    goToNext = { navController.navigate(route = Password) }
                )
            }
            composable<Token> {
                LoginTokenScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    goToNext = { navController.navigate(route = Password) }
                )
            }
            composable<Password> {
                LoginPasswordScreen(
                    modifier = Modifier
                        .padding(contentPadding)
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
                    }
                )
            }
            remoteLoginSecretTransferNavigation(
                contentPadding = contentPadding,
                onSuccess = { strategy -> onSuccess(strategy, null) },
                onCancel = { navController.popBackStack(Email(email, allowSkipEmail), false) }
            )
        }
    }
}
