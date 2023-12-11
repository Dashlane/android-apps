package com.dashlane.ui.screens.fragments.userdata.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember
import com.dashlane.vault.summary.SummaryObject

sealed class SharingModels {
    abstract val item: SummaryObject
    abstract val itemGroup: ItemGroup
    val isAdmin: Boolean
        get() = Permission.ADMIN.key == item.sharingPermission
    abstract val isMemberAdmin: Boolean
    abstract val isPending: Boolean
    abstract val isAccepted: Boolean
    abstract val sharingStatusResource: Int
    abstract val isItemInCollection: Boolean

    data class ItemUser(
        val userId: String,
        override val isAccepted: Boolean,
        override val isPending: Boolean,
        override val isMemberAdmin: Boolean,
        override val sharingStatusResource: Int,
        override val itemGroup: ItemGroup,
        override val item: SummaryObject,
        override val isItemInCollection: Boolean
    ) : SharingModels()

    data class ItemUserGroup(
        val groupId: String,
        val name: String,
        override val isAccepted: Boolean,
        override val isPending: Boolean,
        override val isMemberAdmin: Boolean,
        override val sharingStatusResource: Int,
        override val itemGroup: ItemGroup,
        override val item: SummaryObject,
        override val isItemInCollection: Boolean
    ) : SharingModels()
}

data class SharingUserGroupUser(
    val userGroup: UserGroup,
    val user: UserDownload
) {
    val sharingStatusResource: Int
        get() = user.getSharingStatusResourceShort()
}
