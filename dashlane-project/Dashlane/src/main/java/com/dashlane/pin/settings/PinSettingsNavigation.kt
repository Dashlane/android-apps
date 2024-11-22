package com.dashlane.pin.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.pin.settings.PinSettingsDestination.SetupDestination
import com.dashlane.pin.settings.PinSettingsDestination.SuccessDestination
import com.dashlane.pin.settings.success.PinSettingsSuccessScreen
import com.dashlane.pin.setup.PinSetupScreen

@Composable
fun PinSettingsNavHost(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = DashlaneTheme.colors.containerAgnosticNeutralSupershy
    ) { contentPadding ->
        NavHost(
            startDestination = SetupDestination,
            navController = navController
        ) {
            composable<SetupDestination> {
                PinSetupScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    isCancellable = true,
                    onPinChosen = { pin ->
                        navController.navigate(SuccessDestination(pin))
                    },
                    onCancel = onCancel,
                )
            }
            composable<SuccessDestination> {
                PinSettingsSuccessScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    onSuccess = onSuccess,
                    onCancel = onCancel,
                )
            }
        }
    }
}