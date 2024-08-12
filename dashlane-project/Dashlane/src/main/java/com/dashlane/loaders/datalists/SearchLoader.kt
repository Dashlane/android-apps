package com.dashlane.loaders.datalists

import com.dashlane.loaders.datalists.search.SearchUtils
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.SearchSorter
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.screens.settings.list.RootSettingsList
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchLoader @Inject constructor(
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private var settingsList: RootSettingsList,
    private val searchSorter: SearchSorter,
) {

    private var lastResult: CacheResult? = null

    suspend fun filterByQuery(query: String): List<MatchedSearchResult> {
        val allItems = prepareItemsForQuery(query)
        val result = withContext(defaultDispatcher) {
            searchSorter.matchAndSort(allItems, query)
        }
        lastResult = CacheResult(query, result)
        return result
    }

    private fun prepareItemsForQuery(query: String): List<Any> {
        val cache = lastResult
        return if (cache?.acceptNewFilter(query) == true) {
            cache.result.map { it.item }
        } else {
            val loadFilter = genericFilter { specificDataType(SearchUtils.FILTER_BY_DATA_TYPE.keys) }
            genericDataQuery.queryAll(loadFilter) + settingsList.getSearchableItems()
        }
    }

    fun loadCredentials(): List<SummaryObject.Authentifiant> {
        val loadFilter = genericFilter { specificDataType(SyncObjectType.AUTHENTIFIANT) }
        val allItems = genericDataQuery.queryAll(loadFilter)

        return searchSorter.matchAndSort(allItems, "")
            .mapNotNull { it.item as? SummaryObject.Authentifiant }
    }

    fun loadCredentialById(authentifiantId: String): VaultItem<SyncObject.Authentifiant>? {
        val loadFilter = vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(authentifiantId)
        }
        return vaultDataQuery.queryLegacy(loadFilter)?.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
    }

    private class CacheResult(private val queryString: String, val result: List<MatchedSearchResult>) {
        fun acceptNewFilter(query: String): Boolean {
            return query.startsWith(queryString) 
        }
    }
}