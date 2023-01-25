package com.dashlane.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingGetService
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateItemService
import com.dashlane.sharing.internal.model.ItemToUpdate
import javax.inject.Inject

class SharingSyncCommunicatorImpl @Inject constructor(
    private val sharingGetService: SharingGetService,
    private val updateItemService: UpdateItemService
) : SharingSyncCommunicator {

    override suspend fun get(
        session: Authorization.User,
        itemUids: List<String>,
        itemGroupUids: List<String>,
        userGroupUids: List<String>
    ): SharingSyncCommunicator.GetSharingResult {
        val request = SharingGetService.Request(
            itemIds = itemUids.map { SharingGetService.Request.ItemId(it) },
            itemGroupIds = itemGroupUids.map { SharingGetService.Request.ItemGroupId(it) },
            userGroupIds = userGroupUids.map { SharingGetService.Request.UserGroupId(it) },
        )
        return run {
            val response = sharingGetService.execute(session, request)
            val sharing = response.data
            SharingSyncCommunicator.GetSharingResult(
                itemGroups = sharing.itemGroups ?: emptyList(),
                userGroup = sharing.userGroups ?: emptyList(),
                itemContents = sharing.items ?: emptyList(),
            )
        }
    }

    override suspend fun updateItems(
        session: Authorization.User,
        items: List<ItemToUpdate>
    ): List<ItemContent> {
        return items.mapNotNull {
            val request = UpdateItemService.Request(
                itemId = UpdateItemService.Request.ItemId(it.itemId),
                content = it.content,
                timestamp = it.previousTimestamp.toDouble(),
            )
            updateItemService.execute(session, request).data.items
        }.flatten()
    }
}