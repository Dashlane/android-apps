package com.dashlane.autofill.api.ui

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.api.model.CreditCardItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.totp.AutofillApiTotp
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.followupnotification.FollowUpNotificationComponent
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AutofillPerformedCallback {
    fun onAutofillPerformed(itemToFill: ItemToFill, summary: AutoFillHintSummary?)
}

class AutofillPerformedCallbackImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val userPreferencesAccess: AutofillAnalyzerDef.IUserPreferencesAccess
) : AutofillPerformedCallback {

    override fun onAutofillPerformed(itemToFill: ItemToFill, summary: AutoFillHintSummary?) {
        when (itemToFill) {
            is AuthentifiantItemToFill -> onAuthentifiantFilled(itemToFill.syncObject)
            is CreditCardItemToFill -> onCreditCardFilled(itemToFill.syncObject, summary)
            else -> {
                
            }
        }
    }

    private fun onAuthentifiantFilled(authentifiant: SyncObject.Authentifiant?) {
        if (authentifiant == null) return
        if (userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.AUTOMATICALLY_COPY_2FA) && userPreferencesAccess.hasAutomatic2faTokenCopy()) {
            AutofillApiTotp(context).clipboardTotpCode(authentifiant)
        } else {
            AutofillApiTotp(context).startTotpNotification(authentifiant)
        }
    }

    private fun onCreditCardFilled(creditCardSyncObject: SyncObject.PaymentCreditCard?, summary: AutoFillHintSummary?) {
        if (creditCardSyncObject != null && summary != null && isCreditCardWithMissingField(summary)) {
            FollowUpNotificationComponent(context).followUpNotificationApiProvider.getFollowUpNotificationApi()
                .startFollowUpNotification(creditCardSyncObject.toSummary(), null)
        }
    }

    private fun isCreditCardWithMissingField(summary: AutoFillHintSummary): Boolean {
        return summary.formType == AutoFillFormType.CREDIT_CARD && !summary.entries.flatMap { it.autoFillHints.getHints() }
            .let {
                it.contains(AutoFillHint.CREDIT_CARD_NUMBER) && it.contains(AutoFillHint.CREDIT_CARD_SECURITY_CODE) && (
                    it.contains(
                    AutoFillHint.CREDIT_CARD_EXPIRATION_DATE
                ) || it.contains(AutoFillHint.CREDIT_CARD_EXPIRATION_DAY) || it.contains(AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH) || it.contains(
                    AutoFillHint.CREDIT_CARD_EXPIRATION_YEAR
                )
                )
            }
    }
}