package com.dashlane.loaders.datalists

import com.dashlane.search.SearchSorter
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class SearchLoader @Inject constructor(
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val searchSorter: SearchSorter,
) {
    fun loadCredentials(): List<SummaryObject.Authentifiant> {
        val loadFilter = genericFilter { specificDataType(SyncObjectType.AUTHENTIFIANT) }
        val allItems = genericDataQuery.queryAll(loadFilter)

        return searchSorter.matchAndSort(allItems, "")
            .mapNotNull { it.item as? SummaryObject.Authentifiant }
    }

    suspend fun loadCredentialById(authentifiantId: String): VaultItem<SyncObject.Authentifiant>? {
        val loadFilter = vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(authentifiantId)
        }
        return vaultDataQuery.query(loadFilter)
            ?.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
    }
}