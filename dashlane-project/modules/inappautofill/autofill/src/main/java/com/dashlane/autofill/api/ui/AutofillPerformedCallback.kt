package com.dashlane.autofill.api.ui

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.totp.AutofillApiTotp
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



interface AutofillPerformedCallback {
    fun onAuthentifiantFilled(authentifiant: SyncObject.Authentifiant)
    fun onCreditCardFilled(authentifiant: SyncObject.PaymentCreditCard)
    fun onEmailFilled(authentifiant: SummaryObject.Email)
}

class AutofillPerformedCallbackImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val userPreferencesAccess: AutofillAnalyzerDef.IUserPreferencesAccess
) : AutofillPerformedCallback {

    override fun onAuthentifiantFilled(authentifiant: SyncObject.Authentifiant) {
        if (userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.AUTOMATICALLY_COPY_2FA) &&
            userPreferencesAccess.hasAutomatic2faTokenCopy()
        ) {
            AutofillApiTotp(context).clipboardTotpCode(authentifiant)
        } else {
            AutofillApiTotp(context).startTotpNotification(authentifiant)
        }
    }

    override fun onCreditCardFilled(authentifiant: SyncObject.PaymentCreditCard) {
        
    }

    override fun onEmailFilled(authentifiant: SummaryObject.Email) {
        
    }
}