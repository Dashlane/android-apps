package com.dashlane.accountrecoverykey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.common.compose.components.TopBarState
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun AccountRecoveryKeySettingsNavigation(
    startDestination: AccountRecoveryKeyDestination,
    userCanExitFlow: Boolean = true,
    onCancel: () -> Unit,
) {
    val navController = rememberNavController()
    val topBarState = rememberSaveable { (mutableStateOf(TopBarState())) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    LaunchedEffect(navBackStackEntry) {
        val topBarStateValue = when (navBackStackEntry?.toAccountRecoveryKeyNavigation()) {
            Intro -> TopBarState(
                title = context.getString(R.string.account_recovery_key_detailed_setting_title),
                backEnabled = userCanExitFlow
            )
            Success -> TopBarState(visible = false, backEnabled = false)
            else -> TopBarState(title = context.getString(R.string.account_recovery_key_detailed_setting_title))
        }
        topBarState.value = topBarStateValue
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            AnimatedVisibility(
                visible = topBarState.value.visible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                content = {
                    DashlaneTopAppBar(
                        navigationIcon = {
                            if (topBarState.value.backEnabled) {
                                IconButton(onClick = { if (!navController.popBackStack()) onCancel() }) {
                                    Icon(
                                        token = IconTokens.arrowLeftOutlined,
                                        contentDescription = null,
                                        tint = DashlaneTheme.colors.textNeutralCatchy
                                    )
                                }
                            }
                        },
                        text = topBarState.value.title,
                    )
                }
            )
        }
    ) { contentPadding ->
        NavHost(
            startDestination = startDestination,
            navController = navController
        ) {
            composable<DetailSettings> {
                AccountRecoveryKeyDetailSettingScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    goToIntro = { navController.navigate(Intro) }
                )
            }
            arkSetupGraph(
                navController = navController,
                contentPadding = contentPadding,
                onArkGenerated = {
                    if (startDestination is Intro) {
                        onCancel()
                    } else {
                        navController.popBackStack(Intro, true)
                    }
                },
                onCancel = { navController.popBackStack(DetailSettings, true) },
                userCanExitFlow = userCanExitFlow
            )
        }
    }
}
