package com.dashlane.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.internal.model.ItemToUpdate

interface SharingSyncCommunicator {

    suspend fun get(
        session: Authorization.User,
        itemUids: List<String>,
        itemGroupUids: List<String>,
        userGroupUids: List<String>
    ): GetSharingResult

    suspend fun updateItems(
        session: Authorization.User,
        items: List<ItemToUpdate>
    ): List<ItemContent>

    data class GetSharingResult(
        val itemGroups: List<ItemGroup>,
        val itemContents: List<ItemContent>,
        val userGroups: List<UserGroup>
    ) {
        val isEmptyResult: Boolean
            get() = itemGroups.isEmpty() && userGroups.isEmpty() && itemContents.isEmpty()
    }
}