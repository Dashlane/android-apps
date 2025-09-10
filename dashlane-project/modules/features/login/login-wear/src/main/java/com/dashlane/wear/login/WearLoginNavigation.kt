package com.dashlane.wear.login

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import androidx.wear.compose.navigation.composable
import com.dashlane.navigation.popUpToTop
import com.dashlane.wear.login.WearLoginDestination.AuthorizationDestination
import com.dashlane.wear.login.WearLoginDestination.FreeUserPaywallDestination
import com.dashlane.wear.login.WearLoginDestination.LoginRootDestination
import com.dashlane.wear.login.WearLoginDestination.QrCodeDestination
import com.dashlane.wear.login.WearLoginDestination.SecondFactorDestination
import com.dashlane.wear.login.WearLoginDestination.SetupCredentialManagerDestination
import com.dashlane.wear.login.WearLoginDestination.TokenDestination
import com.dashlane.wear.login.WearLoginDestination.WelcomeDestination
import com.dashlane.wear.login.common.credentialmanager.WearSetupCredentialManagerScreen
import com.dashlane.wear.login.common.secondfactor.WearSecondFactorInputScreen
import com.dashlane.wear.login.paywall.FreeUserPaywallScreen
import com.dashlane.wear.login.remote.authorize.WearAuthorizeScreen
import com.dashlane.wear.login.remote.qr.WearQrCodeScreen
import com.dashlane.wear.login.remote.welcome.WearWelcomeScreen
import kotlinx.serialization.Serializable

@Suppress("LongMethod")
fun NavGraphBuilder.wearLoginNavigation(
    route: String,
    navHostController: NavHostController,
    onLoginSuccess: () -> Unit,
) {
    navigation(route = route, startDestination = LoginRootDestination.destination) {
        composable(LoginRootDestination.destination) {
            WearLoginScreen(
                viewModel = hiltViewModel(),
                goToWelcome = {
                    navHostController.navigate(route = WelcomeDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
                askSecondFactor = {
                    navHostController.navigate(route = SecondFactorDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
                onSessionRestored = { canSetupCredManager ->
                    if (canSetupCredManager) {
                        navHostController.navigate(route = SetupCredentialManagerDestination.destination) {
                            popUpToTop(navHostController)
                        }
                    } else {
                        onLoginSuccess()
                    }
                },
            )
        }
        composable(WelcomeDestination.destination) {
            WearWelcomeScreen(
                qrCodeClick = {
                    navHostController.navigate(route = QrCodeDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
            )
        }
        composable(QrCodeDestination.destination) {
            WearQrCodeScreen(
                viewModel = hiltViewModel(),
                goToOtpChallenge = {
                    navHostController.navigate(route = TokenDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
                goToAuthorize = {
                    navHostController.navigate(route = AuthorizationDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
            )
        }
        composable(FreeUserPaywallDestination.destination) {
            FreeUserPaywallScreen(
                viewModel = hiltViewModel(),
                onBack = {
                    navHostController.navigate(route = LoginRootDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
            )
        }
        composable(TokenDestination.destination) {
            WearSecondFactorInputScreen(
                viewModel = hiltViewModel(),
                onSuccess = {
                    navHostController.navigate(AuthorizationDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
            )
        }
        composable(AuthorizationDestination.destination) {
            WearAuthorizeScreen(
                viewModel = hiltViewModel(),
                onSuccess = {
                    navHostController.navigate(LoginRootDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
                onRetry = {
                    navHostController.navigate(route = QrCodeDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
                goToFreeUserPaywall = {
                    navHostController.navigate(route = FreeUserPaywallDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
            )
        }
        composable(SecondFactorDestination.destination) {
            WearSecondFactorInputScreen(
                viewModel = hiltViewModel(),
                onSuccess = {
                    navHostController.navigate(route = LoginRootDestination.destination) {
                        popUpToTop(navHostController)
                    }
                },
            )
        }
        composable(SetupCredentialManagerDestination.destination) {
            WearSetupCredentialManagerScreen(
                viewModel = hiltViewModel(),
                onSetupSuccess = onLoginSuccess,
            )
        }
    }
}



sealed interface WearLoginDestination {
    @Serializable
    data object LoginRootDestination : WearLoginDestination {
        val destination: String
            get() = "loginRoot"
    }

    @Serializable
    data object WelcomeDestination : WearLoginDestination {
        val destination: String
            get() = "welcome"
    }

    @Serializable
    data object QrCodeDestination : WearLoginDestination {
        val destination: String
            get() = "qrCode"
    }

    @Serializable
    data object TokenDestination : WearLoginDestination {
        val destination: String
            get() = "token"
    }

    @Serializable
    data object AuthorizationDestination : WearLoginDestination {
        val destination: String
            get() = "authorization"
    }

    @Serializable
    data object SecondFactorDestination : WearLoginDestination {
        val destination: String
            get() = "secondFactorAuthentication"
    }

    @Serializable
    data object FreeUserPaywallDestination : WearLoginDestination {
        val destination: String
            get() = "freeUserPaywall"
    }

    @Serializable
    data object SetupCredentialManagerDestination : WearLoginDestination {
        val destination: String
            get() = "setupCredentialManager"
    }
}