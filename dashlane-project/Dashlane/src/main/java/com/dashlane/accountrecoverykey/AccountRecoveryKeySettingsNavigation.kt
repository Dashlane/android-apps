package com.dashlane.accountrecoverykey

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeyDetailSettingScreen

@Composable
fun AccountRecoveryKeySettingsNavigation() {
    val navController = rememberNavController()
    val detailSettingDestination = "detailSetting"
    val arkSetupDestination = "arkSetup"

    NavHost(
        startDestination = detailSettingDestination,
        navController = navController
    ) {
        composable(detailSettingDestination) {
            AccountRecoveryKeyDetailSettingScreen(
                viewModel = hiltViewModel(),
                goToIntro = { navController.navigate(arkSetupDestination) }
            )
        }
        arkSetupGraph(
            navController = navController,
            arkRoute = arkSetupDestination,
            onArkGenerated = { navController.popBackStack(arkSetupDestination, true) },
            onCancel = { navController.popBackStack(detailSettingDestination, true) },
            userCanSkip = false,
            userCanExitFlow = true
        )
    }
}
