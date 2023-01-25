package com.dashlane.storage.userdata.accessor

import com.dashlane.database.Database
import com.dashlane.database.SearchRepository
import com.dashlane.database.model.SearchItem
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.uid.SpecificUidFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.hasValidSpace
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class FrequentSearchRacletteImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val dataStorageProvider: Lazy<DataStorageProvider>,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val lockHelper: LockHelper
) : FrequentSearch {
    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }

    private val genericDataQuery: GenericDataQuery
        get() = dataStorageProvider.get().genericDataQuery

    private val searchRepository: SearchRepository?
        get() = database?.searchRepository

    override suspend fun markedAsSearched(itemId: String, syncObjectType: SyncObjectType) {
        if (lockHelper.forbidDataAccess()) return
        val searchRepository = searchRepository ?: return
        withContext(Dispatchers.IO) {
            val searchItem = searchRepository.load().items.find { it.dataId == itemId }
                ?: SearchItem(
                    dataId = itemId,
                    dataType = syncObjectType,
                    lastUsed = Instant.now(),
                    hitCount = 0
                )
            val updated =
                searchItem.copy(lastUsed = Instant.now(), hitCount = searchItem.hitCount.inc())
            searchRepository.transaction { update(updated) }
        }
    }

    override fun getFrequentlySearchedItems(max: Int): List<SummaryObject> {
        if (lockHelper.forbidDataAccess()) return emptyList()
        val searchRepository = searchRepository ?: return emptyList()
        val items =
            searchRepository.load().items.sortedWith(compareBy({ it.hitCount }, { it.lastUsed }))
                .reversed().take(max)
        return getSummaryObjects(items)
    }

    override fun getLastSearchedItems(max: Int): List<SummaryObject> {
        if (lockHelper.forbidDataAccess()) return emptyList()
        val searchRepository = searchRepository ?: return emptyList()
        val items =
            searchRepository.load().items.sortedByDescending { it.lastUsed }.take(max)
        return getSummaryObjects(items)
    }

    private fun getSummaryObjects(items: List<SearchItem>): List<SummaryObject> {
        return items.asSequence().mapNotNull { item ->
            genericDataQuery.queryFirst(
                GenericFilter(
                    uidFilter = SpecificUidFilter(item.dataId),
                    dataTypeFilter = SpecificDataTypeFilter(item.dataType),
                    spaceFilter = NoSpaceFilter
                )
            )
        }.filter {
            !it.isSpaceItem() || teamspaceAccessorProvider.get()?.hasValidSpace(it) == true
        }.toList()
    }
}