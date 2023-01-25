package com.dashlane.storage.userdata.accessor

import com.dashlane.core.domain.search.SearchQuery
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.uid.SpecificUidFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.vault.util.desktopId
import com.dashlane.vault.util.hasValidSpace
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FrequentSearchImpl @Inject constructor(
    private val mainDataAccessor: Lazy<MainDataAccessor>,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val lockHelper: LockHelper
) : FrequentSearch {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.get().getGenericDataQuery()

    override suspend fun markedAsSearched(itemId: String, syncObjectType: SyncObjectType) {
        if (lockHelper.forbidDataAccess()) return
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) }
        if (database == null) {
            return
        }
        return withContext(Dispatchers.IO) {
            database.query(
                SqlQuery(
                    SearchQuery.TABLENAME, null,
                    SearchQuery.COLUMN_DATATYPE + " = ? AND " + SearchQuery.COLUMN_DATA_UID + " = ? ",
                    listOf(syncObjectType.desktopId.toString(), itemId)
                )
            )?.use { c ->
                if (!c.moveToFirst()) return@use

                val sq = SearchQuery.getItemFromCursor(c)
                sq.setLastUsedToNow()
                sq.incrementHitCount()
                database.update(
                    SearchQuery.TABLENAME,
                    sq.contentValues,
                    SearchQuery.COLUMN_DATATYPE + " = ? AND " + SearchQuery.COLUMN_DATA_UID + " = ? ",
                    arrayOf(syncObjectType.desktopId.toString(), itemId)
                )
                return@withContext 
            }

            
            val sq = SearchQuery(syncObjectType, itemId)
            sq.setLastUsedToNow()
            sq.incrementHitCount()
            database.insert(SearchQuery.TABLENAME, sq.contentValues)
        }
    }

    override fun getFrequentlySearchedItems(max: Int): List<SummaryObject> {
        val sqlQuery = SqlQuery.Builder(SearchQuery.TABLENAME)
            .columns(arrayOf(SearchQuery.COLUMN_DATATYPE, SearchQuery.COLUMN_DATA_UID))
            .orderBy(SearchQuery.COLUMN_HIT_COUNT + " DESC, " + SearchQuery.COLUMN_LAST_USED + " DESC")
            .build()

        return getSearchedItem(sqlQuery, max)
    }

    override fun getLastSearchedItems(max: Int): List<SummaryObject> {
        val sqlQuery = SqlQuery.Builder(SearchQuery.TABLENAME)
            .columns(arrayOf(SearchQuery.COLUMN_DATATYPE, SearchQuery.COLUMN_DATA_UID))
            .orderBy(SearchQuery.COLUMN_LAST_USED + " DESC")
            .build()

        return getSearchedItem(sqlQuery)
    }

    private fun getSearchedItem(
        sqlQuery: SqlQuery,
        max: Int = 100
    ): List<SummaryObject> {
        val frequentlySearchedItems = mutableListOf<SummaryObject>()
        if (lockHelper.forbidDataAccess()) return frequentlySearchedItems

        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) }
        if (database == null) {
            return frequentlySearchedItems
        }

        database.query(sqlQuery)?.use { c ->
            if (!c.moveToFirst()) return@use
            do {
                val indexType = c.getColumnIndex(SearchQuery.COLUMN_DATATYPE)
                val indexUid = c.getColumnIndex(SearchQuery.COLUMN_DATA_UID)
                if (indexType == -1 || indexUid == -1) continue
                val type =
                    SyncObjectTypeUtils.valueFromDesktopId(c.getInt(indexType))
                val uid = c.getString(indexUid)
                genericDataQuery.queryFirst(
                    GenericFilter(
                        uidFilter = SpecificUidFilter(uid),
                        dataTypeFilter = SpecificDataTypeFilter(type),
                        spaceFilter = NoSpaceFilter
                    )
                )?.takeIf { !it.isSpaceItem() || teamspaceAccessorProvider.get()?.hasValidSpace(it) == true }
                    ?.let { frequentlySearchedItems.add(it) }

                if (frequentlySearchedItems.size >= max) break 
            } while (c.moveToNext())
        }
        return frequentlySearchedItems
    }
}
