package com.dashlane.storage.userdata

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.converter.DbConverter
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.internal.DataChangeHistoryDaoInternal
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.WithExtraDataDbConverter
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import org.intellij.lang.annotations.Language
import java.time.Instant
import javax.inject.Inject

typealias PendingSyncItem = Pair<DataIdentifierExtraDataWrapper<SyncObject>, SyncState>

class DataSyncDao @Inject constructor(
    private val databaseItemSaver: DatabaseItemSaverImplLegacy,
    private val dataChangeHistoryDaoInternal: DataChangeHistoryDaoInternal,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository
) {

    fun markItemsInSync(dataTypes: List<SyncObjectType>) {
        dataTypes.forEach {
            markItemsInSync(it, SyncState.IN_SYNC_MODIFIED, SyncState.MODIFIED)
            markItemsInSync(it, SyncState.IN_SYNC_DELETED, SyncState.DELETED)
        }
    }

    fun getItemWithExtraData(id: String, dataType: SyncObjectType): DataIdentifierExtraDataWrapper<SyncObject>? {
        return getItemsWithExtraData(listOf(id), dataType).firstOrNull()?.first
    }

    fun getItemsWithExtraData(ids: List<String>, dataType: SyncObjectType): List<PendingSyncItem> {
        val tableName = dataType.getTableName()!!
        val whereStmt = DataIdentifierSql.FIELD_UID + " = ? "
        return ids.map { id ->
            getCursorForTable(tableName, whereStmt, whereArgs = listOf(id))!!.use { cursor ->
                check(cursor.moveToFirst()) { "Item not found" }
                val columnIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_ITEM_STATE)
                readPendingSyncItem(cursor, dataType, columnIndex)!!
            }
        }
    }

    fun getItemsPendingSyncForType(type: SyncObjectType): List<PendingSyncItem> {
        val tableName = checkNotNull(type.getTableName()) { "No table for $type" }
        val whereStmt = (DataIdentifierSql.FIELD_ITEM_STATE + "='" + SyncState.IN_SYNC_MODIFIED.code + "' or " +
                DataIdentifierSql.FIELD_ITEM_STATE + "='" + SyncState.IN_SYNC_DELETED.code + "'")
        return getCursorForTable(tableName, whereStmt)!!.use { cursor ->
            val columnIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_ITEM_STATE)
            cursor.asSequence()
                .map { readPendingSyncItem(it, type, columnIndex) }
                .filterNotNull()
                .toList()
        }
    }

    fun markAllItemsAsSynced() {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return
        val cv = ContentValues()
        cv.put(DataIdentifierSql.FIELD_ITEM_STATE, SyncState.SYNCED.code)
        SyncObjectTypeUtils.ALL
            .mapNotNull { it.getTableName() }
            .forEach { tablename ->
                try {
                    database.delete(
                        tablename, DataIdentifierSql.FIELD_ITEM_STATE +
                                "='" + SyncState.IN_SYNC_DELETED.code + "'", null
                    )
                    database.update(
                        tablename, cv, DataIdentifierSql.FIELD_ITEM_STATE + "='" +
                                SyncState.IN_SYNC_MODIFIED.code + "'", null
                    )
                } catch (e: Exception) {
                    
                }
            }
    }

    fun markItemsInSync(dataType: SyncObjectType, syncedState: SyncState, currentState: SyncState) {
        val tableName = checkNotNull(dataType.getTableName()) { "No table for $dataType" }
        val cv = ContentValues().apply {
            put(DataIdentifierSql.FIELD_ITEM_STATE, syncedState.code)
        }
        val whereStatement = DataIdentifierSql.FIELD_ITEM_STATE + " = ?"
        databaseItemSaver.updateItems(cv, tableName, whereStatement, arrayOf(currentState.code))
    }

    fun markItemSyncState(dataType: SyncObjectType, uid: String, syncedState: SyncState) {
        val tableName = checkNotNull(dataType.getTableName()) { "No table for $dataType" }
        val cv = ContentValues().apply {
            put(DataIdentifierSql.FIELD_ITEM_STATE, syncedState.code)
        }
        val whereStatement = DataIdentifierSql.FIELD_UID + " = ?"
        databaseItemSaver.updateItems(cv, tableName, whereStatement, arrayOf(uid))
    }

    private fun readPendingSyncItem(
        it: Cursor,
        type: SyncObjectType,
        stateColumnIndex: Int
    ): Pair<DataIdentifierExtraDataWrapper<SyncObject>, SyncState>? {

        val item = if (type == SyncObjectType.DATA_CHANGE_HISTORY) {
            if (it.position < 0 && !it.moveToFirst()) {
                return null
            }
            dataChangeHistoryDaoInternal.getDataChangeHistory(it)
        } else {
            DbConverter.fromCursor(it, type)
        } ?: error("Unable to read cursor for $type")

        val extraDataWrapper = WithExtraDataDbConverter.cursorToItem(it, item)
        return extraDataWrapper to (SyncState.fromCode(it.getString(stateColumnIndex)) ?: SyncState.SYNCED)
    }

    fun getItemsForSummary(type: SyncObjectType): List<Pair<String, Instant>> {
        val fields = listOf(
            DataIdentifierSql.FIELD_UID,
            DataIdentifierSql.FIELD_BACKUP_DATE
        )
        val tableName = type.getTableName()!!
        return getCursorForTable(tableName, projection = fields)!!.use { cursor ->
            val idIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_UID)
            val backupDateIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_BACKUP_DATE)
            cursor.asSequence()
                .map {
                    val id = it.getString(idIndex)
                    val lastBackupDate = it.getLong(backupDateIndex) * 1000
                    id to Instant.ofEpochMilli(lastBackupDate)
                }
                .toList()
        }
    }

    fun getDuplicationCandidates(type: SyncObjectType): List<List<PendingSyncItem>> {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return emptyList()
        val tableName = type.getTableName()
        val allowedSyncStates = listOf(
            SyncState.IN_SYNC_MODIFIED,
            SyncState.MODIFIED,
            SyncState.SYNCED
        ).joinToString(separator = ",", prefix = "(", postfix = ")") { "'${it.code}'" }

        @Language("RoomSql")
        val query = """
                SELECT *
                FROM $tableName
                JOIN 
                    (SELECT ${DataIdentifierSql.FIELD_USER_DATA_HASH} AS HASH
                        FROM $tableName  
                        WHERE ${DataIdentifierSql.FIELD_ITEM_STATE} in $allowedSyncStates
                        GROUP BY ${DataIdentifierSql.FIELD_USER_DATA_HASH}
                        HAVING count(*) > 1
                    ) DUPLICATE_HASHES 
                    ON ${DataIdentifierSql.FIELD_USER_DATA_HASH} = DUPLICATE_HASHES.HASH
            """.trimIndent()
        return database.rawQuery(query, emptyArray()).use { cursor ->
            val hashColumnIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_USER_DATA_HASH)
            val stateColumnIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_ITEM_STATE)
            cursor.asSequence()
                .groupBy(
                    keySelector = { it.getInt(hashColumnIndex) },
                    valueTransform = { readPendingSyncItem(cursor, type, stateColumnIndex)!! }
                )
                .values.toList()
        }
    }

    private fun getCursorForTable(
        tableName: String,
        where: String? = null,
        projection: List<String>? = null,
        whereArgs: List<String>? = null
    ): Cursor? {
        val database = sessionManager.session?.let { userDataRepository.getDatabase(it) } ?: return null
        return database.query(SqlQuery(tableName, projection, where, whereArgs))
    }
}

fun Cursor.asSequence(): Sequence<Cursor> =
    generateSequence { this }.takeWhile { it.moveToNext() }.constrainOnce()