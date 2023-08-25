package com.dashlane.login.accountrecoverykey

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.LOGIN_KEY
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.emailTokenDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.enterARKDestination
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation.totpDestination
import com.dashlane.login.accountrecoverykey.enterark.EnterARKScreen
import com.dashlane.login.pages.token.compose.LoginTokenScreen
import com.dashlane.login.pages.totp.compose.LoginTotpScreen

object LoginAccountRecoveryNavigation {
    const val emailTokenDestination = "emailToken"
    const val totpDestination = "totp"
    const val enterARKDestination = "enterARK"

    const val LOGIN_KEY = "login"
}

@Composable
fun LoginAccountRecoveryNavigation(
    mainViewModel: LoginAccountRecoveryKeyViewModel,
    login: String
) {
    val navController = rememberNavController()

    NavHost(
        startDestination = "$enterARKDestination/{$LOGIN_KEY}",
        navController = navController
    ) {
        composable(
            route = "$enterARKDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType; defaultValue = login })
        ) {
            EnterARKScreen(
                mainViewModel = mainViewModel,
                viewModel = hiltViewModel(),
                goToToken = { navController.navigate("$emailTokenDestination/$login") },
                goToTOTP = { navController.navigate("$totpDestination/$login") },
            )
        }
        composable(
            route = "$emailTokenDestination/{$LOGIN_KEY}",
            arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType; defaultValue = login })
        ) {
            LoginTokenScreen(
                viewModel = hiltViewModel(),
                login = it.arguments?.getString(LOGIN_KEY) ?: "",
                goToNext = { registeredUserDevice, authTicket ->
                    mainViewModel.deviceRegistered(registeredUserDevice, authTicket)
                    navController.popBackStack()
                }
            )
        }
        composable(route = "$totpDestination/{$LOGIN_KEY}", arguments = listOf(navArgument(LOGIN_KEY) { type = NavType.StringType })) {
            LoginTotpScreen(
                viewModel = hiltViewModel(),
                login = it.arguments?.getString(LOGIN_KEY) ?: "",
                goToNext = { registeredUserDevice, authTicket ->
                    mainViewModel.deviceRegistered(registeredUserDevice, authTicket)
                    navController.popBackStack()
                }
            )
        }
    }
}
