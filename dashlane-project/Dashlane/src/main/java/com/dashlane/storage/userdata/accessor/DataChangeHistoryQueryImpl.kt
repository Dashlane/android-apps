package com.dashlane.storage.userdata.accessor

import android.database.Cursor
import com.dashlane.database.sql.DataChangeHistorySql
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.storage.userdata.dao.QueryDao
import com.dashlane.storage.userdata.internal.DataChangeHistoryDaoInternal
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject



class DataChangeHistoryQueryImpl @Inject constructor(
    private val dataChangeHistoryQueryInternal: DataChangeHistoryDaoInternal,
    private val queryDao: QueryDao,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository
) : DataChangeHistoryQuery {

    override fun query(filter: DataChangeHistoryFilter): VaultItem<SyncObject.DataChangeHistory>? {
        val sqlQuery = SqlQuery(
            table = DataChangeHistorySql.TABLE_NAME,
            selection = DataChangeHistorySql.FIELD_OBJECT_UID + " = ? AND " +
                    DataChangeHistorySql.FIELD_OBJECT_TYPE + " = ? ",
            selectionArgs = listOf(filter.objectUid, filter.objectType.desktopId.toString())
        )
        return getCursor(sqlQuery)?.use {
            if (it.moveToFirst()) {
                dataChangeHistoryQueryInternal.getDataChangeHistory(it)
            } else {
                null
            }
        }
    }

    private fun getCursor(sqlQuery: SqlQuery): Cursor? {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return null
        return queryDao.getCursorForTable(database, sqlQuery)
    }
}