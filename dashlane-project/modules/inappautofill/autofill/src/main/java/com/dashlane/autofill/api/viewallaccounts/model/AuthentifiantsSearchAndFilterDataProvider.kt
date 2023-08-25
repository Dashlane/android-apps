package com.dashlane.autofill.api.viewallaccounts.model

import androidx.annotation.VisibleForTesting
import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsContract
import com.dashlane.search.MatchedSearchResult
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

class AuthentifiantsSearchAndFilterDataProvider(
    private val autofillSearch: AutofillSearch
) : AutofillApiViewAllAccountsContract.DataProvider {
    private var allAuthentifiants: List<SummaryObject.Authentifiant>? = null

    override fun getAuthentifiant(authentifiantId: String): VaultItem<SyncObject.Authentifiant> =
        tryOrNull { autofillSearch.loadAuthentifiant(authentifiantId) }
            ?: throw LoadAuthentifiantsError()

    override fun getMatchedAuthentifiantsFromQuery(query: String?): List<MatchedSearchResult> =
        tryOrNull { matchAuthentifiantsFromQuery(query, loadAllAuthentifiants()) }
            ?: throw FilterAuthentifiantsError()

    @VisibleForTesting
    fun loadAllAuthentifiants(): List<SummaryObject.Authentifiant> {
        allAuthentifiants?.let { return it }

        allAuthentifiants = tryOrNull { autofillSearch.loadAuthentifiants() }
        allAuthentifiants?.let { return it } ?: throw LoadAuthentifiantsError()
    }

    private fun matchAuthentifiantsFromQuery(query: String?, it: List<SummaryObject.Authentifiant>) =
        autofillSearch.matchAuthentifiantsFromQuery(query, it)

    class FilterAuthentifiantsError : Exception("Could not filter authentifiants")

    class LoadAuthentifiantsError : Exception("Could not get authentifiants")
}