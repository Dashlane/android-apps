package com.dashlane.createaccount.passwordless

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.R
import com.dashlane.accountrecoverykey.arkSetupGraph
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.arkSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.biometricsSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.confirmationDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.infoDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.pinSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.termsAndConditionsDestination
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.createaccount.passwordless.confirmation.ConfirmationScreen
import com.dashlane.createaccount.passwordless.infoscreen.InfoScreen
import com.dashlane.createaccount.passwordless.pincodesetup.PinSetupScreen
import com.dashlane.createaccount.passwordless.termsandconditions.TermsAndConditionsScreen
import com.dashlane.design.iconography.IconTokens
import com.dashlane.help.HelpCenterLink
import com.dashlane.ui.widgets.compose.basescreen.AppBarScreenWrapper
import com.dashlane.ui.widgets.compose.basescreen.NoAppBarScreenWrapper

object MplessAccountCreationNavigation {
    const val infoDestination = "infoScreen"
    const val pinSetupDestination = "pinSetupScreen"
    const val biometricsSetupDestination = "biometricsSetupScreen"
    const val termsAndConditionsDestination = "termsAndConditionsScreen"
    const val confirmationDestination = "confirmationScreen"
    const val arkSetupDestination = "arkSetup"
}

@Suppress("LongMethod")
@Composable
fun MplessAccountCreationNavigation(
    viewModel: MplessAccountCreationViewModel,
    onCancel: () -> Unit,
    onRequireLockScreen: () -> Unit,
    onAccountCreated: () -> Unit,
    displayErrorMessage: (Int) -> Unit,
    displayExpirationErrorMessage: () -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit
) {
    val navController = rememberNavController()

    fun onLockScreenSetup(block: () -> Unit) {
        if (viewModel.isUserAllowedToUsePin()) {
            block()
        } else {
            onRequireLockScreen()
        }
    }

    NavHost(
        startDestination = infoDestination,
        navController = navController
    ) {
        composable(infoDestination) {
            AppBarScreenWrapper(
                titleText = stringResource(R.string.passwordless_info_screen_app_bar_title),
                navigationIconToken = IconTokens.arrowLeftOutlined,
                onNavigationClick = onCancel
            ) {
                InfoScreen(
                    onLearnMoreClick = { onOpenHelpCenterPage(HelpCenterLink.ARTICLE_MASTER_PASSWORDLESS_ACCOUNT_INFO.uri) },
                    onNextClick = {
                        onLockScreenSetup {
                            navController.navigate(pinSetupDestination)
                        }
                    }
                )
            }
        }
        composable(pinSetupDestination) {
            NoAppBarScreenWrapper(
                navigationIconToken = IconTokens.arrowLeftOutlined,
                onNavigationClick = navController::popBackStack
            ) {
                PinSetupScreen(
                    viewModel = hiltViewModel(),
                    onPinChosen = { newPin ->
                        viewModel.onNewPin(newPin)
                        navController.navigate(biometricsSetupDestination)
                    }
                )
            }
        }
        composable(biometricsSetupDestination) {
            AppBarScreenWrapper(
                titleText = stringResource(R.string.passwordless_biometrics_setup_app_bar_title),
                navigationIconToken = IconTokens.arrowLeftOutlined,
                onNavigationClick = navController::popBackStack
            ) {
                BiometricsSetupScreen(
                    viewModel = hiltViewModel(),
                    onSkip = {
                        viewModel.onEnableBiometrics(false)
                        navController.navigate(termsAndConditionsDestination)
                    },
                    onBiometricsEnabled = {
                        viewModel.onEnableBiometrics(true)
                        navController.navigate(termsAndConditionsDestination)
                    }
                )
            }
        }
        composable(termsAndConditionsDestination) {
            AppBarScreenWrapper(
                titleText = stringResource(R.string.passwordless_terms_and_conditions_app_bar_title),
                navigationIconToken = IconTokens.arrowLeftOutlined,
                onNavigationClick = navController::popBackStack
            ) {
                TermsAndConditionsScreen(
                    viewModel = viewModel,
                    onCreateAccount = {
                        onLockScreenSetup {
                            navController.navigate(confirmationDestination)
                        }
                    },
                    onOpenHelpCenterPage = onOpenHelpCenterPage
                )
            }
        }
        composable(confirmationDestination) {
            NoAppBarScreenWrapper {
                ConfirmationScreen(
                    viewModel = hiltViewModel(),
                    mpLessViewModel = viewModel,
                    onAccountCreated = { navController.navigate(arkSetupDestination) },
                    onErrorMessageToDisplay = { errorRes ->
                        displayErrorMessage(errorRes)
                        navController.popBackStack()
                    },
                    onApplicationVersionExpired = {
                        displayExpirationErrorMessage()
                        navController.popBackStack()
                    }
                )
            }
        }
        arkSetupGraph(
            navController = navController,
            arkRoute = arkSetupDestination,
            onArkGenerated = onAccountCreated,
            onCancel = onAccountCreated,
            userCanSkip = true,
            userCanExitFlow = false
        )
    }
}