package com.dashlane.createaccount

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.createaccount.CreateAccountDestination.BiometricSetup
import com.dashlane.createaccount.CreateAccountDestination.Email
import com.dashlane.createaccount.CreateAccountDestination.Password
import com.dashlane.createaccount.CreateAccountDestination.TermsAndConditions
import com.dashlane.createaccount.biometric.CreateAccountBiometricScreen
import com.dashlane.createaccount.email.CreateAccountEmailScreen
import com.dashlane.createaccount.password.CreateAccountPasswordScreen
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsScreen
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets

@Composable
fun CreateAccountNavigation(
    startDestination: CreateAccountDestination,
    goToPasswordless: (String) -> Unit,
    success: () -> Unit,
    onHelpCenter: (Uri?) -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
    ) { contentPadding ->
        NavHost(
            startDestination = startDestination,
            navController = navController,
        ) {
            val modifier = Modifier.padding(contentPadding)
            composable<Email> {
                CreateAccountEmailScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    cancel = { navController.popBackStack() },
                    goToNext = { email, isB2B -> navController.navigate(Password(email, isB2B)) },
                    onHelpCenter = onHelpCenter,
                )
            }
            composable<Password> {
                CreateAccountPasswordScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    goBack = { navController.popBackStack() },
                    goToBiometricSetup = { navController.navigate(BiometricSetup) },
                    goToTermsAndConditions = { navController.navigate(TermsAndConditions) },
                    goToPasswordless = goToPasswordless,
                )
            }
            composable<BiometricSetup> {
                CreateAccountBiometricScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    onSuccess = { navController.navigate(TermsAndConditions) },
                )
            }
            composable<TermsAndConditions> {
                CreateAccountTermsAndConditionsScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    onSuccess = success,
                    onOpenHelpCenterPage = onHelpCenter,
                )
            }
        }
    }
}