package com.dashlane.autofill.api.changepassword.view

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.DATABASE_ERROR
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.NO_MATCHING_CREDENTIAL
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.USER_LOGGED_OUT
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordResultHandler
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

class AutofillChangePasswordActivity :
    DelegateNavigationBottomSheetFragment.NavigationDelegate,
    CoroutineScope,
    AutoFillResponseActivity(),
    AutofillChangePasswordResultHandler {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    private val toaster by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiComponent(this).toaster
    }

    private lateinit var summaryPackageName: String
    private lateinit var summaryWebDomain: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent_navigation)
        summaryPackageName = summary?.packageName ?: ""
        summaryWebDomain = summary?.webDomain ?: ""
    }

    override fun delegatedNavigate(delegateNavigationBottomSheetFragment: DelegateNavigationBottomSheetFragment) {
        delegateNavigationBottomSheetFragment.getAutofillBottomSheetNavigator()
            .goToChangePasswordFromRootDecisions(summaryWebDomain, summaryPackageName)
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

    override fun onFinishWithResult(
        result: VaultItem<SyncObject.Authentifiant>,
        oldItem: SyncObject.Authentifiant
    ) {
        finishWithResult(
            itemToFill = result.toItemToFill(oldPassword = oldItem.password),
            autofillFeature = AutofillFeature.CHANGE_PASSWORD,
            matchType = MatchType.REGULAR
        )
    }

    override fun onError(error: AutofillChangePasswordErrors) {
        when (error) {
            USER_LOGGED_OUT,
            DATABASE_ERROR,
            NO_MATCHING_CREDENTIAL -> {
                toaster.show(
                    getString(error.resId),
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
        AutofillNavigatorImpl(this)
            .goToChangePasswordDialogFromWaitForDecision(summaryWebDomain, summaryPackageName)
    }

    companion object {
        internal fun buildIntent(context: Context, summary: AutoFillHintSummary, forKeyboard: Boolean) =
            createIntent(context, summary, AutofillChangePasswordActivity::class).apply {
                putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            }

        internal fun getAuthIntentSenderForChangePassword(
            context: Context,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean
        ) = createIntentSender(context, buildIntent(context, summary, forKeyboard))
    }
}
