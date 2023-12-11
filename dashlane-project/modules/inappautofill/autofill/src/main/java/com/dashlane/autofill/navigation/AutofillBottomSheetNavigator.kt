package com.dashlane.autofill.navigation

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment

interface AutofillBottomSheetNavigator {
    fun popStack(): Boolean
    fun goToCreateAccountFromRootDecisions(website: String? = null, packageName: String? = null)
    fun goToChangePasswordFromRootDecisions(website: String, packageName: String)
    fun goToCreateAccountFromAllAccounts(website: String? = null, packageName: String? = null)
    fun goToLinkServiceFromAllAccounts(formSource: AutoFillFormSource, itemId: String)
    fun hasVisiblePrevious(): Boolean
}

fun NavigableBottomSheetFragment.getAutofillBottomSheetNavigator(): AutofillBottomSheetNavigator =
    AutofillBottomSheetNavigatorImpl(this.getNavHostFragment()?.navController)