package com.dashlane.login.pages.biometric.recovery

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.dashlane.user.UserAccountInfo
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.pages.biometric.compose.LoginBiometricScreen
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryNavigation.biometricDestination
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryNavigation.biometricRecoveryRoute
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryNavigation.changeMpDestination
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryNavigation.otpDestination
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryNavigation.recoveryDestination
import com.dashlane.login.pages.totp.compose.LoginTotpScreen
import com.dashlane.login.root.LocalLoginDestination
import com.dashlane.login.root.LoginDestination
import com.dashlane.masterpassword.compose.ChangeMasterPasswordScreen

object BiometricRecoveryNavigation {
    const val biometricRecoveryRoute = "biometricRecovery"
    const val otpDestination = "$biometricRecoveryRoute/otp"
    const val biometricDestination = "$biometricRecoveryRoute/biometric"
    const val changeMpDestination = "$biometricRecoveryRoute/changeMp"
    const val recoveryDestination = "$biometricRecoveryRoute/success"
}

@Suppress("LongMethod")
fun NavGraphBuilder.biometricRecoveryNavigation(
    navController: NavHostController,
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit,
    onLogout: () -> Unit,
) {
    
    
    val startDestination = if (userAccountInfo.securitySettings?.isTotp == true && !lockSetting.isMasterPasswordReset) {
        otpDestination
    } else {
        biometricDestination
    }

    navigation(
        startDestination = startDestination,
        route = biometricRecoveryRoute,
    ) {
        composable(
            route = "$otpDestination?${LoginDestination.LOGIN_KEY}={${LoginDestination.LOGIN_KEY}}",
            arguments = listOf(
                navArgument(LocalLoginDestination.LOGIN_KEY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = userAccountInfo.username
                }
            )
        ) {
            LoginTotpScreen(
                viewModel = hiltViewModel(),
                verificationMode = VerificationMode.OTP1,
                goToNext = { _, _ -> navController.navigate(route = biometricDestination) },
                goToPush = {
                    
                }
            )
        }
        composable(biometricDestination) {
            LoginBiometricScreen(
                viewModel = hiltViewModel(),
                userAccountInfo = userAccountInfo,
                lockSetting = lockSetting,
                onSuccess = {
                    navController.navigate(changeMpDestination) {
                        popUpTo(biometricDestination) {
                            inclusive = true
                        }
                    }
                },
                onCancel = { onCancel() },
                onFallback = { onCancel() },
                onLockout = { onCancel() },
                onLogout = { _, _ -> onLogout() }
            )
        }
        composable(changeMpDestination) { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry(biometricRecoveryRoute) }
            val biometricRecoveryViewModel = hiltViewModel<BiometricRecoveryViewModel>(parentEntry)

            ChangeMasterPasswordScreen(
                viewModel = hiltViewModel(),
                hasSteps = false,
                goToNext = { newMasterPassword ->
                    biometricRecoveryViewModel.updateNewMasterPassword(newMasterPassword)
                    navController.navigate(recoveryDestination) {
                        popUpTo(changeMpDestination) {
                            inclusive = true
                        }
                    }
                },
                goBack = {
                    biometricRecoveryViewModel.cancel()
                    onCancel()
                }
            )
        }
        composable(recoveryDestination) { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry(biometricRecoveryRoute) }
            val biometricRecoveryViewModel = hiltViewModel<BiometricRecoveryViewModel>(parentEntry)

            BiometricRecoveryScreen(
                viewModel = biometricRecoveryViewModel,
                onSuccess = onSuccess,
                onCancel = onCancel
            )
        }
    }
}