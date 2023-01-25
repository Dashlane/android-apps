package com.dashlane.core.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.storage.userdata.dao.ItemContentDB



interface SharingDaoMemoryDataAccess {
    val itemGroups: List<ItemGroup>
    val userGroups: List<UserGroup>
    val itemContentsDB: List<ItemContentDB>
    val itemGroupsToDelete: MutableList<String>
    val userGroupsToDelete: MutableList<String>
    val itemContentsDBToDelete: MutableList<String>

    suspend fun init()

    fun saveItemGroups(itemGroups: List<ItemGroup>)
    fun save(itemId: String, timestamp: Long, extraData: String, itemKeyBase64: String)
    fun saveUserGroups(userGroups: List<UserGroup>)

    fun deleteUserGroups(uidToDelete: List<String>)
    fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup>
    fun deleteItemGroups(itemGroupsUid: List<String>)
    fun deleteItemContent(itemId: String)
    fun delete(itemGroup: ItemGroup)
    fun loadItemContentExtraData(itemUid: String): String?
    suspend fun close()
}

fun MutableList<UserGroup>.addOrReplaceUserGroup(list: List<UserGroup>) {
    removeAll { userGroup ->
        list.any { it.groupId == userGroup.groupId }
    }
    addAll(list)
}

fun MutableList<ItemGroup>.addOrReplaceItemGroup(list: List<ItemGroup>) {
    removeAll { itemGroup ->
        list.any { it.groupId == itemGroup.groupId }
    }
    addAll(list)
}