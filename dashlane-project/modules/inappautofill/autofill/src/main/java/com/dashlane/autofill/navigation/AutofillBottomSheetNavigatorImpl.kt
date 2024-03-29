package com.dashlane.autofill.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.viewallaccounts.view.ViewAllItemsDialogFragmentDirections

internal class AutofillBottomSheetNavigatorImpl(
    private val navigationController: NavController?
) : AutofillBottomSheetNavigator {

    override fun popStack(): Boolean = navigationController?.popBackStack() ?: false

    override fun hasVisiblePrevious(): Boolean {
        return navigationController?.previousBackStackEntry != null
    }

    override fun goToCreateAccountFromRootDecisions(website: String?, packageName: String?) {
        val action = AutofillBottomSheetRootFragmentDirections.toNavCreateAccount(
            argsWebpage = website,
            argsPackageName = packageName
        )
        navigate(action)
    }

    override fun goToChangePasswordFromRootDecisions(website: String, packageName: String) {
        val action = AutofillBottomSheetRootFragmentDirections.toNavChangePassword(
            argsPackageName = if (website.isEmpty() && packageName.isNotEmpty()) packageName else null,
            argsWebpage = website.ifEmpty { null }
        )
        navigate(action)
    }

    override fun goToCreateAccountFromAllAccounts(website: String?, packageName: String?) {
        val action = ViewAllItemsDialogFragmentDirections
            .createAccountFromAllAccounts(argsWebpage = website, argsPackageName = packageName)
        navigate(action)
    }

    override fun goToLinkServiceFromAllAccounts(formSource: AutoFillFormSource, itemId: String) {
        val action = ViewAllItemsDialogFragmentDirections
            .linkServiceFromAllAccounts(formSource, itemId)
        navigate(action)
    }

    private fun navigate(destination: NavDirections) = navigationController?.run {
        currentDestination?.getAction(destination.actionId)?.let { navigate(destination) }
    }
}