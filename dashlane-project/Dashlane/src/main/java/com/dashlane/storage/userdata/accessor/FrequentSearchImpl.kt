package com.dashlane.storage.userdata.accessor

import com.dashlane.database.Database
import com.dashlane.database.SearchRepository
import com.dashlane.database.model.SearchItem
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.uid.SpecificUidFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrequentSearchImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val genericDataQuery: Lazy<GenericDataQuery>,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val lockHelper: LockHelper,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FrequentSearch {
    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }

    private val searchRepository: SearchRepository?
        get() = database?.searchRepository

    override suspend fun markedAsSearched(itemId: String, syncObjectType: SyncObjectType) {
        if (lockHelper.forbidDataAccess()) return
        val searchRepository = searchRepository ?: return
        withContext(ioDispatcher) {
            val searchItem = searchRepository.load().items.find { it.dataId == itemId }
                ?: SearchItem(
                    dataId = itemId,
                    dataType = syncObjectType,
                    lastUsed = Instant.now(),
                    hitCount = 0,
                )
            val updated =
                searchItem.copy(lastUsed = Instant.now(), hitCount = searchItem.hitCount.inc())
            searchRepository.transaction { update(updated) }
        }
    }

    override suspend fun getLastSearchedItems(max: Int): List<SummaryObject> {
        if (lockHelper.forbidDataAccess()) return emptyList()
        val searchRepository = searchRepository ?: return emptyList()
        val items =
            searchRepository.load().items.sortedByDescending { it.lastUsed }.take(max)
        return getSummaryObjects(items)
    }

    private fun getSummaryObjects(items: List<SearchItem>): List<SummaryObject> {
        val availableSpaceIds =
            (teamSpaceAccessorProvider.get()?.availableSpaces ?: listOf(TeamSpace.Personal))
                .map { it.teamId }
        val loadedItems = items.asSequence().mapNotNull { item ->
            genericDataQuery.get().queryFirst(
                GenericFilter(
                    uidFilter = SpecificUidFilter(item.dataId),
                    dataTypeFilter = SpecificDataTypeFilter(item.dataType),
                    spaceFilter = NoSpaceFilter,
                ),
            )
        }
        return loadedItems.filter {
            
            (it.spaceId ?: TeamSpace.Personal.teamId) in availableSpaceIds
        }.toList()
    }
}