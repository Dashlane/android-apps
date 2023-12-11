package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingGetService
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingServerResponse
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import javax.inject.Inject

class SharingDataUpdateProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingGetService: SharingGetService
) {
    private val session: Session?
        get() = sessionManager.session

    suspend fun getUpdatedItemGroup(itemGroup: ItemGroup): ItemGroup? =
        getUpdatedItemGroups(listOf(itemGroup))?.firstOrNull()

    suspend fun getUpdatedItemGroups(itemGroups: List<ItemGroup>): List<ItemGroup>? =
        runCatching { getUpdatedRevision(itemGroupIds = itemGroups.map { it.groupId }) }.getOrNull()
            ?.itemGroups

    suspend fun getUpdatedUserGroup(userGroup: UserGroup): UserGroup? =
        runCatching { getUpdatedRevision(userGroupIds = listOf(userGroup.groupId)) }.getOrNull()
            ?.userGroups?.firstOrNull()

    suspend fun getUpdatedCollection(collection: Collection): Collection? =
        runCatching { getUpdatedRevision(collectionIds = listOf(collection.uuid)) }.getOrNull()
            ?.collections?.firstOrNull()

    private suspend fun getUpdatedRevision(
        itemGroupIds: List<String>? = null,
        userGroupIds: List<String>? = null,
        collectionIds: List<String>? = null,
    ): SharingServerResponse? {
        val authorization = session?.authorization ?: return null
        if (itemGroupIds != null && userGroupIds != null) return null
        val itemGroupRequests = itemGroupIds?.map { SharingGetService.Request.ItemGroupId(it) }
        val userGroupRequests = userGroupIds?.map { SharingGetService.Request.UserGroupId(it) }
        val collectionRequests = collectionIds?.map { SharingGetService.Request.CollectionId(it) }
        val result = sharingGetService.execute(
            authorization,
            SharingGetService.Request(
                itemGroupIds = itemGroupRequests ?: emptyList(),
                userGroupIds = userGroupRequests ?: emptyList(),
                collectionIds = collectionRequests ?: emptyList()
            )
        )
        return result.data
    }
}
