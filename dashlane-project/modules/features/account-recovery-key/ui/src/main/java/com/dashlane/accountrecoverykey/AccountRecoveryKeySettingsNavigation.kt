package com.dashlane.accountrecoverykey

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
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.DetailSettings
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.DetailSettings.toAccountRecoveryKeyNavigation
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Intro
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Success
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeyDetailSettingScreen
import com.dashlane.accountrecoverykey.ui.R
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState

@Suppress("LongMethod")
@Composable
fun AccountRecoveryKeySettingsNavigation(
    startDestination: AccountRecoveryKeyDestination,
    userCanExitFlow: Boolean = true,
    onCancel: () -> Unit,
) {
    val navController = rememberNavController()
    var topBarState by rememberSaveable { mutableStateOf(TopBarState()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    LaunchedEffect(navBackStackEntry) {
        topBarState = when (navBackStackEntry?.toAccountRecoveryKeyNavigation()) {
            Intro -> TopBarState(
                title = context.getString(R.string.account_recovery_key_detailed_setting_title),
                backEnabled = userCanExitFlow,
            )
            Success -> TopBarState(visible = false, backEnabled = false)
            else -> TopBarState(title = context.getString(R.string.account_recovery_key_detailed_setting_title))
        }
    }

    TopBarScaffold(
        topBarState = topBarState,
        back = { if (!navController.popBackStack()) onCancel() },
    ) { contentPadding ->
        NavHost(startDestination = startDestination, navController = navController) {
            val modifier = Modifier.padding(contentPadding)

            composable<DetailSettings> {
                AccountRecoveryKeyDetailSettingScreen(
                    modifier = modifier,
                    viewModel = hiltViewModel(),
                    goToIntro = { navController.navigate(Intro) },
                )
            }
            arkSetupGraph(
                navController = navController,
                modifier = modifier,
                onArkGenerated = {
                    if (startDestination is Intro) {
                        onCancel()
                    } else {
                        navController.popBackStack(Intro, true)
                    }
                },
                onCancel = { navController.popBackStack(DetailSettings, true) },
                userCanExitFlow = userCanExitFlow,
            )
        }
    }
}