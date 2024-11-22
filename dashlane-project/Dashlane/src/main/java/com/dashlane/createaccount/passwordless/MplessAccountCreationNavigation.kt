package com.dashlane.createaccount.passwordless

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Confirm
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Confirm.toAccountRecoveryKeyNavigation
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Intro
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Success
import com.dashlane.accountrecoverykey.arkSetupGraph
import com.dashlane.createaccount.passwordless.MplessDestination.BiometricsSetup
import com.dashlane.createaccount.passwordless.MplessDestination.BiometricsSetup.toMpLessDestination
import com.dashlane.createaccount.passwordless.MplessDestination.Confirmation
import com.dashlane.createaccount.passwordless.MplessDestination.Info
import com.dashlane.createaccount.passwordless.MplessDestination.PinSetup
import com.dashlane.createaccount.passwordless.MplessDestination.TermsAndConditions
import com.dashlane.createaccount.passwordless.biometrics.BiometricsSetupScreen
import com.dashlane.createaccount.passwordless.confirmation.ConfirmationScreen
import com.dashlane.createaccount.passwordless.info.InfoScreen
import com.dashlane.createaccount.passwordless.termsandconditions.TermsAndConditionsScreen
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.pin.setup.PinSetupScreen
import com.dashlane.ui.common.compose.components.TopBarState
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar
import com.dashlane.util.compose.navigateAndPopupToStart

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
        val topBarStateValue = when (navBackStackEntry?.toMpLessDestination()) {
            Info -> TopBarState(title = context.getString(R.string.passwordless_info_screen_app_bar_title))
            PinSetup -> TopBarState()
            BiometricsSetup -> TopBarState(title = context.getString(R.string.passwordless_biometrics_setup_app_bar_title))
            TermsAndConditions -> TopBarState(title = context.getString(R.string.passwordless_terms_and_conditions_app_bar_title))
            Confirmation -> TopBarState(visible = false)
            else -> when (navBackStackEntry?.toAccountRecoveryKeyNavigation()) {
                Confirm -> TopBarState(title = context.getString(R.string.account_recovery_key_setting_title))
                Success -> TopBarState(visible = false)
                else -> TopBarState()
            }
        }
        topBarState.value = topBarStateValue
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
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
            startDestination = Info,
            navController = navController
        ) {
            composable<Info> {
                InfoScreen(
                    modifier = Modifier.padding(contentPadding),
                    onLearnMoreClick = { onOpenHelpCenterPage(HelpCenterLink.ARTICLE_MASTER_PASSWORDLESS_ACCOUNT_INFO.androidUri) },
                    onNextClick = { navController.navigate(PinSetup) }
                )
            }
            composable<PinSetup> {
                PinSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    isCancellable = false,
                    onPinChosen = { newPin ->
                        viewModel.onNewPin(newPin)
                        navController.navigate(BiometricsSetup)
                    }
                )
            }
            composable<BiometricsSetup> {
                BiometricsSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onSkip = {
                        viewModel.onEnableBiometrics(false)
                        navController.navigate(TermsAndConditions)
                    },
                    onBiometricsDisabled = {
                        viewModel.onEnableBiometrics(false)
                        navController.navigate(TermsAndConditions) {
                            popUpTo(PinSetup)
                        }
                    },
                    onBiometricsEnabled = {
                        viewModel.onEnableBiometrics(true)
                        navController.navigate(TermsAndConditions)
                    }
                )
            }
            composable<TermsAndConditions> {
                TermsAndConditionsScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = viewModel,
                    onCreateAccount = { navController.navigate(Confirmation) },
                    onOpenHelpCenterPage = onOpenHelpCenterPage
                )
            }
            composable<Confirmation> {
                ConfirmationScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    mpLessViewModel = viewModel,
                    onAccountCreated = { navController.navigateAndPopupToStart(Intro) },
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
                onArkGenerated = onAccountCreated,
                onCancel = onAccountCreated,
                userCanExitFlow = false
            )
        }
    }
}
