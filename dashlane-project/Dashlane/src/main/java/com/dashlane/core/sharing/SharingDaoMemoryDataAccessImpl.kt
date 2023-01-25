package com.dashlane.core.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.storage.userdata.Database
import com.dashlane.storage.userdata.dao.ItemContentDB
import com.dashlane.storage.userdata.dao.SharingItemContentDao
import com.dashlane.storage.userdata.dao.SharingItemGroupDao
import com.dashlane.storage.userdata.dao.SharingUserGroupDao
import com.dashlane.util.JsonSerialization

class SharingDaoMemoryDataAccessImpl(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val jsonSerialization: JsonSerialization
) : SharingDaoMemoryDataAccess {
    override lateinit var itemGroups: MutableList<ItemGroup>
    override lateinit var userGroups: MutableList<UserGroup>
    override lateinit var itemContentsDB: MutableList<ItemContentDB>

    override val itemGroupsToDelete: MutableList<String> = mutableListOf()
    override val userGroupsToDelete: MutableList<String> = mutableListOf()
    override val itemContentsDBToDelete: MutableList<String> = mutableListOf()

    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getDatabase(it) }

    private val itemGroupDao: SharingItemGroupDao?
        get() = database?.let { SharingItemGroupDao(jsonSerialization, it) }

    private val userGroupDao: SharingUserGroupDao?
        get() = database?.let { SharingUserGroupDao(jsonSerialization, it) }

    private val itemContentDao: SharingItemContentDao?
        get() = database?.let { SharingItemContentDao(jsonSerialization, it) }

    override suspend fun init() {
        itemGroups = itemGroupDao?.loadAll()?.toMutableList() ?: mutableListOf()
        userGroups = userGroupDao?.loadAll()?.toMutableList() ?: mutableListOf()
        itemContentsDB = itemContentDao?.loadAll()?.toMutableList() ?: mutableListOf()
    }

    override fun saveItemGroups(itemGroups: List<ItemGroup>) {
        this.itemGroups.addOrReplaceItemGroup(itemGroups)
    }

    override fun save(itemId: String, timestamp: Long, extraData: String, itemKeyBase64: String) {
        itemContentsDB.apply {
            removeIf { it.id == itemId }
            add(ItemContentDB(itemId, timestamp, extraData, itemKeyBase64))
        }
    }

    override fun saveUserGroups(userGroups: List<UserGroup>) {
        this.userGroups.addOrReplaceUserGroup(userGroups)
    }

    override fun deleteUserGroups(uidToDelete: List<String>) {
        userGroups.removeAll { it.groupId in uidToDelete }
        userGroupsToDelete.addAll(uidToDelete)
    }

    override fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup> {
        return userGroups.filter {
            it.getUser(userId)?.isAcceptedOrPending == true
        }
    }

    override fun deleteItemGroups(itemGroupsUid: List<String>) {
        itemGroups.removeAll { itemGroup -> itemGroup.groupId in itemGroupsUid }
        itemGroupsToDelete.addAll(itemGroupsUid)
    }

    override fun deleteItemContent(itemId: String) {
        itemContentsDB.removeAll { it.id == itemId }
        itemContentsDBToDelete.add(itemId)
    }

    override fun delete(itemGroup: ItemGroup) {
        itemGroups.removeAll { it.groupId == itemGroup.groupId }
        itemGroupsToDelete.add(itemGroup.groupId)
    }

    override fun loadItemContentExtraData(itemUid: String): String? {
        return itemContentsDB.find { it.id == itemUid }?.extraData
    }

    override suspend fun close() {
        itemGroupDao?.apply {
            itemGroups.forEach { save(it) }
            itemGroupsToDelete.forEach { delete(it) }
        }
        userGroupDao?.apply {
            userGroups.forEach { save(it) }
            userGroupsToDelete.forEach { delete(it) }
        }
        itemContentDao?.apply {
            itemContentsDB.forEach { saveItemContent(it) }
            itemContentsDBToDelete.forEach { delete(it) }
        }
    }
}
