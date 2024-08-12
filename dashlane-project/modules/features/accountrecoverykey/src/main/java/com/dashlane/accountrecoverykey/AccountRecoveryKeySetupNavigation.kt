package com.dashlane.accountrecoverykey

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dashlane.accountrecoverykey.AccountRecoveryKeySetupNavigation.confirmDestination
import com.dashlane.accountrecoverykey.AccountRecoveryKeySetupNavigation.generateDestination
import com.dashlane.accountrecoverykey.AccountRecoveryKeySetupNavigation.introDestination
import com.dashlane.accountrecoverykey.AccountRecoveryKeySetupNavigation.successDestination
import com.dashlane.accountrecoverykey.activation.confirm.AccountRecoveryKeyConfirmScreen
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateScreen
import com.dashlane.accountrecoverykey.activation.intro.AccountRecoveryKeyActivationIntroScreen
import com.dashlane.accountrecoverykey.activation.success.AccountRecoveryKeySuccessScreen

object AccountRecoveryKeySetupNavigation {
    const val introDestination = "ark/intro"
    const val generateDestination = "ark/generate"
    const val confirmDestination = "ark/confirm"
    const val successDestination = "ark/success"
}

fun NavGraphBuilder.arkSetupGraph(
    navController: NavController,
    contentPadding: PaddingValues = PaddingValues(),
    onArkGenerated: () -> Unit,
    onCancel: () -> Unit,
    userCanExitFlow: Boolean
) {
    composable(introDestination) {
        AccountRecoveryKeyActivationIntroScreen(
            modifier = Modifier.padding(contentPadding),
            viewModel = hiltViewModel(),
            onBackPressed = { navController.navigateUp() },
            onGenerateKeyClicked = { navController.navigate(generateDestination) },
            userCanExitFlow = userCanExitFlow
        )
    }
    composable(generateDestination) {
        AccountRecoveryKeyGenerateScreen(
            modifier = Modifier.padding(contentPadding),
            viewModel = hiltViewModel(),
            goToConfirm = { navController.navigate(confirmDestination) },
            cancel = { navController.popBackStack() }
        )
    }
    composable(confirmDestination) {
        AccountRecoveryKeyConfirmScreen(
            modifier = Modifier.padding(contentPadding),
            viewModel = hiltViewModel(),
            back = { navController.popBackStack() },
            success = { navController.navigate(successDestination) },
            cancel = onCancel
        )
    }
    composable(successDestination) {
        AccountRecoveryKeySuccessScreen(
            modifier = Modifier.padding(contentPadding),
            done = onArkGenerated,
        )
    }
}