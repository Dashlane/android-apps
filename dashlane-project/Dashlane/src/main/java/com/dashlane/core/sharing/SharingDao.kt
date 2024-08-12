package com.dashlane.core.sharing

import androidx.annotation.Discouraged
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.dao.SharingDataType
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.toCollectionVaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy

interface SharingDao {

    val vaultDataQuery: VaultDataQuery
    val dataSaver: Lazy<DataSaver>
    val collectionDataQuery: Lazy<CollectionDataQuery>

    val databaseOpen: Boolean

    suspend fun getItemsSummary(
        dataType: SharingDataType
    ): List<Pair<String, Long>>

    suspend fun updateItemTimestamp(uid: String, timestamp: Long)

    suspend fun getItemKeyTimestamp(uid: String): Pair<String, Long>?
    suspend fun isDirtyForSharing(id: String, type: SyncObjectType): Boolean

    suspend fun getDirtyForSharing(): List<DataIdentifierExtraDataWrapper<out SyncObject>>

    suspend fun markAsShared(uids: List<String>)

    @Discouraged("Use the suspend version instead")
    fun loadItemGroupLegacy(itemGroupUid: String): ItemGroup?
    suspend fun loadItemGroup(itemGroupUid: String): ItemGroup?

    @Discouraged("Use the suspend version instead")
    fun loadAllItemGroupLegacy(): List<ItemGroup>
    suspend fun loadAllItemGroup(): List<ItemGroup>

    @Discouraged("Use the suspend version instead")
    fun loadAllUserGroupLegacy(): List<UserGroup>
    suspend fun loadAllUserGroup(): List<UserGroup>

    @Discouraged("Use the suspend version instead")
    fun loadUserGroupsAcceptedOrPendingLegacy(userId: String): List<UserGroup>
    suspend fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup>

    @Discouraged("Use the suspend version instead")
    fun loadAllCollectionLegacy(): List<Collection>
    suspend fun loadAllCollection(): List<Collection>

    fun loadCollectionsAcceptedOrPending(
        userId: String,
        myUserGroupsAcceptedOrPending: List<UserGroup>
    ): List<Collection>

    suspend fun saveAsLocalItem(
        identifier: String,
        extraData: String,
        userPermission: String
    )

    suspend fun saveAsLocalItem(
        objectToSave: DataIdentifierExtraDataWrapper<out SyncObject>?,
        userPermission: String
    )

    @Discouraged("Use the suspend version instead")
    fun loadItemContentExtraDataLegacy(itemUid: String): String?
    suspend fun loadItemContentExtraData(itemUid: String): String?

    suspend fun deleteItemGroups(memory: SharingDaoMemoryDataAccess, itemGroupsUid: List<String>)
    suspend fun deleteLocallyItemGroupAndItems(
        memory: SharingDaoMemoryDataAccess,
        itemGroup: ItemGroup
    )

    @Discouraged("Use the suspend version instead")
    fun loadItemGroupForItemLegacy(itemUID: String): ItemGroup?
    suspend fun loadItemGroupForItem(itemUID: String): ItemGroup?

    @Discouraged("Use the suspend version instead")
    fun loadUserGroupsAcceptedLegacy(userId: String): List<UserGroup>?
    suspend fun loadUserGroupsAccepted(userId: String): List<UserGroup>?

    @Discouraged("Use the suspend version instead")
    fun loadUserGroupLegacy(userGroupId: String): UserGroup?
    suspend fun loadUserGroup(userGroupId: String): UserGroup?

    @Discouraged("Use the suspend version instead")
    fun getItemWithExtraDataLegacy(id: String, dataType: SyncObjectType): DataIdentifierExtraDataWrapper<SyncObject>?

    suspend fun getItemWithExtraData(id: String, dataType: SyncObjectType): DataIdentifierExtraDataWrapper<SyncObject>?

    suspend fun duplicateDataIdentifier(itemId: String): VaultItem<*>? {
        val vaultItem = loadVaultItem(itemId) ?: return null
        
        val new = vaultItem.copyWithAttrs {
            id = 0 
            uid = generateUniqueIdentifier()
            
            syncState = SyncState.MODIFIED
            sharingPermission = null
        }
        dataSaver.get().save(new)
        return new
    }

    suspend fun updatePrivateCollections(
        newItem: VaultItem<*>,
        oldItemId: String
    ) {
        if (newItem.syncObjectType != SyncObjectType.AUTHENTIFIANT) return
        val dataQuery = collectionDataQuery.get()
        val collections = dataQuery.queryAll().filter { collection ->
            collection.vaultItems?.any { it.id == oldItemId } == true
        }
        if (collections.isNotEmpty()) {
            dataQuery.queryByIds(collections.map { it.id }).forEach { collection ->
                val updatedCollection = collection
                    .copySyncObject {
                        val currentItems = vaultItems?.filterNot { it.id == oldItemId }
                            ?: emptyList()
                        vaultItems = currentItems + newItem.toCollectionVaultItem()
                    }
                    .copyWithAttrs {
                        syncState = SyncState.MODIFIED
                    }
                dataSaver.get().save(updatedCollection)
            }
        }
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