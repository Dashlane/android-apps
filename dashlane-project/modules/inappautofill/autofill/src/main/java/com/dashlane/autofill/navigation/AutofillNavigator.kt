package com.dashlane.autofill.navigation

import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.dashlane.autofill.api.R

interface AutofillNavigator {
    fun goToViewAllAccountsDialogFromWaitForDecision()
    fun goToCreateAccountDialogFromWaitForDecision()
    fun goToChangePasswordDialogFromWaitForDecision(website: String, packageName: String)
}

fun FragmentActivity.getAutofillNavigator(): AutofillNavigator = AutofillNavigatorImpl(
    this.supportFragmentManager.findFragmentById(R.id.autofill_nav_host_fragment) as NavHostFragment
)