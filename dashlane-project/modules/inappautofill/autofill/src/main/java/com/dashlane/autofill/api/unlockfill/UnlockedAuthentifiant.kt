package com.dashlane.autofill.api.unlockfill

import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject



data class UnlockedAuthentifiant(
    val formSource: AutoFillFormSource,
    val authentifiant: VaultItem<SyncObject.Authentifiant>
) {
    internal constructor(
        formSource: AutoFillFormSource,
        itemToFill: AuthentifiantItemToFill
    ) : this(formSource, itemToFill.primaryItem)

    val packageName: String = formSource.getPackageName()
    val authentifiantSummary: SummaryObject.Authentifiant = authentifiant.toSummary()
    val itemId: String = authentifiantSummary.id
    val itemUrl: String = authentifiantSummary.urlForUsageLog
    internal val itemToFill = AuthentifiantItemToFill(
        primaryItem = authentifiant,
        lastUsedDate = authentifiantSummary.modificationDatetime ?: authentifiantSummary.creationDatetime
    )

    private fun AutoFillFormSource.getPackageName(): String {
        return when (this) {
            is ApplicationFormSource -> packageName
            is WebDomainFormSource -> packageName
        }
    }
}
