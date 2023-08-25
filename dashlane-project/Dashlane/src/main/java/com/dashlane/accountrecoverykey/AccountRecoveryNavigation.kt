package com.dashlane.accountrecoverykey

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.accountrecoverykey.activation.confirm.AccountRecoveryKeyConfirmScreen
import com.dashlane.accountrecoverykey.activation.generate.AccountRecoveryKeyGenerateScreen
import com.dashlane.accountrecoverykey.activation.intro.AccountRecoveryKeyActivationIntroScreen
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeyDetailSettingScreen

@Composable
fun AccountRecoveryNavigation() {
    val navController = rememberNavController()
    val detailSettingDestination = "detailSetting"
    val introDestination = "intro"
    val generateDestination = "generate"
    val confirmDestination = "confirm"

    NavHost(
        startDestination = detailSettingDestination,
        navController = navController
    ) {
        composable(detailSettingDestination) {
            AccountRecoveryKeyDetailSettingScreen(
                viewModel = hiltViewModel(),
                goToIntro = { navController.navigate(introDestination) }
            )
        }
        composable(introDestination) {
            AccountRecoveryKeyActivationIntroScreen(
                onGenerateKeyClicked = { navController.navigate(generateDestination) }
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
                finish = { navController.popBackStack(introDestination, true) },
                cancel = { navController.popBackStack(detailSettingDestination, true) }
            )
        }
    }
}