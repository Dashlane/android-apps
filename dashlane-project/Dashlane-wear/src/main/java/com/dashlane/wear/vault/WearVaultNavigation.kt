package com.dashlane.wear.vault

import android.content.Intent
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.navigation.composable
import com.dashlane.wear.R
import com.dashlane.wear.login.common.ui.CheckYourPhoneScreenContent
import com.dashlane.wear.settings.wearSettingsNavigation
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.vault.WearVaultDestination.CheckYourPhoneDestination
import com.dashlane.wear.vault.WearVaultDestination.CredentialProviderSettingsDestination
import com.dashlane.wear.vault.WearVaultDestination.ItemDetailsDestination
import com.dashlane.wear.vault.WearVaultDestination.ItemsListDestination
import com.dashlane.wear.vault.details.WearItemDetailsScreen
import com.dashlane.wear.vault.list.WearVaultListScreen
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

fun NavGraphBuilder.wearVaultNavigation(
    route: String,
    navHostController: NavHostController,
    onLogout: () -> Unit,
    onOpenMobileActivity: (Intent) -> Unit,
) {
    navigation(route = route, startDestination = ItemsListDestination.destination) {
        composable(ItemsListDestination.destination) {
            WearVaultListScreen(
                viewModel = hiltViewModel(),
                onGoToItemDetails = { itemId ->
                    navHostController.navigate(route = "${ItemDetailsDestination.destination}/$itemId")
                },
                onOpenSupportedServicesOnMobile = { mobileIntent ->
                    onOpenMobileActivity(mobileIntent)
                    navHostController.navigate(route = CheckYourPhoneDestination.destination)
                },
                onGoToSettings = {
                    navHostController.navigate(route = CredentialProviderSettingsDestination.destination)
                },
            )
        }
        composable(
            route = "${ItemDetailsDestination.destination}/{${ItemDetailsDestination.itemIdArg}}",
        ) { backStackEntry ->
            WearItemDetailsScreen(
                viewModel = hiltViewModel(),
                itemId = backStackEntry.arguments?.getString(ItemDetailsDestination.itemIdArg)!!,
            )
        }
        composable(
            route = CheckYourPhoneDestination.destination,
        ) {
            val scalingListState = rememberScalingLazyListState(initialCenterItemScrollOffset = 1)

            WearContentWrapper(
                isLogoVisible = true,
                scrollableState = scalingListState,
                positionIndicator = {
                    PositionIndicator(scalingLazyListState = scalingListState)
                },
            ) {
                CheckYourPhoneScreenContent(
                    text = stringResource(R.string.check_your_phone),
                    waitingTime = 3.seconds,
                ) {
                    navHostController.navigate(route = ItemsListDestination.destination)
                }
            }
        }
        wearSettingsNavigation(
            route = CredentialProviderSettingsDestination.destination,
            navHostController = navHostController,
            onLogoutComplete = onLogout,
        )
    }
}



sealed interface WearVaultDestination {
    @Serializable
    data object ItemsListDestination : WearVaultDestination {
        val destination: String
            get() = "itemsList"
    }

    @Serializable
    data object ItemDetailsDestination : WearVaultDestination {
        val destination: String
            get() = "itemDetails"
        val itemIdArg: String
            get() = "itemId"
    }

    @Serializable
    data object CredentialProviderSettingsDestination : WearVaultDestination {
        val destination: String
            get() = "credentialProviderSettings"
    }

    @Serializable
    data object CheckYourPhoneDestination : WearVaultDestination {
        val destination: String
            get() = "checkYourPhone"
    }
}