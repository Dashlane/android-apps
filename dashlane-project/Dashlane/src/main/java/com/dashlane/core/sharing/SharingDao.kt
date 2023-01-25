package com.dashlane.core.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.dao.SharingDataType
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy



interface SharingDao {

    val vaultDataQuery: VaultDataQuery
    val dataSaver: Lazy<DataSaver>

    val databaseOpen: Boolean

    

    suspend fun getItemsSummary(
        dataType: SharingDataType
    ): List<Pair<String, Long>>

    

    suspend fun updateItemTimestamp(uid: String, timestamp: Long)

    

    suspend fun getItemKeyTimestamp(uid: String): Pair<String, Long>?
    fun isDirtyForSharing(id: String, type: SyncObjectType): Boolean

    

    suspend fun getDirtyForSharing(): List<DataIdentifierExtraDataWrapper<out SyncObject>>

    suspend fun markAsShared(uids: List<String>)

    fun loadItemGroup(itemGroupUid: String): ItemGroup?

    fun loadAllItemGroup(): List<ItemGroup>

    fun loadAllUserGroup(): List<UserGroup>

    fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup>
    suspend fun saveAsLocalItem(
        identifier: String,
        extraData: String,
        userPermission: String
    )

    suspend fun saveAsLocalItem(
        objectToSave: DataIdentifierExtraDataWrapper<out SyncObject>?,
        userPermission: String
    )

    fun loadItemContentExtraData(itemUid: String): String?
    suspend fun deleteItemGroups(memory: SharingDaoMemoryDataAccess, itemGroupsUid: List<String>)
    suspend fun deleteLocallyItemGroupAndItems(
        memory: SharingDaoMemoryDataAccess,
        itemGroup: ItemGroup
    )

    fun getExtraData(uid: String): String?

    fun loadItemGroupForItem(itemUID: String): ItemGroup?

    fun loadUserGroupsAccepted(userId: String): List<UserGroup>?

    fun loadUserGroup(userGroupId: String): UserGroup?

    fun getItemWithExtraData(
        id: String,
        dataType: SyncObjectType
    ): DataIdentifierExtraDataWrapper<SyncObject>?

    suspend fun duplicateDataIdentifier(itemId: String): Boolean {
        val vaultItem = loadVaultItem(itemId) ?: return false
        
        val new = vaultItem.copyWithAttrs {
            id = 0 
            uid = generateUniqueIdentifier()
            
            syncState = SyncState.MODIFIED
            sharingPermission = null
        }
        return dataSaver.get().save(new)
    }

    suspend fun updatePermission(
        itemUid: String,
        permission: String
    ) {
        
        val vaultItem = loadVaultItem(itemUid)
        if (vaultItem != null) {
            
            val new = vaultItem.copyWithAttrs {
                sharingPermission = permission
            }
            
            dataSaver.get().save(new)
        }
    }

    suspend fun deleteDataIdentifier(itemId: String) {
        loadVaultItem(itemId)?.let { deleteDataIdentifier(it) }
    }

    private suspend fun deleteDataIdentifier(vaultItem: VaultItem<*>) {
        val new = vaultItem.copyWithAttrs {
            syncState = SyncState.DELETED
        }
        dataSaver.get().save(new)
    }

    private fun loadVaultItem(itemUid: String): VaultItem<*>? =
        vaultDataQuery.getSharableItem(itemUid)
}