package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary



internal class AuthentifiantSyncObjectFiller(autofillValueFactory: AutofillValueFactory) :
    AuthentifiantFiller(autofillValueFactory) {

    @Suppress("UNCHECKED_CAST")
    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val authentifiantSyncObject =
            (item as? AuthentifiantItemToFill)?.primaryItem?.syncObject ?: return false
        val authentifiant: SummaryObject.Authentifiant = authentifiantSyncObject.toSummary()
        val loginFieldFound = setLogin(dataSetBuilder, summary, authentifiant)
        val password = authentifiantSyncObject.password?.takeUnless { requireLock }?.toString() ?: ""
        val oldPassword = item.oldItem?.password?.toString()
        val passwordFieldFound = setPassword(dataSetBuilder, summary, password, oldPassword)
        return loginFieldFound || passwordFieldFound
    }
}