package com.dashlane.autofill.api.viewallaccounts.view

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountErrors
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountResultHandler
import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.api.navigation.AutofillBottomSheetNavigator
import com.dashlane.autofill.api.navigation.AutofillBottomSheetNavigatorImpl
import com.dashlane.autofill.api.navigation.AutofillNavigatorImpl
import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberAccountComponent
import com.dashlane.autofill.api.request.autofill.logger.getAutofillApiOrigin
import com.dashlane.autofill.api.securitywarnings.view.IncorrectSecurityWarningDialogFragment
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsComponent
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragment
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.runIfNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext



@AndroidEntryPoint
class AutofillViewAllItemsActivity :
    SearchAuthentifiantDialogResponse,
    AutofillCreateAccountResultHandler,
    CoroutineScope,
    IncorrectSecurityWarningDialogFragment.Actions,
    AutoFillResponseActivity() {

    private lateinit var summaryPackageName: String
    private lateinit var summaryWebDomain: String

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private val viewAllAccountsComponent: AutofillApiViewAllAccountsComponent
        get() = AutofillApiViewAllAccountsComponent(this)

    private val apiRememberAccountComponent: AutofillApiRememberAccountComponent
        get() = AutofillApiRememberAccountComponent(this)

    private val securityWarningsViewProxy: ViewAllItemsSecurityWarningsViewProxy by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        ViewAllItemsSecurityWarningsViewProxy(
            this,
            applicationContext,
            component.authentifiantResult,
            component.securityWarningsLogger,
            component.rememberSecurityWarningsService,
            component.autofillFormSourcesStrings,
            component.toaster,
            viewAllAccountsComponent.viewAllAccountSelectionNotifier,
            viewAllAccountsComponent.userFeaturesChecker,
            apiRememberAccountComponent.formSourcesDataProvider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent_navigation)
        summaryPackageName = summary?.packageName ?: ""
        summaryWebDomain = summary?.webDomain ?: ""
    }

    override fun onResultsLoaded() {
        viewAllAccountsLogger.onResultsLoaded()
    }

    override fun onNavigateToCreateAuthentifiant(autofillBottomSheetNavigator: AutofillBottomSheetNavigator) {
        autofillBottomSheetNavigator.goToCreateAccountFromAllAccounts(
            summaryWebDomain,
            summaryPackageName
        )
    }

    override fun onNavigateToLinkService(formSource: AutoFillFormSource, itemId: String) {
        getNavigableFragment()?.findNavController()?.let {
            AutofillBottomSheetNavigatorImpl(it).goToLinkServiceFromAllAccounts(formSource, itemId)
        }
    }

    

    fun getNavigableFragment(): Fragment? {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.autofill_nav_host_fragment) as NavHostFragment
        return navHostFragment.childFragmentManager.fragments
            .firstOrNull { it is NavigableBottomSheetDialogFragment }
            ?.childFragmentManager?.findFragmentById(R.id.navigable_bottom_sheet_dialog_nav_host_fragment)
    }

    override fun onResume() {
        super.onResume()

        if (summary == null) {
            finish()
            return
        }
        if (isFirstRun) {
            viewAllAccountsLogger.onClickToViewAllAccounts(
                getAutofillApiOrigin(forKeyboardAutofill),
                summaryPackageName,
                summaryWebDomain,
                intent.getBooleanExtra(EXTRA_HAD_CREDENTIALS, false)
            )
        }

        performLoginAndUnlock {
            openBottomSheetAuthentifiantsListDialog()
        }
    }

    private fun openBottomSheetAuthentifiantsListDialog() {
        AutofillNavigatorImpl(this).goToViewAllAccountsDialogFromWaitForDecision()
    }

    override fun onAuthentifiantDialogResponse(
        authentifiant: VaultItem<SyncObject.Authentifiant>?,
        itemListContext: ItemListContext?,
        searchQuery: String
    ) {
        val resultCount = itemListContext?.sectionCount ?: 0
        viewAllAccountsLogger.onViewAllAccountOver(
            totalCount = resultCount,
            hasInteracted = authentifiant != null,
            searchQuery.length
        )
        if (authentifiant == null) {
            finish()
        } else {
            val formSource = summary?.formSource.runIfNull {
                finish()
            } ?: return
            val itemToFill = AuthentifiantItemToFill(
                primaryItem = authentifiant,
                lastUsedDate = authentifiant.syncObject.modificationDatetime
                    ?: authentifiant.syncObject.creationDatetime
            )
            viewAllAccountsLogger.onSelectFromViewAllAccount(
                origin = getAutofillApiOrigin(forKeyboardAutofill),
                packageName = summaryPackageName,
                webappDomain = summaryWebDomain,
                itemUrl = authentifiant.syncObject.urlForUsageLog,
                itemId = authentifiant.syncObject.id,
                itemListContext = itemListContext
            )
            securityWarningsViewProxy.dealWithSecurityBeforeFill(
                UnlockedAuthentifiant(
                    formSource,
                    itemToFill
                )
            )
        }
    }

    override fun incorrectDialogPositiveAction(doNotShowAgainChecked: Boolean, domain: Domain) =
        securityWarningsViewProxy.incorrectDialogPositiveAction(doNotShowAgainChecked, domain)

    override fun incorrectDialogNegativeAction(domain: Domain) =
        securityWarningsViewProxy.incorrectDialogNegativeAction(domain)

    override fun incorrectDialogCancelAction(domain: Domain) =
        securityWarningsViewProxy.incorrectDialogCancelAction(domain)

    override fun onFinishWithResult(result: VaultItem<SyncObject.Authentifiant>) {
        finishWithResult(
            itemToFill = AuthentifiantItemToFill(primaryItem = result, lastUsedDate = result.locallyViewedDate),
            autofillFeature = AutofillFeature.CREATE_ACCOUNT,
            matchType = MatchType.CREATED_PASSWORD 
        )
    }

    override fun onError(error: AutofillCreateAccountErrors) {
        when (error) {
            AutofillCreateAccountErrors.USER_LOGGED_OUT,
            AutofillCreateAccountErrors.DATABASE_ERROR -> {
                component.toaster.show(
                    error.message,
                    Toast.LENGTH_SHORT
                )
            }
            else -> Unit
        }
    }

    override fun onCancel() {
        finish()
    }

    companion object {
        const val LINK_SERVICE_REQUEST_KEY = "LINK_SERVICE_REQUEST_KEY"
        const val LINK_SERVICE_SHOULD_AUTOFILL = "LINK_SERVICE_SHOULD_AUTOFILL"
        const val LINK_SERVICE_SHOULD_LINK = "LINK_SERVICE_SHOULD_LINK"
        private const val EXTRA_HAD_CREDENTIALS = "extra_had_credentials"

        internal fun getAuthIntentSenderForViewAllItems(
            context: Context,
            summary: AutoFillHintSummary,
            hadCredentials: Boolean,
            forKeyboard: Boolean
        ): IntentSender {
            val intent =
                createIntent(
                    context,
                    summary,
                    AutofillViewAllItemsActivity::class
                )
            intent.putExtra(EXTRA_HAD_CREDENTIALS, hadCredentials)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            return createIntentSender(
                context,
                intent
            )
        }
    }
}
