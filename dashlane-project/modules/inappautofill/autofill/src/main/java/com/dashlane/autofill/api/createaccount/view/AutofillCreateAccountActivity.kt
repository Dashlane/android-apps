package com.dashlane.autofill.api.createaccount.view

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountErrors
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountErrors.DATABASE_ERROR
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountErrors.USER_LOGGED_OUT
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountResultHandler
import com.dashlane.autofill.api.createaccount.getDomainInfoForCreateAccount
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.model.toItemToFill
import com.dashlane.autofill.api.navigation.AutofillNavigatorImpl
import com.dashlane.autofill.api.navigation.getAutofillBottomSheetNavigator
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.bottomnavigation.delegatenavigation.DelegateNavigationBottomSheetFragment
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class AutofillCreateAccountActivity :
    DelegateNavigationBottomSheetFragment.NavigationDelegate,
    CoroutineScope,
    AutoFillResponseActivity(),
    AutofillCreateAccountResultHandler {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private val toaster by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiComponent(this).toaster
    }

    private lateinit var summaryPackageName: String
    private lateinit var summaryWebDomain: String

    private var hasExtraCredential: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent_navigation)
        summaryPackageName = summary?.packageName ?: ""
        summaryWebDomain = summary?.webDomain ?: ""
        hasExtraCredential = intent.getBooleanExtra(EXTRA_HAD_CREDENTIALS, false)
    }

    override fun delegatedNavigate(delegateNavigationBottomSheetFragment: DelegateNavigationBottomSheetFragment) {
        val (website, packageName) = summary.getDomainInfoForCreateAccount()
        delegateNavigationBottomSheetFragment.getAutofillBottomSheetNavigator()
            .goToCreateAccountFromRootDecisions(website, packageName)
    }

    override fun onResume() {
        super.onResume()

        if (summary == null) {
            finish()
            return
        }
        performLoginAndUnlock {
            openBottomSheetDialog()
        }
    }

    override fun onFinishWithResult(result: VaultItem<SyncObject.Authentifiant>) {
        finishWithResult(
            itemToFill = result.toItemToFill(),
            autofillFeature = AutofillFeature.CREATE_ACCOUNT,
            matchType = MatchType.CREATED_PASSWORD
        )
    }

    override fun onError(error: AutofillCreateAccountErrors) {
        when (error) {
            USER_LOGGED_OUT,
            DATABASE_ERROR -> {
                toaster.show(
                    error.message,
                    Toast.LENGTH_SHORT
                )
            }
            else -> {
                
            }
        }
        finish()
    }

    override fun onCancel() {
        finish()
    }

    private fun openBottomSheetDialog() {
        AutofillNavigatorImpl(this).goToCreateAccountDialogFromWaitForDecision()
    }

    companion object {
        private const val EXTRA_HAD_CREDENTIALS = "extra_had_credentials"

        internal fun getAuthIntentSenderForCreateAccount(
            context: Context,
            summary: AutoFillHintSummary,
            hadCredentials: Boolean,
            forKeyboard: Boolean
        ): IntentSender {
            val intent = createIntent(context, summary, AutofillCreateAccountActivity::class)
            intent.putExtra(EXTRA_HAD_CREDENTIALS, hadCredentials)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            return createIntentSender(context, intent)
        }
    }
}
