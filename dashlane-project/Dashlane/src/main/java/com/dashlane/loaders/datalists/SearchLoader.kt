package com.dashlane.loaders.datalists

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.loaders.datalists.search.SearchUtils
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.SearchSorter
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.screens.settings.list.RootSettingsList
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext



class SearchLoader(
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private var settingsList: RootSettingsList,
    private val searchSorter: SearchSorter,
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext
) {
    private var allDataDeferred: Deferred<List<SummaryObject>>? = null

    private var lastResult: CacheResult? = null

    constructor(
        searchSorter: SearchSorter,
        scope: CoroutineScope,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ) : this(
        SingletonProvider.getMainDataAccessor().getGenericDataQuery(),
        SingletonProvider.getMainDataAccessor().getVaultDataQuery(),
        SingletonProvider.getComponent().rootSettingsList,
        searchSorter,
        scope,
        coroutineContext,
    )

    fun refreshData() {
        lastResult = null
        allDataDeferred = coroutineScope.async(coroutineContext) {
            val loadFilter = genericFilter { specificDataType(SearchUtils.FILTER_BY_DATA_TYPE.keys) }
            genericDataQuery.queryAll(loadFilter)
        }
    }

    suspend fun filterByQuery(query: String): List<MatchedSearchResult> {
        val allItems = prepareItemsForQuery(query)
        val result = withContext(Dispatchers.Default) {
            searchSorter.matchAndSort(allItems, query)
        }
        lastResult = CacheResult(query, result)
        return result
    }

    

    private suspend fun prepareItemsForQuery(query: String): List<Any> {
        val cache = lastResult
        return if (cache?.acceptNewFilter(query) == true) {
            cache.result.map { it.item }
        } else {
            (allDataDeferred?.await() ?: listOf()) + settingsList.getSearchableItems()
        }
    }

    fun loadAuthentifiants(): List<SummaryObject.Authentifiant> {
        val loadFilter = genericFilter { specificDataType(SyncObjectType.AUTHENTIFIANT) }
        val allItems = genericDataQuery.queryAll(loadFilter)

        return searchSorter.matchAndSort(allItems, "").mapNotNull { it.item as? SummaryObject.Authentifiant }
    }

    

    fun loadAuthentifiantsById(authentifiantsIds: List<String>): List<SummaryObject.Authentifiant>? {
        val loadFilter = genericFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(authentifiantsIds)
            ignoreUserLock()
        }

        val allItems = genericDataQuery.queryAll(loadFilter)

        return searchSorter.matchAndSort(allItems, "")
            .mapNotNull { it.item as? SummaryObject.Authentifiant }
            .takeIf { it.isNotEmpty() }
    }

    

    @Suppress("UNCHECKED_CAST")
    fun loadAuthentifiantById(authentifiantId: String): VaultItem<SyncObject.Authentifiant>? {
        val loadFilter = vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(authentifiantId)
        }
        return vaultDataQuery.query(loadFilter) as VaultItem<SyncObject.Authentifiant>
    }

    private class CacheResult(private val queryString: String, val result: List<MatchedSearchResult>) {
        fun acceptNewFilter(query: String): Boolean {
            return query.startsWith(queryString) 
        }
    }
}