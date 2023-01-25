package com.dashlane.sharing.internal.model

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

open class ItemToShare(val itemId: String, val content: String)

data class UserToUpdate(
    val userId: String,
    val permission: Permission,
    val publicKey: String
)

class ItemToUpdate(
    itemId: String,
    content: String,
    val previousTimestamp: Long
) : ItemToShare(itemId, content)