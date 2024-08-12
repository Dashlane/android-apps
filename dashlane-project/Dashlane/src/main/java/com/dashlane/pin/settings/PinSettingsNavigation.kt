package com.dashlane.pin.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dashlane.navigation.ObfuscatedByteArrayParamType
import com.dashlane.pin.settings.PinSettingsNavigation.PIN_KEY
import com.dashlane.pin.settings.PinSettingsNavigation.setupDestination
import com.dashlane.pin.settings.PinSettingsNavigation.successDestination
import com.dashlane.pin.settings.success.PinSettingsSuccessScreen
import com.dashlane.pin.setup.PinSetupScreen

object PinSettingsNavigation {
    const val PIN_KEY = "pin"

    const val setupDestination = "pin/setup"
    const val successDestination = "pin/success"
}

@Composable
fun PinSettingsNavHost(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        startDestination = setupDestination,
        navController = navController
    ) {
        composable(setupDestination) {
            PinSetupScreen(
                viewModel = hiltViewModel(),
                isCancellable = true,
                onPinChosen = { pin ->
                    navController.navigate("$successDestination/$pin")
                },
                onCancel = onCancel,
            )
        }
        composable(
            "$successDestination/{$PIN_KEY}",
            arguments = listOf(navArgument(PIN_KEY) { type = ObfuscatedByteArrayParamType() })
        ) {
            PinSettingsSuccessScreen(
                viewModel = hiltViewModel(),
                onSuccess = onSuccess,
                onCancel = onCancel,
            )
        }
    }
}