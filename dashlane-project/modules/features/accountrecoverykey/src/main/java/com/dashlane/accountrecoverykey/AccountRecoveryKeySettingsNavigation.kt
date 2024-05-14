package com.dashlane.accountrecoverykey

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingsNavigation.arkSetupDestination
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingsNavigation.detailSettingDestination
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeyDetailSettingScreen
import com.dashlane.util.compose.navigateAndPopupToStart

object AccountRecoveryKeySettingsNavigation {
    const val detailSettingDestination = "detailSetting"
    const val arkSetupDestination = "arkSetup"
}

@Composable
fun AccountRecoveryKeySettingsNavigation(
    startDestination: String,
    userCanExitFlow: Boolean = true
) {
    val navController = rememberNavController()

    NavHost(
        startDestination = startDestination,
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
            onArkGenerated = {
                if (startDestination == arkSetupDestination) {
                    navController.navigateAndPopupToStart(detailSettingDestination)
                } else {
                    navController.popBackStack(arkSetupDestination, true)
                }
            },
            onCancel = { navController.popBackStack(detailSettingDestination, true) },
            userCanExitFlow = userCanExitFlow
        )
    }
}
