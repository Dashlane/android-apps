package com.dashlane.accountsummary

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dashlane.accountsummary.AccountSummaryDestination.AccountSummary
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeContactEmail
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmail
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmailPending
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmailVerification
import com.dashlane.accountsummary.changecontact.ChangeContactEmailScreen
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailScreen
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationScreen
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState

@Suppress("LongMethod")
@Composable
fun AccountSummaryNavigation(
    goToLock: () -> Unit,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
    onSuccessLogout: () -> Unit,
) {
    val navController = rememberNavController()
    var topBarState by rememberSaveable { mutableStateOf(TopBarState()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    LaunchedEffect(navBackStackEntry) {
        topBarState = when (navBackStackEntry?.toAccountSummaryNavigation()) {
            ChangeLoginEmailPending -> TopBarState(visible = false, backEnabled = false)
            else -> TopBarState(title = context.getString(R.string.account_status_settings_title))
        }
    }

    TopBarScaffold(
        topBarState = topBarState,
        back = { if (!navController.popBackStack()) onCancel() },
    ) { contentPadding ->
        NavHost(
            startDestination = AccountSummary,
            navController = navController,
        ) {
            val modifier = Modifier.padding(contentPadding)
            composable<AccountSummary> {
                AccountSummaryScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    goToLock = goToLock,
                    goToChangeContactEmail = { navController.navigate(ChangeContactEmail) },
                    goToChangeLoginEmail = { navController.navigate(ChangeLoginEmail) },
                )
            }
            composable<ChangeContactEmail> {
                ChangeContactEmailScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                )
            }
            composable<ChangeLoginEmail> {
                ChangeLoginEmailScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                    onNext = { newEmail -> navController.navigate(ChangeLoginEmailVerification(newEmail)) },
                )
            }
            composable<ChangeLoginEmailVerification> {
                EmailVerificationScreen(
                    modifier = modifier,
                    updateTopBarState = { topBarState = it },
                    viewModel = hiltViewModel(),
                    onSuccess = onSuccess,
                    onSuccessLogout = onSuccessLogout,
                )
            }
        }
    }
}