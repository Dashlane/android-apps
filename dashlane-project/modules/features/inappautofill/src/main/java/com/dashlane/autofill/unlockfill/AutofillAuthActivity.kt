package com.dashlane.autofill.unlockfill

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.OtpItemToFill
import com.dashlane.autofill.model.ToUnlockItemToFill
import com.dashlane.autofill.securitywarnings.view.SecurityWarningsViewProxy
import com.dashlane.autofill.ui.AutoFillResponseActivity
import com.dashlane.autofill.ui.AutofillFeature
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.xml.domain.SyncObject

open class AutofillAuthActivity : AutoFillResponseActivity() {
    private val securityWarningsViewProxy: SecurityWarningsViewProxy by lazy(LazyThreadSafetyMode.NONE) {
        SecurityWarningsViewProxy(
            this,
            applicationContext,
            component.authentifiantResult,
            component.securityWarningsLogger,
            component.rememberSecurityWarningsService,
            component.autofillFormSourcesStrings,
            component.toaster,
            matchType
        )
    }

    private var itemToFill: ItemToFill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        itemToFill = intent.getParcelableExtraCompat(EXTRA_ITEM_TO_FILL)
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
        val item = itemToFill
        if (item is ToUnlockItemToFill<*>) {
            val syncObject = componentInternal.itemLoader.loadSyncObject(item.itemId)
            when (item) {
                is AuthentifiantItemToFill -> {
                    if (syncObject !is SyncObject.Authentifiant) {
                        finish()
                        return
                    }
                    item.syncObject = syncObject

                    val unlockedAuthentifiant = UnlockedAuthentifiant(summary!!.formSource, item)
                    
                    
                    if (isGuidedChangePassword) {
                        changePasswordShowDialog(unlockedAuthentifiant)
                    } else {
                        dealWithSecurityWarning(unlockedAuthentifiant)
                    }
                    return
                }

                is CreditCardItemToFill -> {
                    if (syncObject !is SyncObject.PaymentCreditCard) {
                        finish()
                        return
                    }
                    item.syncObject = syncObject
                }

                is OtpItemToFill -> {
                    
                }
            }
        }
        finishWithResult(
            itemToFill = itemToFill,
            autofillFeature = AutofillFeature.SUGGESTION,
            matchType = matchType
        )
    }

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
            forKeyboard: Boolean
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
            matchType: MatchType?
        ): IntentSender {
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