package com.dashlane.core.sharing

import android.content.ContentValues
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.database.converter.DbConverter
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.DataSyncDao
import com.dashlane.storage.userdata.Database
import com.dashlane.storage.userdata.DatabaseItemSaverImplLegacy
import com.dashlane.storage.userdata.SqlQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplLegacy
import com.dashlane.storage.userdata.dao.SharingDataType
import com.dashlane.storage.userdata.dao.SharingItemContentDao
import com.dashlane.storage.userdata.dao.SharingItemGroupDao
import com.dashlane.storage.userdata.dao.SharingUserGroupDao
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.WithExtraDataDbConverter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.getSuggestedTeamspace
import com.dashlane.util.JsonSerialization
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.model.UserPermission
import com.dashlane.util.toList
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject



class SharingDaoImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val xmlConverterLazy: Lazy<DataIdentifierSharingXmlConverter>,
    override val dataSaver: Lazy<DataSaver>,
    override val vaultDataQuery: VaultDataQueryImplLegacy,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val jsonSerialization: JsonSerialization,
    private val databaseItemSaver: DatabaseItemSaverImplLegacy,
    private val dataSyncDao: DataSyncDao
) : SharingDao {

    override val databaseOpen: Boolean
        get() = database != null

    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getDatabase(it) }

    private val itemGroupDao: SharingItemGroupDao?
        get() = database?.let { SharingItemGroupDao(jsonSerialization, it) }

    private val userGroupDao: SharingUserGroupDao?
        get() = database?.let { SharingUserGroupDao(jsonSerialization, it) }

    private val itemContentDao: SharingItemContentDao?
        get() = database?.let { SharingItemContentDao(jsonSerialization, it) }

    

    override suspend fun getItemsSummary(
        dataType: SharingDataType
    ): List<Pair<String, Long>> = withContext(Dispatchers.IO) {
        val database = database ?: return@withContext emptyList<Pair<String, Long>>()

        val idColumnName = dataType.idColumnName
        val revisionColumnName = dataType.revisionColumnName
        database.query(SqlQuery(dataType.tableName, listOf(idColumnName, revisionColumnName))).use { cursor ->
            val idColumnIndex = cursor.getColumnIndexOrThrow(idColumnName)
            val revisionColumnIndex = cursor.getColumnIndexOrThrow(revisionColumnName)
            cursor.toList {
                getString(idColumnIndex) to getLong(revisionColumnIndex)
            }
        }
    }

    

    override suspend fun updateItemTimestamp(uid: String, timestamp: Long) = withContext(Dispatchers.IO) {
        val database = database ?: return@withContext

        val cv = ContentValues().apply {
            put(SharingDataType.ColumnName.ITEM_TIMESTAMP, timestamp)
        }
        database.update(SharingDataType.TableName.ITEM, cv, SharingDataType.ColumnName.ITEM_ID + " = ?", arrayOf(uid))
    }

    

    override suspend fun getItemKeyTimestamp(uid: String): Pair<String, Long>? = withContext(Dispatchers.IO) {
        val database = database ?: return@withContext null

        database.query(
            SqlQuery(
                SharingDataType.TableName.ITEM,
                listOf(SharingDataType.ColumnName.ITEM_TIMESTAMP, SharingDataType.ColumnName.ITEM_KEY),
                "${SharingDataType.ColumnName.ITEM_ID} = ?",
                listOf(uid)
            )
        ).use { cursor ->
            val timestampColumnIndex = cursor.getColumnIndexOrThrow(SharingDataType.ColumnName.ITEM_TIMESTAMP)
            val keyColumnIndex = cursor.getColumnIndexOrThrow(SharingDataType.ColumnName.ITEM_KEY)
            cursor.toList {
                val timestamp = getLong(timestampColumnIndex)
                val key = getString(keyColumnIndex)
                if (key == null || timestamp == 0L) {
                    null
                } else {
                    key to timestamp
                }
            }
        }.single()
    }

    override fun isDirtyForSharing(id: String, type: SyncObjectType): Boolean {
        val database = database ?: return false
        val tableName = type.getTableName() ?: return false

        return database.query(
            SqlQuery(
                tableName,
                columns = listOf(DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD),
                selection = DataIdentifierSql.FIELD_UID + " = ?",
                selectionArgs = listOf(id)
            )
        ).use { cursor ->
            if (!cursor.moveToFirst()) return false
            val columnIndex = cursor.getColumnIndex(DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD)
            cursor.getInt(columnIndex) != 0
        }
    }

    

    override suspend fun getDirtyForSharing(): List<DataIdentifierExtraDataWrapper<out SyncObject>> =
        withContext(Dispatchers.IO) {
            val database =
                database
                    ?: return@withContext emptyList<DataIdentifierExtraDataWrapper<out SyncObject>>()

            SyncObjectTypeUtils.SHAREABLE.flatMap { dataType ->
                database.query(
                    SqlQuery(
                        dataType.getTableName()!!,
                        selection = DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD + " = 1"
                    )
                ).use { cursor ->
                    cursor.toList {
                        val vaultItem = DbConverter.fromCursor(this, dataType)!!
                        WithExtraDataDbConverter.cursorToItem(this, vaultItem)
                    }
                }
            }
        }

    override suspend fun markAsShared(uids: List<String>) = withContext(Dispatchers.IO) {
        val database = database ?: return@withContext
        for (dataType in SyncObjectTypeUtils.SHAREABLE) {
            val query = """
                UPDATE ${dataType.getTableName()}
                SET ${DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD} = 0
                WHERE ${DataIdentifierSql.FIELD_UID} IN (${uids.joinToString(",") { "'$it'" }})
                """.trimIndent()
            database.executeRawExecSQL(query)
        }
    }

    override fun loadItemGroup(itemGroupUid: String): ItemGroup? =
        itemGroupDao?.load(itemGroupUid)

    override fun loadAllItemGroup(): List<ItemGroup> =
        itemGroupDao?.loadAll() ?: emptyList()

    override fun loadAllUserGroup(): List<UserGroup> =
        userGroupDao?.loadAll() ?: emptyList()

    override fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup> =
        userGroupDao?.loadUserGroupsAcceptedOrPending(userId) ?: emptyList()

    override suspend fun saveAsLocalItem(
        identifier: String,
        extraData: String,
        @UserPermission
        userPermission: String
    ) = saveAsLocalItem(
        xmlConverterLazy.get().fromXml(identifier, extraData),
        userPermission
    )

    override suspend fun saveAsLocalItem(
        objectToSave: DataIdentifierExtraDataWrapper<out SyncObject>?,
        @UserPermission
        userPermission: String
    ) {
        if (objectToSave == null) {
            return
        }
        val teamspaceAccessor = teamspaceAccessorProvider.get() ?: return
        val vaultItem = objectToSave.vaultItem.let {
            it.copyWithAttrs { teamSpaceId = it.getSuggestedTeamspace(teamspaceAccessor)?.teamId }
        }
        val newObjectToSave = DataIdentifierExtraDataWrapper(
            vaultItem.copyWithAttrs {
                sharingPermission = userPermission
            },
            objectToSave.extraData,
            objectToSave.backupDate
        )
        databaseItemSaver.saveItemFromSharingSync(newObjectToSave)
    }

    override fun loadItemContentExtraData(itemUid: String): String? {
        return itemContentDao?.getExtraData(itemUid)
    }

    override suspend fun deleteItemGroups(memory: SharingDaoMemoryDataAccess, itemGroupsUid: List<String>) {
        memory.itemGroups.filter {
            it.groupId in itemGroupsUid
        }.forEach {
            deleteLocallyItemGroupAndItems(memory, it)
        }
    }

    override suspend fun deleteLocallyItemGroupAndItems(memory: SharingDaoMemoryDataAccess, itemGroup: ItemGroup) {
        val items = itemGroup.items ?: return
        for (item in items) {
            val itemId = item.itemId
            
            memory.deleteItemContent(itemId)
            
            deleteDataIdentifier(itemId)
        }
        memory.delete(itemGroup)
    }

    override fun getExtraData(uid: String): String? = itemContentDao?.getExtraData(uid)

    override fun loadItemGroupForItem(itemUID: String): ItemGroup? = itemGroupDao?.loadForItem(itemUID)

    override fun loadUserGroupsAccepted(userId: String): List<UserGroup>? =
        userGroupDao?.loadUserGroupsAccepted(userId)

    override fun loadUserGroup(userGroupId: String): UserGroup? = userGroupDao?.load(userGroupId)

    override fun getItemWithExtraData(
        id: String,
        dataType: SyncObjectType
    ): DataIdentifierExtraDataWrapper<SyncObject>? = dataSyncDao.getItemWithExtraData(id, dataType)
}