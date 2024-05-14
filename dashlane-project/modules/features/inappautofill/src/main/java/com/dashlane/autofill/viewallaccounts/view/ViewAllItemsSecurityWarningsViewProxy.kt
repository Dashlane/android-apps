package com.dashlane.autofill.viewallaccounts.view

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.R
import com.dashlane.autofill.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.autofill.securitywarnings.AutofillSecurityWarningsLogger
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsService
import com.dashlane.autofill.securitywarnings.view.SecurityWarningsViewProxy
import com.dashlane.autofill.ui.AutofillFeature
import com.dashlane.autofill.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.Toaster
import kotlinx.coroutines.launch

internal class ViewAllItemsSecurityWarningsViewProxy(
    private val autofillViewAllItemsActivity: AutofillViewAllItemsActivity,
    applicationContext: Context,
    authentifiantResult: AutofillAnalyzerDef.IAutofillSecurityApplication,
    securityWarningsLogger: AutofillSecurityWarningsLogger,
    rememberSecurityWarningsService: RememberSecurityWarningsService,
    autofillFormSourcesStrings: AutofillFormSourcesStrings,
    toaster: Toaster,
    val viewAllAccountSelectionNotifier: ViewAllAccountSelectionNotifier,
    val formSourcesDataProvider: FormSourcesDataProvider
) : SecurityWarningsViewProxy(
    autofillViewAllItemsActivity,
    applicationContext,
    authentifiantResult,
    securityWarningsLogger,
    rememberSecurityWarningsService,
    autofillFormSourcesStrings,
    toaster,
    MatchType.EXPLORE_PASSWORDS
) {

    override fun autoAcceptUnknown(): Boolean = true

    override fun autoAcceptMismatch(): Boolean = true

    override fun finishWithResult(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        showWarningRemembered: Boolean,
        warningShown: Boolean
    ) {
        if (showWarningRemembered) {
            toaster.show(R.string.autofill_warning_we_will_remember, Toast.LENGTH_SHORT)
        }
        if (!hasEditRight(unlockedAuthentifiant.itemToFill)) {
            
            finishAndAutofill(unlockedAuthentifiant, false)
        } else if (warningShown) {
            
            
            finishAndAutofill(unlockedAuthentifiant, true)
        } else {
            autofillViewAllItemsActivity.lifecycleScope.launch {
                val isAlreadyLinked = formSourcesDataProvider.isLinked(
                    unlockedAuthentifiant.formSource,
                    unlockedAuthentifiant.authentifiantSummary.id
                )
                
                if (isAlreadyLinked) {
                    finishAndAutofill(unlockedAuthentifiant, false)
                } else {
                    openLinkServiceBottomSheet(unlockedAuthentifiant)
                }
            }
        }
    }

    private fun openLinkServiceBottomSheet(unlockedAuthentifiant: UnlockedAuthentifiant) {
        autofillViewAllItemsActivity.onNavigateToLinkService(
            unlockedAuthentifiant.formSource,
            unlockedAuthentifiant.authentifiantSummary.id
        )

        
        autofillViewAllItemsActivity.getNavigableFragment()?.childFragmentManager?.setFragmentResultListener(
            AutofillViewAllItemsActivity.LINK_SERVICE_REQUEST_KEY,
            autofillViewAllItemsActivity
        ) { _, bundle ->
            if (bundle.getBoolean(AutofillViewAllItemsActivity.LINK_SERVICE_SHOULD_AUTOFILL)) {
                val shouldLink = bundle.getBoolean(AutofillViewAllItemsActivity.LINK_SERVICE_SHOULD_LINK)
                finishAndAutofill(unlockedAuthentifiant, shouldLink)
            } else {
                autofillViewAllItemsActivity.finish()
            }
        }
    }

    private fun finishAndAutofill(unlockedAuthentifiant: UnlockedAuthentifiant, shouldLink: Boolean) {
        if (shouldLink) {
            viewAllAccountSelectionNotifier.onAccountSelected(
                unlockedAuthentifiant.formSource,
                unlockedAuthentifiant.authentifiantSummary
            )
        }
        autofillViewAllItemsActivity.finishWithResult(
            itemToFill = unlockedAuthentifiant.itemToFill,
            autofillFeature = AutofillFeature.VIEW_ALL_ACCOUNTS,
            matchType = MatchType.EXPLORE_PASSWORDS
        )
    }

    private fun hasEditRight(itemToFill: AuthentifiantItemToFill): Boolean {
        return !itemToFill.isSharedWithLimitedRight
    }
}
