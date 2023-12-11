package com.dashlane.autofill.viewallaccounts

import com.dashlane.search.MatchedSearchResult
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface AutofillSearch {
    suspend fun loadAuthentifiants(): List<SummaryObject.Authentifiant>
    suspend fun loadAuthentifiant(authentifiantId: String): VaultItem<SyncObject.Authentifiant>?
    fun matchAuthentifiantsFromQuery(
        query: String?,
        items: List<SummaryObject.Authentifiant>
    ): List<MatchedSearchResult>
}