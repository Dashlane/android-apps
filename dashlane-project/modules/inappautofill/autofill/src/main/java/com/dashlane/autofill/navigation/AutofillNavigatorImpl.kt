package com.dashlane.autofill.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.dashlane.autofill.api.R
import com.dashlane.autofill.changepassword.ChangePasswordDialogFragmentArgs

internal class AutofillNavigatorImpl(
    private val navHostFragment: NavHostFragment
) : AutofillNavigator {
    private val navigationController: NavController = navHostFragment.navController

    constructor(containingActivity: AppCompatActivity) :
            this(containingActivity.supportFragmentManager.findFragmentById(R.id.autofill_nav_host_fragment) as NavHostFragment)

    override fun goToViewAllAccountsDialogFromWaitForDecision() {
        if (!hasNavigated()) {
            markAsNavigated()
            navigate(WaitForNavigationDecisionFragmentDirections.toNavAllAccountsDialog())
        }
    }

    override fun goToCreateAccountDialogFromWaitForDecision() {
        if (!hasNavigated()) {
            markAsNavigated()
            navigate(WaitForNavigationDecisionFragmentDirections.toNavCreateAccountDialog())
        }
    }

    override fun goToChangePasswordDialogFromWaitForDecision(website: String, packageName: String) {
        if (!hasNavigated()) {
            markAsNavigated()
            val args = ChangePasswordDialogFragmentArgs(
                argsPackageName = if (website.isEmpty() && packageName.isNotEmpty()) packageName else null,
                argsWebpage = website.ifEmpty { null }
            ).toBundle()
            navigate(
                WaitForNavigationDecisionFragmentDirections.toNavChangePasswordDialog(
                    startDestinationArgs = args
                )
            )
        }
    }

    internal class AutofillDecisionActivityViewModel : ViewModel() {
        var madeDecision: Boolean = false
    }

    private fun hasNavigated(): Boolean = getAutofillDecisionActivityViewModel()?.madeDecision == true

    private fun markAsNavigated() {
        getAutofillDecisionActivityViewModel()?.madeDecision = true
    }

    private fun getAutofillDecisionActivityViewModel(): AutofillDecisionActivityViewModel? =
        navHostFragment.activity?.let {
            ViewModelProvider(it).get(AutofillDecisionActivityViewModel::class.java)
        }

    private fun navigate(action: NavDirections) {
        navigationController.navigate(action)
    }
}