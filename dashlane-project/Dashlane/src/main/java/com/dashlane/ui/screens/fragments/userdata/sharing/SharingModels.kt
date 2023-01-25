package com.dashlane.ui.screens.fragments.userdata.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending
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

    

    data class ItemUser(
        val user: UserDownload,
        override val itemGroup: ItemGroup,
        override val item: SummaryObject
    ) : SharingModels() {
        override val isAccepted: Boolean
            get() = user.isAccepted
        override val isPending: Boolean
            get() = user.isPending
        override val isMemberAdmin: Boolean
            get() = user.isAdmin
        override val sharingStatusResource: Int
            get() = user.getSharingStatusResource()
    }

    

    data class ItemUserGroup(
        val userGroup: UserGroupMember,
        override val itemGroup: ItemGroup,
        override val item: SummaryObject
    ) : SharingModels() {
        override val isAccepted: Boolean
            get() = userGroup.isAccepted
        override val isPending: Boolean
            get() = userGroup.isPending
        override val isMemberAdmin: Boolean
            get() = userGroup.isAdmin
        override val sharingStatusResource: Int
            get() = userGroup.getSharingStatusResource()
    }
}



data class SharingUserGroupUser(
    val userGroup: UserGroup,
    val user: UserDownload
) {
    val sharingStatusResource: Int
        get() = user.getSharingStatusResourceShort()
}
