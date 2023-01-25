package com.dashlane.storage.userdata.internal

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.dashlane.database.sql.ChangeSetChangeSql
import com.dashlane.database.sql.ChangeSetSql
import com.dashlane.database.sql.DataChangeHistorySql
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.dao.QueryDao
import com.dashlane.sync.WithExtraDataDbConverter
import com.dashlane.util.getStringListFromCursor
import com.dashlane.util.toList
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Instant
import javax.inject.Inject



class DataChangeHistoryDaoInternal @Inject constructor(
    private val queryDao: QueryDao,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository
) {

    fun getDataChangeHistory(cursor: Cursor): VaultItem<SyncObject.DataChangeHistory> {
        val dbItem = DataChangeHistoryDbConverter.getItemFromCursor(cursor)
        return createDataChangeHistory(dbItem)
    }

    fun getDataChangeHistories(cursor: Cursor): List<VaultItem<SyncObject.DataChangeHistory>> {
        val list = mutableListOf<VaultItem<SyncObject.DataChangeHistory>>()
        if (cursor.moveToFirst()) {
            do {
                list.add(getDataChangeHistory(cursor))
            } while (cursor.moveToNext())
        }
        return list.toList()
    }

    fun save(dataChangeHistory: VaultItem<SyncObject.DataChangeHistory>): Boolean {
        return insertOrUpdateItem(dataChangeHistory, null, null)
    }

    fun deleteChildrenOfDataChangeHistory(uid: String) {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return

        queryChangeSetByChangeHistoryUID(uid).forEach { changeSetUid ->
            
            database.delete(
                ChangeSetChangeSql.TABLE_NAME,
                ChangeSetChangeSql.FIELD_CHANGESET_UID + " = ?",
                arrayOf(changeSetUid)
            )
        }

        
        database.delete(ChangeSetSql.TABLE_NAME, ChangeSetSql.FIELD_DATA_CHANGE_HISTORY_UID + " = ?", arrayOf(uid))
    }

    fun insertOrUpdateItem(
        dataChangeHistory: VaultItem<SyncObject.DataChangeHistory>,
        extraData: String?,
        backupDate: Instant?
    ): Boolean {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return false

        return try {
            val aggregateDataChangeHistory = dataChangeHistory.toAggregateDataChangeHistory()

            val dataChangeHistoryDb = aggregateDataChangeHistory.dataChangeHistory
            val changeSetList = aggregateDataChangeHistory.changeSetList
            val changeSetChangeList = aggregateDataChangeHistory.changeSetChangeList

            
            val contentValuesToSave = DataChangeHistoryDbConverter.toContentValues(dataChangeHistoryDb)!!

            if (extraData != null) {
                
                
                WithExtraDataDbConverter.addSyncInformation(contentValuesToSave, extraData, backupDate)
            }

            val objectUid = dataChangeHistoryDb.objectUID
            val objectTypeId = dataChangeHistoryDb.objectTypeId.toString()
            val existingDataChangeHistory = queryDataChangeHistoryByObjectUID(objectTypeId, objectUid)
            if (existingDataChangeHistory != null) {
                database.update(
                    DataChangeHistorySql.TABLE_NAME, contentValuesToSave,
                    DataChangeHistorySql.FIELD_OBJECT_UID + " = ? AND " +
                            DataChangeHistorySql.FIELD_OBJECT_TYPE + " = ? ",
                    arrayOf(objectUid, objectTypeId)
                )
            } else {
                database.insert(DataChangeHistorySql.TABLE_NAME, contentValuesToSave)
            }

            
            changeSetList.forEach {
                database.insertWithOnConflict(
                    ChangeSetSql.TABLE_NAME, null, ChangeSetDbConverter.getContentValues(it),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            
            changeSetChangeList.forEach {
                database.insertWithOnConflict(
                    ChangeSetChangeSql.TABLE_NAME, null,
                    ChangeSetChangeDbConverter.getContentValues(it),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createDataChangeHistory(dataChangeHistory: DataChangeHistoryForDb): VaultItem<SyncObject.DataChangeHistory> {
        val changeSetList: MutableList<ChangeSetForDb> = mutableListOf()
        val changeSetChangeList: MutableList<ChangeSetChangeForDb> = mutableListOf()

        
        val changeSetIdList = queryChangeSetByChangeHistoryUID(dataChangeHistory.dataIdentifier.uid)

        changeSetIdList.forEach { changeSetId ->

            val changeSet = queryChangeSet(changeSetId) ?: return@forEach

            
            changeSetList.add(changeSet)

            
            val changeSetChangeListForOneChangeSet = queryChangeSetChangeByChangeSetUID(changeSetId)
                ?.takeIf { it.isNotEmpty() }
                ?: return@forEach

            
            changeSetChangeList.addAll(changeSetChangeListForOneChangeSet)
        }

        
        val aggregateDataChangeHistory =
            AggregateDataChangeHistory(dataChangeHistory, changeSetList, changeSetChangeList)

        
        return aggregateDataChangeHistory.toDataChangeHistory()
    }

    private fun queryDataChangeHistoryByObjectUID(objectTypeId: String, objectUID: String): DataChangeHistoryForDb? {
        val sqlQuery = SqlQuery(
            table = DataChangeHistorySql.TABLE_NAME,
            selection = DataChangeHistorySql.FIELD_OBJECT_UID + " = ? AND " +
                    DataChangeHistorySql.FIELD_OBJECT_TYPE + " = ? ",
            selectionArgs = listOf(objectUID, objectTypeId)
        )

        return sqlQuery.let {
            getCursor(sqlQuery)?.use {
                if (!it.moveToFirst()) return null
                DataChangeHistoryDbConverter.getItemFromCursor(it)
            }
        }
    }

    private fun queryChangeSetByChangeHistoryUID(changeHistoryUID: String): List<String> {
        val sqlQuery = SqlQuery(
            table = ChangeSetSql.TABLE_NAME,
            columns = listOf(ChangeSetSql.FIELD_UID),
            selection = ChangeSetSql.FIELD_DATA_CHANGE_HISTORY_UID + " = ? ",
            selectionArgs = listOf(changeHistoryUID)
        )

        return sqlQuery.let {
            getCursor(sqlQuery)?.use {
                it.getStringListFromCursor()
            }
        } ?: listOf()
    }

    private fun queryChangeSet(uid: String): ChangeSetForDb? {
        val sqlQuery = SqlQuery(
            table = ChangeSetSql.TABLE_NAME,
            selection = ChangeSetSql.FIELD_UID + " = ? ",
            selectionArgs = listOf(uid)
        )

        return sqlQuery.let {
            getCursor(sqlQuery)?.use {
                if (!it.moveToFirst()) return null
                ChangeSetDbConverter.getItemFromCursor(it)
            }
        }
    }

    private fun queryChangeSetChangeByChangeSetUID(changeSetUID: String): List<ChangeSetChangeForDb>? {
        val sqlQuery = SqlQuery(
            table = ChangeSetChangeSql.TABLE_NAME,
            selection = ChangeSetChangeSql.FIELD_CHANGESET_UID + " = ? ",
            selectionArgs = listOf(changeSetUID)
        )
        return sqlQuery.let {
            getCursor(sqlQuery)?.use { c ->
                c.toList { ChangeSetChangeDbConverter.getItemFromCursor(this) }
            }
        }
    }

    private fun getCursor(sqlQuery: SqlQuery): Cursor? {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return null
        return queryDao.getCursorForTable(database, sqlQuery)
    }
}