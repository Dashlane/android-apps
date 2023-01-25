package com.dashlane.core.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup



data class SharingItemUpdaterRequest(
    val itemGroupUpdates: List<ItemGroup> = emptyList(),
    val itemContentUpdates: List<ItemContent> = emptyList(),
    val userGroupUpdates: List<UserGroup> = emptyList(),
    val itemsDeletionIds: List<String> = emptyList(),
    val itemGroupsDeletionIds: List<String> = emptyList(),
    val userGroupDeletionIds: List<String> = emptyList()
) {

    companion object {
        @JvmStatic
        fun toSaveItemGroup(
            itemGroup: ItemGroup?,
            itemContent: ItemContent?
        ): SharingItemUpdaterRequest {
            return SharingItemUpdaterRequest(
                itemGroupUpdates = listOfNotNull(itemGroup),
                itemContentUpdates = listOfNotNull(itemContent)
            )
        }

        @JvmStatic
        fun toSaveUserGroup(userGroup: UserGroup): SharingItemUpdaterRequest {
            return SharingItemUpdaterRequest(
                userGroupUpdates = listOf(userGroup)
            )
        }
    }
}