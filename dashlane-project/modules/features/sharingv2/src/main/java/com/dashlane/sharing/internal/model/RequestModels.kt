package com.dashlane.sharing.internal.model

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup

data class UserToInvite(
    val userId: String,
    val alias: String,
    val permission: Permission,
    val publicKey: String?
)

data class GroupToInvite(
    val userGroup: UserGroup,
    val permission: Permission
)

data class ItemToShare(
    val itemId: String,
    val content: String,
    val itemType: ItemUpload.ItemType
)

data class UserToUpdate(
    val userId: String,
    val permission: Permission,
    val publicKey: String
)

data class ItemToUpdate(
    val itemId: String,
    val content: String,
    val previousTimestamp: Long
)