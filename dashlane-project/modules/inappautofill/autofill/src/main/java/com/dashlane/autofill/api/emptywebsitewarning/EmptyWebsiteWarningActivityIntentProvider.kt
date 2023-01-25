package com.dashlane.autofill.api.emptywebsitewarning

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.api.emptywebsitewarning.view.EmptyWebsiteWarningActivity
import com.dashlane.autofill.api.fillresponse.EmptyWebsiteWarningIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.MatchType
import javax.inject.Inject

class EmptyWebsiteWarningActivityIntentProvider @Inject constructor() : EmptyWebsiteWarningIntentProvider {
    override fun getEmptyWebsiteWarningSender(
        context: Context,
        itemId: String,
        summary: AutoFillHintSummary,
        matchType: MatchType
    ): IntentSender {
        return EmptyWebsiteWarningActivity.getAuthIntentSenderForEmptyWebsiteWarning(
            context,
            itemId,
            summary,
            matchType
        )
    }
}