package com.dashlane.changemasterpassword

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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.ChangeMasterPassword
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.Success
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.Success.toChangeMasterPasswordNavigation
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.Warning
import com.dashlane.changemasterpassword.success.ChangeMasterPasswordSuccessScreen
import com.dashlane.changemasterpassword.ui.R
import com.dashlane.changemasterpassword.warning.ChangeMasterPasswordWarningScreen
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import kotlinx.serialization.Serializable

@Suppress("LongMethod")
@Composable
fun ChangeMasterPasswordNavigation(
    startDestination: ChangeMasterPasswordNavigation,
    origin: ChangeMasterPasswordOrigin,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    logout: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var topBarState by rememberSaveable { mutableStateOf(TopBarState()) }
    val context = LocalContext.current
    val isMigrationToMp = origin is ChangeMasterPasswordOrigin.Migration

    LaunchedEffect(navBackStackEntry) {
        val backStackEntry = navBackStackEntry?.toChangeMasterPasswordNavigation()
        topBarState = when {
            isMigrationToMp -> TopBarState(visible = false)
            backStackEntry is Success -> TopBarState(visible = false)
            else -> TopBarState(
                visible = true,
                title = context.getString(R.string.change_master_password_activity_label),
            )
        }
    }

    TopBarScaffold(
        topBarState = topBarState,
        back = { if (isMigrationToMp) logout() else onCancel() },
    ) { contentPadding ->
        NavHost(
            startDestination = startDestination,
            navController = navController,
        ) {
            composable<Warning> {
                ChangeMasterPasswordWarningScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    origin = origin,
                    goToNext = { navController.navigate(ChangeMasterPassword) },
                    cancel = onCancel,
                    logout = logout,
                )
            }
            composable<ChangeMasterPassword> {
                ChangeMasterPasswordScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    origin = origin,
                    hasSteps = false,
                    goToNext = { navController.navigate(Success) },
                    goBack = { if (isMigrationToMp) logout() else onCancel() },
                )
            }
            composable<Success> {
                ChangeMasterPasswordSuccessScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    origin = origin,
                    onSuccess = onSuccess,
                    onCancel = onCancel,
                    logout = logout,
                )
            }
        }
    }
}

sealed interface ChangeMasterPasswordNavigation {
    @Serializable
    data object Warning : ChangeMasterPasswordNavigation

    @Serializable
    data object ChangeMasterPassword : ChangeMasterPasswordNavigation

    @Serializable
    data object Success : ChangeMasterPasswordNavigation

    fun NavBackStackEntry?.toChangeMasterPasswordNavigation() =
        destinationRouteClassSimpleName()?.let {
            when (it) {
                Warning::class.simpleName -> Warning
                ChangeMasterPassword::class.simpleName -> ChangeMasterPassword
                Success::class.simpleName -> Success
                else -> null
            }
        }
}