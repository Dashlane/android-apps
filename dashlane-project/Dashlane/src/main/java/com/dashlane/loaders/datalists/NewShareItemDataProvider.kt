package com.dashlane.loaders.datalists

import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.comparatorAlphabeticAuthentifiant
import com.dashlane.vault.util.comparatorAlphabeticSecureNote
import com.dashlane.vault.util.get
import com.dashlane.vault.util.hasAttachments
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject



class NewShareItemDataProvider @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    private lateinit var deferred: Deferred<List<SummaryObject>>

    suspend fun init() {
        deferred = coroutineScope {
            async {
                withContext(ioDispatcher) {
                    val filter = genericFilter {
                        specificDataType(SyncObjectType.AUTHENTIFIANT, SyncObjectType.SECURE_NOTE)
                        onlyShareable()
                    }
                    genericDataQuery.queryAll(filter).filter {
                        !it.hasAttachments()
                    }
                }
            }
        }
    }

    suspend fun loadAccounts(queryFilter: String? = null): List<SummaryObject.Authentifiant> {
        return deferred.await().filterIsInstance<SummaryObject.Authentifiant>().filter {
            match(it, queryFilter)
        }.sortedWith(comparatorAlphabeticAuthentifiant())
    }

    suspend fun loadSecureNotes(queryFilter: String? = null): List<SummaryObject.SecureNote> {
        return deferred.await().filterIsInstance<SummaryObject.SecureNote>().filter {
            match(it, queryFilter)
        }.sortedWith(comparatorAlphabeticSecureNote())
    }

    

    private fun match(vaultItem: SummaryObject, query: String?): Boolean {
        query ?: return true
        return !vaultItem.hasAttachments() && FILTER_BY_DATA_TYPE[SyncObjectType[vaultItem]]?.invoke(
            vaultItem,
            query
        ) ?: false
    }

    companion object {
        private val FILTER_BY_DATA_TYPE =
            mapOf<SyncObjectType, (SummaryObject, String) -> (Boolean)>(
                SyncObjectType.AUTHENTIFIANT to { item, query ->
                    item is SummaryObject.Authentifiant &&
                            (query.match(item.title) ||
                                    query.match(item.url) ||
                                    query.match(item.userSelectedUrl) ||
                                    query.match(item.login) ||
                                    query.match(item.email))
                },
                SyncObjectType.SECURE_NOTE to { item, query ->
                    item is SummaryObject.SecureNote &&
                            (query.match(item.title) ||
                                    
                                    (item.secured != true && query.match(item.content)))
                }
            )
    }
}

private fun String.match(value: String?): Boolean = value?.contains(this, true) == true
