package com.dashlane.autofill.api.viewallaccounts.model

import com.dashlane.search.MatchedSearchResult
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface AutofillSearch {
    fun loadAuthentifiants(): List<SummaryObject.Authentifiant>
    fun loadAuthentifiant(authentifiantId: String): VaultItem<SyncObject.Authentifiant>?
    fun matchAuthentifiantsFromQuery(
        query: String?,
        items: List<SummaryObject.Authentifiant>
    ): List<MatchedSearchResult>
}