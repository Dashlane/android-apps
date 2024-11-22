package com.dashlane.autofill.ui

import android.content.Context
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.totp.AutofillTotpCopyService
import com.dashlane.followupnotification.FollowUpNotificationEntryPoint
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AutofillPerformedCallback {
    fun onAutofillPerformed(itemToFill: ItemToFill, summary: AutoFillHintSummary?)
}

class AutofillPerformedCallbackImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val totpRepository: AutofillTotpCopyService
) : AutofillPerformedCallback {

    override fun onAutofillPerformed(itemToFill: ItemToFill, summary: AutoFillHintSummary?) {
        when (itemToFill) {
            is AuthentifiantItemToFill -> {
                
                if (summary?.formType == AutoFillFormType.CREDENTIAL) {
                    onAuthentifiantFilled(itemToFill.vaultItem)
                }
            }
            is CreditCardItemToFill -> onCreditCardFilled(itemToFill.vaultItem, summary)
            else -> {
                
            }
        }
    }

    private fun onAuthentifiantFilled(authentifiant: VaultItem<SyncObject.Authentifiant>?) {
        if (authentifiant == null || !preferencesManager[sessionManager.session?.username].hasAutomatic2faTokenCopy) return

        totpRepository.copyTotpToClipboard(authentifiant)
    }

    private fun onCreditCardFilled(
        creditCardVaultItem: VaultItem<SyncObject.PaymentCreditCard>?,
        summary: AutoFillHintSummary?
    ) {
        if (creditCardVaultItem != null && summary != null && isCreditCardWithMissingField(summary)) {
            FollowUpNotificationEntryPoint(context).followUpNotificationApiProvider.getFollowUpNotificationApi()
                .startFollowUpNotification(creditCardVaultItem.toSummary(), null)
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