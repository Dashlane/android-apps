package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.vault.summary.SummaryObject
typealias UserGroupModel = UserGroup

sealed class SharingContact {
    data class UserGroup(
        val userGroup: UserGroupModel,
        val itemCount: Int
    ) {
        val name: String
            get() = userGroup.name
        val groupId: String
            get() = userGroup.groupId
        val teamId: String?
            get() = userGroup.teamId?.toString()
        val memberCount: Int
            get() = userGroup.users.size
    }

    data class User(
        val name: String,
        val itemIds: List<String>
    ) {
        val count: Int
            get() = itemIds.size
    }

    data class ItemInvite(
        val itemGroup: ItemGroup,
        val item: SummaryObject,
        val login: String
    )

    data class UserGroupInvite(
        val userGroup: UserGroupModel,
        val login: String
    )
}
