package com.dashlane.wear.settings

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import androidx.wear.compose.navigation.composable
import com.dashlane.wear.settings.WearVaultDestination.SettingsLogoutDestination
import com.dashlane.wear.settings.WearVaultDestination.SettingsMainDestination
import com.dashlane.wear.settings.logout.WearConfirmLogoutScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.wearSettingsNavigation(
    route: String,
    navHostController: NavHostController,
    onLogoutComplete: () -> Unit,
) {
    navigation(route = route, startDestination = SettingsMainDestination.destination) {
        composable(SettingsMainDestination.destination) {
            WearSettingsScreen(
                viewModel = hiltViewModel(),
                askLogoutConfirmation = {
                    navHostController.navigate(SettingsLogoutDestination.destination)
                },
            )
        }
        composable(SettingsLogoutDestination.destination) {
            WearConfirmLogoutScreen(
                viewModel = hiltViewModel(),
                onCancel = { navHostController.navigateUp() },
                onLogoutComplete = onLogoutComplete,
            )
        }
    }
}



sealed interface WearVaultDestination {
    @Serializable
    data object SettingsMainDestination : WearVaultDestination {
        val destination: String
            get() = "settingsMain"
    }

    @Serializable
    data object SettingsLogoutDestination : WearVaultDestination {
        val destination: String
            get() = "settingsLogout"
    }
}