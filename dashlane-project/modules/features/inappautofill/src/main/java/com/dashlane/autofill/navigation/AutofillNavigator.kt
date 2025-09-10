package com.dashlane.autofill.navigation

interface AutofillNavigator {
    fun goToViewAllAccountsDialogFromWaitForDecision()

    fun goToCreateAccountDialogFromWaitForDecision()

    fun goToChangePasswordDialogFromWaitForDecision(website: String, packageName: String)
}