package com.dashlane.storage.userdata.accessor

import android.database.Cursor
import com.dashlane.database.converter.getDataIdentifiers
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.filter.FilterToPredicate
import com.dashlane.storage.userdata.accessor.filter.FilterToSql
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.copyWithFilter
import com.dashlane.storage.userdata.dao.QueryDao
import com.dashlane.storage.userdata.internal.DataChangeHistoryDaoInternal
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getTableName
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import javax.inject.Inject



class VaultDataQueryImplLegacy @Inject constructor(
    private val queryDao: QueryDao,
    private val dataChangeHistoryDaoInternal: DataChangeHistoryDaoInternal,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val filterToSql: FilterToSql, 
    private val filterToPredicate: FilterToPredicate, 
    private val lockHelper: Lazy<LockHelper>
) : VaultDataQuery {

    override fun query(filter: VaultFilter): VaultItem<SyncObject>? {
        return queryAll(filter, limitToOne = true).firstOrNull()
    }

    override fun queryAll(filter: VaultFilter): List<VaultItem<SyncObject>> {
        return queryAll(filter, limitToOne = false)
    }

    private fun queryAll(filter: VaultFilter, limitToOne: Boolean): List<VaultItem<*>> {
        if (lockHelper.get().forbidDataAccess(filter)) return listOf()
        return filter.dataTypes.flatMap { dataType ->
            getSqlQuery(dataType, filter, limitToOne)?.let { sqlFilter ->
                getCursor(sqlFilter)?.use {
                    if (dataType == SyncObjectType.DATA_CHANGE_HISTORY) {
                        dataChangeHistoryDaoInternal.getDataChangeHistories(it)
                    } else {
                        it.getDataIdentifiers(dataType)
                    }
                }
            }?.filter(filterToPredicate.toPredicate(filter)) ?: listOf()
        }
    }

    private fun getSqlQuery(dataType: SyncObjectType, filter: VaultFilter, limitToOne: Boolean): SqlQuery? {
        val tableName = dataType.getTableName() ?: return null
        return SqlQuery(table = tableName).copyWithFilter(filterToSql, filter, limitToOne)
    }

    private fun getCursor(sqlQuery: SqlQuery): Cursor? {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return null
        return queryDao.getCursorForTable(database, sqlQuery)
    }
}