package com.dashlane.login.root

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.pages.authenticator.compose.LoginDashlaneAuthenticatorScreen
import com.dashlane.login.pages.email.compose.LoginEmailScreen
import com.dashlane.login.pages.password.compose.LoginPasswordScreen
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation
import com.dashlane.login.pages.secrettransfer.secretTransferNavigation
import com.dashlane.login.pages.token.compose.LoginTokenScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.login.root.LoginDestination.ALLOW_SKIP_EMAIL_KEY
import com.dashlane.login.root.LoginDestination.AUTHENTICATOR_ENABLED_KEY
import com.dashlane.login.root.LoginDestination.LOGIN_KEY
import com.dashlane.login.root.LoginDestination.authenticatorDestination
import com.dashlane.login.root.LoginDestination.emailDestination
import com.dashlane.login.root.LoginDestination.otpDestination
import com.dashlane.login.root.LoginDestination.passwordDestination
import com.dashlane.login.root.LoginDestination.secretTransferDestination
import com.dashlane.login.root.LoginDestination.tokenDestination

@Suppress("LongMethod")
@Composable
fun LoginNavigationHost(
    viewModel: LoginViewModel = hiltViewModel(),
    email: String?,
    allowSkipEmail: Boolean,
    lockSetting: LockSetting,
    goToCreateAccount: (String, Boolean) -> Unit,
    endOfLife: () -> Unit,
    onSuccess: (LoginStrategy.Strategy?, SsoInfo?) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        startDestination = emailDestination,
        navController = navController
    ) {
        composable(
            route = "$emailDestination?$LOGIN_KEY={$LOGIN_KEY}&$ALLOW_SKIP_EMAIL_KEY={$ALLOW_SKIP_EMAIL_KEY}",
            arguments = listOf(
                navArgument(LOGIN_KEY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = email
                },
                navArgument(ALLOW_SKIP_EMAIL_KEY) {
                    type = NavType.BoolType
                    defaultValue = allowSkipEmail
                }
            )
        ) {
            LoginEmailScreen(
                viewModel = hiltViewModel(),
                goToCreateAccount = goToCreateAccount,
                goToAuthenticator = { secondFactor, ssoInfo ->
                    viewModel.updateSsoInfo(ssoInfo)
                    navController.navigate(route = "$authenticatorDestination/${secondFactor.login}")
                },
                goToOTP = { secondFactor, ssoInfo ->
                    viewModel.updateSsoInfo(ssoInfo)
                    navController.navigate(
                        route = "$otpDestination/${secondFactor.login}?$AUTHENTICATOR_ENABLED_KEY=${secondFactor.isAuthenticatorEnabled}"
                    )
                },
                goToPassword = { registeredUserDevice, ssoInfo ->
                    viewModel.updateSsoInfo(ssoInfo)
                    viewModel.deviceRegistered(registeredUserDevice = registeredUserDevice, authTicket = null)
                    navController.navigate(route = passwordDestination)
                },
                goToSecretTransfer = { email, startDestination ->
                    navController.navigate(route = "$secretTransferDestination?${LoginSecretTransferNavigation.START_DESTINATION_KEY}=$startDestination&$LOGIN_KEY=$email")
                },
                goToToken = { secondFactor, ssoInfo ->
                    viewModel.updateSsoInfo(ssoInfo)
                    navController.navigate(route = "$tokenDestination/${secondFactor.login}")
                },
                ssoSuccess = {
                    
                    onSuccess(null, null)
                },
                endOfLife = endOfLife
            )
        }
        composable(
            route = "$authenticatorDestination/{$LOGIN_KEY}?",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString(LOGIN_KEY)
            LoginDashlaneAuthenticatorScreen(
                viewModel = hiltViewModel(),
                goToNext = { registeredUserDevice, authTicket ->
                    viewModel.deviceRegistered(registeredUserDevice = registeredUserDevice, authTicket = authTicket)
                    navController.navigate(route = passwordDestination)
                },
                cancel = {
                    navController.navigate(route = "$otpDestination/$email") {
                        popUpTo(emailDestination)
                    }
                }
            )
        }
        composable(
            route = "$otpDestination/{$LOGIN_KEY}?$AUTHENTICATOR_ENABLED_KEY={$AUTHENTICATOR_ENABLED_KEY}",
            arguments = listOf(
                navArgument(LOGIN_KEY) { type = NavType.StringType },
                navArgument(AUTHENTICATOR_ENABLED_KEY) { type = NavType.BoolType; defaultValue = false }
            )
        ) {
            LoginTotpScreen(
                viewModel = hiltViewModel(),
                verificationMode = VerificationMode.OTP1,
                goToNext = { registeredUserDevice, authTicket ->
                    viewModel.deviceRegistered(registeredUserDevice = registeredUserDevice, authTicket = authTicket)
                    navController.navigate(route = passwordDestination)
                },
                goToPush = { email ->
                    navController.navigate(route = "$authenticatorDestination/$email")
                }
            )
        }
        composable(
            route = "$tokenDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType })
        ) {
            LoginTokenScreen(
                viewModel = hiltViewModel(),
                goToNext = { registeredUserDevice, authTicket ->
                    viewModel.deviceRegistered(registeredUserDevice, authTicket)
                    navController.navigate(route = passwordDestination)
                }
            )
        }
        composable(route = passwordDestination) {
            LoginPasswordScreen(
                modifier = Modifier.fillMaxHeight(),
                viewModel = hiltViewModel(),
                lockSetting = lockSetting,
                onSuccess = onSuccess,
                onCancel = { navController.popBackStack(emailDestination, false) },
                onFallback = { navController.popBackStack(emailDestination, false) },
                changeAccount = { email ->
                    val allowSkipEmail = true
                    navController.navigate("$emailDestination?$LOGIN_KEY=$email&$ALLOW_SKIP_EMAIL_KEY=$allowSkipEmail") {
                        popUpTo(emailDestination)
                    }
                },
                biometricRecovery = {
                    
                },
                logout = { email ->
                    navController.navigate("$emailDestination?$LOGIN_KEY=$email") {
                        popUpTo(emailDestination)
                    }
                }
            )
        }
        secretTransferNavigation(
            route = secretTransferDestination,
            onSuccess = { onSuccess(LoginStrategy.Strategy.NO_STRATEGY, null) },
            onCancel = { navController.popBackStack(emailDestination, false) }
        )
    }
}

object LoginDestination {
    const val emailDestination = "login/email"
    const val authenticatorDestination = "login/authenticator"
    const val otpDestination = "login/otp"
    const val passwordDestination = "login/password"
    const val tokenDestination = "login/token"
    const val secretTransferDestination = "login/secretTransfer"

    const val LOGIN_KEY = "login"
    const val ALLOW_SKIP_EMAIL_KEY = "allowSkipEmail"
    const val AUTHENTICATOR_ENABLED_KEY = "isAuthenticatorEnabled"

    const val LOGIN_SECRET_TRANSFER_START_DESTINATION_KEY = "loginSecretTransferStartDestination"
}
