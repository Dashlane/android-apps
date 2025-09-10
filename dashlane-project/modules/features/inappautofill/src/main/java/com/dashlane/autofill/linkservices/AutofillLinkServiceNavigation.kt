package com.dashlane.autofill.linkservices

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dashlane.autofill.linkservices.rememberaccount.ui.RememberAccountScreen
import com.dashlane.autofill.phishing.ui.PhishingWarningScreen
import kotlinx.serialization.Serializable

@Composable
fun AutofillLinkServiceNavigation(
    startDestination: AutofillLinkServicesDestination,
    navigationArgs: AutofillLinkServiceFragmentArgs,
    finishWithResult: (Boolean) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        startDestination = startDestination,
        navController = navController,
    ) {
        composable<AutofillLinkServicesDestination.RememberAccount> {
            RememberAccountScreen(
                viewModel = hiltViewModel(),
                itemId = navigationArgs.itemId,
                formSource = navigationArgs.formSource,
                finishWithResult = finishWithResult,
            )
        }
        composable<AutofillLinkServicesDestination.PhishingWarning> {
            PhishingWarningScreen(
                viewModel = hiltViewModel(),
                itemId = navigationArgs.itemId,
                formSource = navigationArgs.formSource,
                finishWithResult = finishWithResult,
            )
        }
    }
}

sealed interface AutofillLinkServicesDestination {
    @Serializable
    data object RememberAccount : AutofillLinkServicesDestination

    @Serializable
    data object PhishingWarning : AutofillLinkServicesDestination
}