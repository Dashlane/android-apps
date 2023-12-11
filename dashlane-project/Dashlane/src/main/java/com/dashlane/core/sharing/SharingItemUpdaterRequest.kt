package com.dashlane.core.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup

data class SharingItemUpdaterRequest(
    val itemGroupUpdates: List<ItemGroup> = emptyList(),
    val itemContentUpdates: List<ItemContent> = emptyList(),
    val userGroupUpdates: List<UserGroup> = emptyList(),
    val collectionUpdates: List<Collection> = emptyList(),
    val itemsDeletionIds: List<String> = emptyList(),
    val itemGroupsDeletionIds: List<String> = emptyList(),
    val userGroupDeletionIds: List<String> = emptyList(),
    val collectionsDeletionIds: List<String> = emptyList()
)