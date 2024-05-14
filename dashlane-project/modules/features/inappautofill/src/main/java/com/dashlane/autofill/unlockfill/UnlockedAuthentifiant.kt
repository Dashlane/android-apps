package com.dashlane.autofill.unlockfill

import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary

data class UnlockedAuthentifiant(
    val formSource: AutoFillFormSource,
    val itemToFill: AuthentifiantItemToFill
) {
    val packageName: String = formSource.getPackageName()
    val authentifiantSummary: SummaryObject.Authentifiant = itemToFill.syncObject!!.toSummary()
    val itemId: String = authentifiantSummary.id
    val itemUrl: String = authentifiantSummary.urlForUsageLog

    private fun AutoFillFormSource.getPackageName(): String {
        return when (this) {
            is ApplicationFormSource -> packageName
            is WebDomainFormSource -> packageName
        }
    }
}
