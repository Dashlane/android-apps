package com.dashlane.sharing.itemselection

import androidx.annotation.VisibleForTesting
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.isProtected
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class NewShareItemsRepository @Inject constructor(
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val genericDataQuery: GenericDataQuery,
) {
    private var summaries: List<SummaryObject>? = null
    private var selectedItemIds: List<String> = emptyList()

    fun isItemSelected(itemId: String) = selectedItemIds.contains(itemId)

    fun selectItemId(itemId: String) {
        selectedItemIds = selectedItemIds + itemId
    }

    fun unselectItemId(itemId: String) {
        selectedItemIds = selectedItemIds - itemId
    }

    fun getSelectedItemCount(): Int = selectedItemIds.count()

    fun hasProtectedItemsSelected(): Boolean = selectedItemIds.mapNotNull { selectedId ->
        summaries?.find { summary ->
            summary.id == selectedId
        }
    }.any { summary ->
        summary.isProtected
    }

    fun getSelectedItemsIdsByType(): Map<SyncObjectType, List<String>> =
        selectedItemIds.mapNotNull { selectedId ->
            summaries?.find { summary ->
                summary.id == selectedId
            }
        }.groupBy {
            it.syncObjectType
        }.mapValues {
            it.value.map { summary ->
                summary.id
            }
        }

    suspend fun fetchShareableSummaries(query: String? = null, comparator: Comparator<SummaryObject>) =
        (summaries ?: loadShareableSummaries().also { summaries = it })
            .filter {
                it.match(query)
            }.sortedWith(comparator)

    private suspend fun loadShareableSummaries(): List<SummaryObject> {
        val shareableItems = withContext(ioDispatcher) {
            val filter = genericFilter {
                dataTypeFilter = ShareableDataTypeFilter
                onlyShareable()
            }
            genericDataQuery.queryAll(filter).filter {
                !it.hasAttachments()
            }
        }
        return shareableItems
    }
}

@VisibleForTesting
fun SummaryObject.match(query: String?): Boolean {
    query ?: return true
    return filterByDataType[syncObjectType]?.invoke(this, query) ?: false
}

private val filterByDataType = mapOf<SyncObjectType, (SummaryObject, String) -> Boolean>(
    SyncObjectType.AUTHENTIFIANT to { item, query ->
        item is SummaryObject.Authentifiant && (
            query.match(item.title) ||
                query.match(item.urlDomain) ||
                query.match(item.login) ||
                query.match(item.email)
        )
    },
    SyncObjectType.SECURE_NOTE to { item, query ->
        item is SummaryObject.SecureNote && (
            query.match(item.title) ||
                
                (item.secured != true && query.match(item.content))
        )
    },
    SyncObjectType.SECRET to { item, query ->
        item is SummaryObject.Secret && query.match(item.title)
    },
)

private fun String.match(value: String?): Boolean = value?.contains(this, true) == true