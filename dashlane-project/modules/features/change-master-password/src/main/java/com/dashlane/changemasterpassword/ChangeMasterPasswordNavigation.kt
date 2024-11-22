package com.dashlane.changemasterpassword

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.ChangeMasterPassword.toChangeMasterPasswordNavigation
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.Success
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.Warning
import com.dashlane.changemasterpassword.success.ChangeMasterPasswordSuccessScreen
import com.dashlane.changemasterpassword.success.ChangeMasterPasswordSuccessViewModel
import com.dashlane.changemasterpassword.warning.ChangeMasterPasswordWarningScreen
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import kotlinx.serialization.Serializable

@Suppress("LongMethod")
@Composable
fun ChangeMasterPasswordNavigation(
    startDestination: ChangeMasterPasswordNavigation,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    logout: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var topBarState by rememberSaveable { mutableStateOf(TopBarState()) }
    val context = LocalContext.current

    LaunchedEffect(navBackStackEntry) {
        val topBarStateValue = when (navBackStackEntry?.toChangeMasterPasswordNavigation()) {
            Success -> TopBarState(visible = false)
            else -> TopBarState(
                visible = true,
                title = context.getString(R.string.change_master_password_activity_label)
            )
        }
        topBarState = topBarStateValue
    }

    TopBarScaffold(
        topBarState = topBarState,
        back = onCancel
    ) { contentPadding ->
        NavHost(
            startDestination = startDestination,
            navController = navController
        ) {
            composable<Warning> {
                ChangeMasterPasswordWarningScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    goToNext = { navController.navigate(ChangeMasterPassword) },
                    cancel = onCancel
                )
            }
            composable<ChangeMasterPassword> { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry(ChangeMasterPassword) }
                val changeMasterPasswordSuccessViewModel = hiltViewModel<ChangeMasterPasswordSuccessViewModel>(parentEntry)

                ChangeMasterPasswordScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                    hasSteps = false,
                    goToNext = { newMasterPassword ->
                        changeMasterPasswordSuccessViewModel.updateNewMasterPassword(newMasterPassword)
                        navController.navigate(Success)
                    },
                    goBack = onCancel
                )
            }
            composable<Success> { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) { navController.getBackStackEntry(ChangeMasterPassword) }
                val changeMasterPasswordSuccessViewModel = hiltViewModel<ChangeMasterPasswordSuccessViewModel>(parentEntry)

                ChangeMasterPasswordSuccessScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = changeMasterPasswordSuccessViewModel,
                    onSuccess = onSuccess,
                    onCancel = onCancel,
                    logout = logout
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
