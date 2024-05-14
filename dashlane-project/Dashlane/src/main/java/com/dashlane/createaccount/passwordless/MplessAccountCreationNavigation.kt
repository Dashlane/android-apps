package com.dashlane.createaccount.passwordless

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dashlane.R
import com.dashlane.accountrecoverykey.AccountRecoveryKeySetupNavigation
import com.dashlane.accountrecoverykey.arkSetupGraph
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.arkSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.biometricsSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.confirmationDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.infoDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.pinSetupDestination
import com.dashlane.createaccount.passwordless.MplessAccountCreationNavigation.termsAndConditionsDestination
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.createaccount.passwordless.confirmation.ConfirmationScreen
import com.dashlane.createaccount.passwordless.info.InfoScreen
import com.dashlane.createaccount.passwordless.pincodesetup.PinSetupScreen
import com.dashlane.createaccount.passwordless.termsandconditions.TermsAndConditionsScreen
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.ui.widgets.compose.TopBarState
import com.dashlane.ui.widgets.compose.system.DashlaneTopAppBar
import com.dashlane.util.compose.navigateAndPopupToStart

object MplessAccountCreationNavigation {
    const val infoDestination = "infoScreen"
    const val pinSetupDestination = "pinSetupScreen"
    const val biometricsSetupDestination = "biometricsSetupScreen"
    const val termsAndConditionsDestination = "termsAndConditionsScreen"
    const val confirmationDestination = "confirmationScreen"
    const val arkSetupDestination = "arkSetup"
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun MplessAccountCreationNavigation(
    viewModel: MplessAccountCreationViewModel,
    onCancel: () -> Unit,
    onAccountCreated: () -> Unit,
    displayErrorMessage: (Int) -> Unit,
    displayExpirationErrorMessage: () -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit
) {
    val navController = rememberNavController()
    val topBarState = rememberSaveable { (mutableStateOf(TopBarState())) }
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val topBarStateValue = when (navBackStackEntry?.destination?.route) {
            infoDestination -> TopBarState(title = context.getString(R.string.passwordless_info_screen_app_bar_title))
            pinSetupDestination -> TopBarState()
            biometricsSetupDestination -> TopBarState(title = context.getString(R.string.passwordless_biometrics_setup_app_bar_title))
            termsAndConditionsDestination -> TopBarState(title = context.getString(R.string.passwordless_terms_and_conditions_app_bar_title))
            confirmationDestination -> TopBarState(visible = false)
            AccountRecoveryKeySetupNavigation.introDestination -> TopBarState(visible = false)
            AccountRecoveryKeySetupNavigation.generateDestination -> TopBarState(
                visible = true,
                title = context.getString(R.string.account_recovery_key_setting_title)
            )
            AccountRecoveryKeySetupNavigation.confirmDestination -> TopBarState(title = context.getString(R.string.account_recovery_key_setting_title))
            AccountRecoveryKeySetupNavigation.successDestination -> TopBarState(visible = false)
            else -> TopBarState()
        }
        topBarState.value = topBarStateValue
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = topBarState.value.visible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                content = {
                    DashlaneTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = {
                                if (!navController.navigateUp()) onCancel()
                            }) {
                                Icon(
                                    token = IconTokens.arrowLeftOutlined,
                                    contentDescription = null,
                                    tint = DashlaneTheme.colors.textNeutralCatchy
                                )
                            }
                        },
                        text = topBarState.value.title,
                    )
                }
            )
        }
    ) { contentPadding ->
        NavHost(
            startDestination = infoDestination,
            navController = navController
        ) {
            composable(infoDestination) {
                InfoScreen(
                    modifier = Modifier.padding(contentPadding),
                    onLearnMoreClick = { onOpenHelpCenterPage(HelpCenterLink.ARTICLE_MASTER_PASSWORDLESS_ACCOUNT_INFO.uri) },
                    onNextClick = { navController.navigate(pinSetupDestination) }
                )
            }
            composable(pinSetupDestination) {
                PinSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onPinChosen = { newPin ->
                        viewModel.onNewPin(newPin)
                        navController.navigate(biometricsSetupDestination)
                    }
                )
            }
            composable(biometricsSetupDestination) {
                BiometricsSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onSkip = {
                        viewModel.onEnableBiometrics(false)
                        navController.navigate(termsAndConditionsDestination)
                    },
                    onBiometricsDisabled = {
                        viewModel.onEnableBiometrics(false)
                        navController.navigate(termsAndConditionsDestination) {
                            popUpTo(pinSetupDestination)
                        }
                    },
                    onBiometricsEnabled = {
                        viewModel.onEnableBiometrics(true)
                        navController.navigate(termsAndConditionsDestination)
                    }
                )
            }
            composable(termsAndConditionsDestination) {
                TermsAndConditionsScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = viewModel,
                    onCreateAccount = { navController.navigate(confirmationDestination) },
                    onOpenHelpCenterPage = onOpenHelpCenterPage
                )
            }
            composable(confirmationDestination) {
                ConfirmationScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    mpLessViewModel = viewModel,
                    onAccountCreated = { navController.navigateAndPopupToStart(arkSetupDestination) },
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
            arkSetupGraph(
                navController = navController,
                contentPadding = contentPadding,
                arkRoute = arkSetupDestination,
                onArkGenerated = onAccountCreated,
                onCancel = onAccountCreated,
                userCanExitFlow = false
            )
        }
    }
}
