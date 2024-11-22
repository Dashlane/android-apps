package com.dashlane.accountrecoverykey

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Confirm
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Generate
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Intro
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Success
import com.dashlane.accountrecoverykey.activation.confirm.AccountRecoveryKeyConfirmScreen
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateScreen
import com.dashlane.accountrecoverykey.activation.intro.AccountRecoveryKeyActivationIntroScreen
import com.dashlane.accountrecoverykey.activation.success.AccountRecoveryKeySuccessScreen

fun NavGraphBuilder.arkSetupGraph(
    navController: NavController,
    contentPadding: PaddingValues = PaddingValues(),
    onArkGenerated: () -> Unit,
    onCancel: () -> Unit,
    userCanExitFlow: Boolean
) {
    composable<Intro> {
        AccountRecoveryKeyActivationIntroScreen(
            modifier = Modifier.padding(contentPadding),
            viewModel = hiltViewModel(),
            userCanExitFlow = userCanExitFlow,
            onBackPressed = { navController.navigateUp() },
            onGenerateKeyClicked = { navController.navigate(Generate) }
        )
    }
    composable<Generate> {
        AccountRecoveryKeyGenerateScreen(
            modifier = Modifier.padding(contentPadding),
            viewModel = hiltViewModel(),
            userCanExitFlow = userCanExitFlow,
            goToConfirm = { navController.navigate(Confirm) },
            cancel = { navController.popBackStack() },
        )
    }
    composable<Confirm> {
        AccountRecoveryKeyConfirmScreen(
            modifier = Modifier.padding(contentPadding),
            viewModel = hiltViewModel(),
            back = { navController.popBackStack() },
            success = { navController.navigate(Success) },
            cancel = onCancel
        )
    }
    composable<Success> {
        AccountRecoveryKeySuccessScreen(
            modifier = Modifier.padding(contentPadding),
            done = onArkGenerated,
        )
    }
}