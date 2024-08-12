package com.dashlane.autofill.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.MatchType

interface EmptyWebsiteWarningIntentProvider {
    fun getEmptyWebsiteWarningSender(
        context: Context,
        itemId: String,
        summary: AutoFillHintSummary,
        matchType: MatchType,
        isAccountFrozen: Boolean
    ): IntentSender
}
