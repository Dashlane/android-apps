package com.dashlane.login.biometric.recovery

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.changemasterpassword.ChangeMasterPasswordScreen
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockSetting
import com.dashlane.login.LoginStrategy
import com.dashlane.login.biometric.LoginBiometricScreen
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Biometric
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.ChangeMp
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Otp
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Recovery
import com.dashlane.login.totp.LoginTotpScreen
import com.dashlane.login.totp.LoginTotpViewModel
import com.dashlane.user.UserAccountInfo
import kotlinx.serialization.Serializable


@Serializable
data object BiometricRecoveryNavigation

@Suppress("LongMethod")
fun NavGraphBuilder.biometricRecoveryNavigation(
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues(),
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    showSnackBar: (String) -> Unit,
    onSuccess: (LoginStrategy.Strategy) -> Unit,
    onCancel: () -> Unit,
    onLogout: () -> Unit,
) {
    
    
    val startDestination =
        if (userAccountInfo.securitySettings?.isTotp == true && !lockSetting.isMasterPasswordReset) {
            Otp(userAccountInfo.username)
        } else {
            Biometric(userAccountInfo.username)
        }

    navigation<BiometricRecoveryNavigation>(startDestination = startDestination) {
        composable<Otp> {
            val viewModel: LoginTotpViewModel = hiltViewModel()
            LoginTotpScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = viewModel,
                verificationMode = VerificationMode.OTP1,
                goToNext = { navController.navigate(Biometric(userAccountInfo.username)) },
            )
        }
        composable<Biometric> {
            LoginBiometricScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(contentPadding),
                viewModel = hiltViewModel(),
                userAccountInfo = userAccountInfo,
                lockSetting = lockSetting,
                isBiometricRecovery = true,
                showSnackBar = showSnackBar,
                onSuccess = {
                    navController.navigate(ChangeMp) {
                        popUpTo(Biometric(userAccountInfo.username)) {
                            inclusive = true
                        }
                    }
                },
                onCancel = { onCancel() },
                onFallback = { onCancel() },
                onLockout = { onCancel() },
                onLogout = { _, _ -> onLogout() },
            )
        }
        composable<ChangeMp> { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry<BiometricRecoveryNavigation>() }
            val biometricRecoveryViewModel = hiltViewModel<BiometricRecoveryViewModel>(parentEntry)

            ChangeMasterPasswordScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = hiltViewModel(),
                origin = ChangeMasterPasswordOrigin.BiometricRecovery,
                hasSteps = false,
                goToNext = {
                    navController.navigate(Recovery) {
                        popUpTo(ChangeMp) {
                            inclusive = true
                        }
                    }
                },
                goBack = {
                    biometricRecoveryViewModel.cancel()
                    onCancel()
                },
            )
        }
        composable<Recovery> { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry<BiometricRecoveryNavigation>() }
            val biometricRecoveryViewModel = hiltViewModel<BiometricRecoveryViewModel>(parentEntry)

            BiometricRecoveryScreen(
                modifier = Modifier.padding(contentPadding),
                viewModel = biometricRecoveryViewModel,
                onSuccess = onSuccess,
                onCancel = onCancel,
            )
        }
    }
}