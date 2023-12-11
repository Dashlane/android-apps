package com.dashlane.accountrecoverykey

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dashlane.accountrecoverykey.activation.confirm.AccountRecoveryKeyConfirmScreen
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateScreen
import com.dashlane.accountrecoverykey.activation.intro.AccountRecoveryKeyActivationIntroScreen

fun NavGraphBuilder.arkSetupGraph(
    navController: NavController,
    arkRoute: String,
    onArkGenerated: () -> Unit,
    onCancel: () -> Unit,
    userCanSkip: Boolean,
    userCanExitFlow: Boolean
) {
    val introDestination = "intro"
    val generateDestination = "generate"
    val confirmDestination = "confirm"

    navigation(
        startDestination = introDestination,
        route = arkRoute
    ) {
        composable(introDestination) {
            AccountRecoveryKeyActivationIntroScreen(
                viewModel = hiltViewModel(),
                onBackPressed = { navController.navigateUp() },
                onGenerateKeyClicked = { navController.navigate(generateDestination) },
                onCancelClicked = onCancel,
                userCanSkip = userCanSkip,
                userCanExitFlow = userCanExitFlow
            )
        }
        composable(generateDestination) {
            AccountRecoveryKeyGenerateScreen(
                viewModel = hiltViewModel(),
                goToConfirm = { navController.navigate(confirmDestination) },
                cancel = { navController.popBackStack() }
            )
        }
        composable(confirmDestination) {
            AccountRecoveryKeyConfirmScreen(
                viewModel = hiltViewModel(),
                back = { navController.popBackStack() },
                finish = onArkGenerated,
                cancel = onCancel
            )
        }
    }
}