package com.dashlane.autofill.unlockfill

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.frozenautofill.FrozenAutofillActivity
import com.dashlane.autofill.linkservices.model.FormSourcesDataProvider
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.OtpItemToFill
import com.dashlane.autofill.model.ToUnlockItemToFill
import com.dashlane.autofill.phishing.AntiPhishingCapabilityChecker
import com.dashlane.autofill.phishing.AutofillPhishingWarningFragment
import com.dashlane.autofill.securitywarnings.view.SecurityWarningsViewProxy
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.model.urlForUI
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class AutofillAuthActivity : AutoFillResponseActivity() {
    @Inject
    lateinit var antiPhishingCapabilityChecker: AntiPhishingCapabilityChecker

    @Inject
    lateinit var formSourcesDataProvider: FormSourcesDataProvider

    private val securityWarningsViewProxy: SecurityWarningsViewProxy by lazy(LazyThreadSafetyMode.NONE) {
        SecurityWarningsViewProxy(
            this,
            applicationContext,
            component.authentifiantResult,
            component.securityWarningsLogger,
            component.rememberSecurityWarningsService,
            component.autofillFormSourcesStrings,
            component.toaster,
            matchType,
        )
    }

    private var itemToFill: ItemToFill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        itemToFill = intent.getParcelableExtraCompat(EXTRA_ITEM_TO_FILL)

        supportFragmentManager.setFragmentResultListener(
            AutofillPhishingWarningFragment.PHISHING_WARNING_RESULT,
            this,
        ) { _, bundle ->
            val shouldAutofill =
                bundle.getBoolean(AutofillPhishingWarningFragment.PHISHING_WARNING_SHOULD_AUTOFILL)
            if (shouldAutofill) {
                
                onUserLoggedIn()
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (summary == null) {
            finish()
            return
        }

        
        
        when (itemToFill) {
            is EmailItemToFill -> onUserLoggedIn()
            is ToUnlockItemToFill<*>, null -> performLoginAndUnlock { onUserLoggedIn() }
        }
    }

    private fun onUserLoggedIn() {
        lifecycleScope.launch {
            val item = itemToFill
            if (item is ToUnlockItemToFill<*>) {
                val vaultItem = itemLoader.loadSyncObject(item.itemId)
                val syncObject = vaultItem?.syncObject
                when (item) {
                    is AuthentifiantItemToFill -> {
                        if (syncObject !is SyncObject.Authentifiant) {
                            finish()
                            return@launch
                        }
                        handleAuthentifiantAutofill(item, vaultItem)
                        return@launch
                    }

                    is CreditCardItemToFill -> {
                        if (syncObject !is SyncObject.PaymentCreditCard) {
                            finish()
                            return@launch
                        }
                        item.vaultItem =
                            vaultItem.asVaultItemOfClassOrNull(SyncObject.PaymentCreditCard::class.java)
                    }

                    is OtpItemToFill -> {
                        
                    }
                }
            }
            finishWithResult(
                itemToFill = itemToFill,
                matchType = matchType,
            )
        }
    }

    private suspend fun AutofillAuthActivity.handleAuthentifiantAutofill(
        item: AuthentifiantItemToFill,
        vaultItem: VaultItem<SyncObject>,
    ) {
        item.vaultItem =
            vaultItem.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)

        val formSource = summary!!.formSource
        val unlockedAuthentifiant = UnlockedAuthentifiant(formSource, item)
        
        
        if (isGuidedChangePassword) {
            changePasswordShowDialog(unlockedAuthentifiant)
        } else if (shouldShowPhishingWarning(
                formSource = formSource,
                item = item,
                unlockedAuthentifiant = unlockedAuthentifiant,
            )
        ) {
            showPhishingWarning(formSource, item)
        } else {
            dealWithSecurityWarning(unlockedAuthentifiant)
        }
    }

    private fun showPhishingWarning(
        formSource: AutoFillFormSource,
        item: ToUnlockItemToFill<*>,
    ) {
        AutofillPhishingWarningFragment.create(
            formSource = formSource,
            itemId = item.itemId,
        ).show(supportFragmentManager, SECURITY_WARNING_DIALOG_TAG)
    }

    private suspend fun shouldShowPhishingWarning(
        formSource: AutoFillFormSource,
        item: AuthentifiantItemToFill,
        unlockedAuthentifiant: UnlockedAuthentifiant,
    ) = antiPhishingCapabilityChecker.showAntiPhishingWarning(
        formSource = formSource,
        itemWebsite = item.vaultItem?.urlForUI(),
    ) && !formSourcesDataProvider.isLinked(
        unlockedAuthentifiant.formSource,
        unlockedAuthentifiant.authentifiantSummary.id,
    )

    open fun changePasswordShowDialog(unlockedAuthentifiant: UnlockedAuthentifiant) {
        
    }

    fun dealWithSecurityWarning(unlockedAuthentifiant: UnlockedAuthentifiant) {
        securityWarningsViewProxy.dealWithSecurityBeforeFill(unlockedAuthentifiant)
    }

    companion object {
        private const val EXTRA_IS_TO_JUST_LOGIN = "extra_is_to_just_login"
        private const val EXTRA_ITEM_TO_FILL = "extra_item_to_fill"

        internal fun getAuthIntentSenderForLoggedOutDataset(
            context: Context,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean,
        ): IntentSender {
            val intent = createIntent(context, summary, AutofillAuthActivity::class)
            intent.putExtra(EXTRA_IS_TO_JUST_LOGIN, true)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)

            return createIntentSender(context, intent)
        }

        internal fun getAuthIntentSenderForDataset(
            context: Context,
            itemToFill: ItemToFill,
            summary: AutoFillHintSummary,
            forKeyboard: Boolean,
            guidedChangePasswordFlow: Boolean,
            matchType: MatchType?,
            isAccountFrozen: Boolean,
        ): IntentSender {
            if (isAccountFrozen) {
                return FrozenAutofillActivity.getPendingIntent(context, summary)
            }

            val intent = if (guidedChangePasswordFlow) {
                createIntent(context, summary, AutofillAuthChangePasswordActivity::class)
            } else {
                createIntent(context, summary, AutofillAuthActivity::class)
            }
            intent.putExtra(EXTRA_IS_TO_JUST_LOGIN, false)
            intent.putExtra(EXTRA_ITEM_TO_FILL, itemToFill)
            intent.putExtra(EXTRA_FOR_KEYBOARD_AUTOFILL, forKeyboard)
            intent.putExtra(EXTRA_IS_GUIDED_CHANGE_PASSWORD, guidedChangePasswordFlow)
            intent.putExtra(EXTRA_MATCH_TYPE, matchType?.code)

            return createIntentSender(context, intent)
        }
    }
}