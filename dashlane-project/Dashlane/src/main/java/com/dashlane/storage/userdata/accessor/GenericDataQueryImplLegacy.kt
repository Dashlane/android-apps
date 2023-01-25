package com.dashlane.storage.userdata.accessor

import android.database.Cursor
import com.dashlane.database.converter.getDataIdentifiers
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.filter.BaseFilter
import com.dashlane.storage.userdata.accessor.filter.FilterToPredicate
import com.dashlane.storage.userdata.accessor.filter.FilterToSql
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.copyWithFilter
import com.dashlane.storage.userdata.dao.QueryDao
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import javax.inject.Inject



class GenericDataQueryImplLegacy @Inject constructor(
    private val queryDao: QueryDao,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val filterToSql: FilterToSql, 
    private val filterToPredicate: FilterToPredicate, 
    private val lockHelper: Lazy<LockHelper>,
    private val summaryTransformProvider: SummaryTransform.Provider 
) : GenericDataQuery {

    override fun createFilter() = GenericFilter()

    override fun queryFirst(filter: BaseFilter): SummaryObject? {
        return queryAll(filter, limitToOne = true).firstOrNull()
    }

    override fun queryAll(filter: BaseFilter): List<SummaryObject> {
        return queryAll(filter, limitToOne = false)
    }

    private fun queryAll(filter: BaseFilter, limitToOne: Boolean): List<SummaryObject> {
        if (lockHelper.get().forbidDataAccess(filter)) return listOf()
        val transformer = summaryTransformProvider.get()
        return filter.dataTypes.flatMap { dataType ->
            if (dataType in SKIP_DATA_TYPES) {
                listOf()
            } else {
                getSqlQuery(dataType, filter, limitToOne)
                    ?.let { sqlFilter ->
                        getCursor(sqlFilter)?.use {
                            it.getDataIdentifiers(dataType)
                        }
                    }
                    ?.filter(filterToPredicate.toPredicate(filter))
                    ?.map { it.toSummary<SummaryObject>() }
                    ?.map { transformer(it) }
                    ?: listOf()
            }
        }
    }

    override fun count(filter: BaseFilter): Int {
        return filter.dataTypes.sumOf { dataType ->
            getSqlQuery(dataType, filter, false)?.let { sqlFilter ->
                getCursor(sqlFilter)?.use { it.count } 
            } ?: 0
        }
    }

    private fun getSqlQuery(dataType: SyncObjectType, filter: BaseFilter, limitToOne: Boolean): SqlQuery? {
        val tableName = dataType.getTableName() ?: return null
        return SqlQuery(table = tableName).copyWithFilter(filterToSql, filter, limitToOne)
    }

    private fun getCursor(sqlQuery: SqlQuery): Cursor? {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) }
                ?: return null
        return queryDao.getCursorForTable(database, sqlQuery)
    }

    companion object {
        private val SKIP_DATA_TYPES = arrayOf(SyncObjectType.SETTINGS, SyncObjectType.DATA_CHANGE_HISTORY)
    }
}