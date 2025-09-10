package com.dashlane.accountsummary

import androidx.navigation.NavBackStackEntry
import com.dashlane.accountsummary.AccountSummaryDestination.AccountSummary
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeContactEmail
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmail
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmailPending
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmailVerification
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import kotlinx.serialization.Serializable

sealed interface AccountSummaryDestination {
    @Serializable
    data object AccountSummary : AccountSummaryDestination

    @Serializable
    data object ChangeContactEmail : AccountSummaryDestination

    @Serializable
    data object ChangeLoginEmail : AccountSummaryDestination

    @Serializable
    data object ChangeLoginEmailPending : AccountSummaryDestination

    @Serializable
    data class ChangeLoginEmailVerification(val newEmail: String) : AccountSummaryDestination
}

fun NavBackStackEntry?.toAccountSummaryNavigation() =
    destinationRouteClassSimpleName()?.let {
        when (it) {
            AccountSummary::class.simpleName -> AccountSummary
            ChangeContactEmail::class.simpleName -> ChangeContactEmail
            ChangeLoginEmail::class.simpleName -> ChangeLoginEmail
            ChangeLoginEmailPending::class.simpleName -> ChangeLoginEmailPending
            ChangeLoginEmailVerification::class.simpleName -> ChangeLoginEmailVerification
            else -> null
        }
    }