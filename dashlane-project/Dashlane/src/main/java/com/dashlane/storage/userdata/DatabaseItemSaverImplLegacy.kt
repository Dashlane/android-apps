package com.dashlane.storage.userdata

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.dashlane.core.sharing.SharedItemMerger
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplLegacy
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.storage.userdata.internal.DataChangeHistoryDaoInternal
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.WithExtraDataDbConverter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.util.get
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

class DatabaseItemSaverImplLegacy @Inject constructor(
    private val dataChangeHistoryDao: DataChangeHistoryDaoInternal,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val vaultDataQuery: VaultDataQueryImplLegacy
) : DatabaseItemSaver {

    fun updateItems(cv: ContentValues, tableName: String, whereStatement: String, selectionArgs: Array<String>) {
        getDatabase()?.updateItems(cv, tableName, whereStatement, selectionArgs)
    }

    override suspend fun delete(item: VaultItem<*>): Boolean {
        return delete(item.uid, SyncObjectType[item]!!) 
    }

    fun delete(uid: String, dataType: SyncObjectType): Boolean {
        return try {
            val tableName = dataType.getTableName()
            val database = getDatabase()
            database!!.delete(
                tableName,
                DataIdentifierSql.FIELD_UID + "='" + uid + "'", null
            )
            if (dataType == SyncObjectType.DATA_CHANGE_HISTORY) {
                dataChangeHistoryDao.deleteChildrenOfDataChangeHistory(uid)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveItemFromPersonalSync(itemWrapper: DataIdentifierExtraDataWrapper<out SyncObject>): Boolean {
        return saveItemFromSync(itemWrapper, DataSaver.SaveRequest.Origin.PERSONAL_SYNC)
    }

    fun saveItemFromSharingSync(itemWrapper: DataIdentifierExtraDataWrapper<out SyncObject>): Boolean {
        return saveItemFromSync(itemWrapper, DataSaver.SaveRequest.Origin.SHARING_SYNC)
    }

    override suspend fun <T : SyncObject> save(saveRequest: DataSaver.SaveRequest<T>, oldItems: List<VaultItem<*>>): List<VaultItem<T>> {
        val database = getDatabase() ?: return listOf()
        database.beginTransaction()
        try {
            val origin = saveRequest.origin
            val savedItems = saveRequest.itemsToSave.filter { item ->
                val oldItem = oldItems.firstOrNull { it.uid == item.uid }
                insertOrUpdateItem(
                    database = database,
                    item = item,
                    itemInDb = oldItem,
                    origin = origin
                )
            }
            database.setTransactionSuccessful()
            return savedItems
        } finally {
            database.endTransaction()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun saveItemFromSync(
        itemWrapper: DataIdentifierExtraDataWrapper<out SyncObject>,
        origin: DataSaver.SaveRequest.Origin
    ): Boolean {
        val item = itemWrapper.vaultItem
        return if (item.syncObject is SyncObject.DataChangeHistory) {
            dataChangeHistoryDao.insertOrUpdateItem(
                item as VaultItem<SyncObject.DataChangeHistory>,
                itemWrapper.extraData,
                itemWrapper.backupDate
            )
        } else {
            
            val oldItem = try {
                
                val dataType = SyncObjectType[item]!!
                getItemFromDb(item.uid, dataType)
            } catch (e: Exception) {
                return false
            }
            val database = getDatabase() ?: return false
            insertOrUpdateItem(
                database = database,
                item = item,
                extraData = itemWrapper.extraData,
                backupDate = itemWrapper.backupDate,
                itemInDb = oldItem,
                origin = origin
            )
        }
    }

    private fun insertOrUpdateItem(
        database: Database,
        item: VaultItem<*>,
        extraData: String? = null,
        backupDate: Instant? = null,
        itemInDb: VaultItem<*>?,
        origin: DataSaver.SaveRequest.Origin
    ): Boolean {
        if (Thread.interrupted()) {
            return false
        }
        try {
            
            val dataType = SyncObjectType[item]!!

            val mergedVaultItem = if (SharedItemMerger.needSharingMerge(itemInDb, item)) {
                SharedItemMerger.mergeVaultItemToSave(itemInDb, item, origin)
            } else {
                item
            } ?: return false
            val contentValuesToSave = WithExtraDataDbConverter.toContentValues(mergedVaultItem, extraData, backupDate)

            val tableName = dataType.getTableName()!!
            if (itemInDb == null) {
                database.insertWithOnConflict(
                    tableName, null,
                    contentValuesToSave,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            } else {
                database.updateItems(
                    contentValuesToSave!!,
                    tableName,
                    DataIdentifierSql.FIELD_ID + " = ?",
                    arrayOf(itemInDb.id.toInt().toString())
                )
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun Database.updateItems(
        cv: ContentValues,
        tableName: String,
        whereStatement: String,
        selectionArgs: Array<String>
    ) {
        update(tableName, cv, whereStatement, selectionArgs)
    }

    private fun getDatabase(): Database? {
        return sessionManager.session?.let { userDataRepository.getDatabase(it) }
    }

    private fun getItemFromDb(itemUid: String, dataType: SyncObjectType): VaultItem<*>? {
        val filter = vaultFilter {
            ignoreUserLock()
            specificUid(itemUid)
            specificDataType(dataType)
        }
        return vaultDataQuery.query(filter)
    }
}