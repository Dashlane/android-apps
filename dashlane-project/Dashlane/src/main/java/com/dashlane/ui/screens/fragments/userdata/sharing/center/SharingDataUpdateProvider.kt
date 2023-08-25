package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingGetService
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
        runCatching { getUpdatedRevision(itemGroupId = itemGroup.groupId) }.getOrNull()
            ?.let {
                itemGroup.copy(revision = it)
            }

    suspend fun getUpdatedUserGroup(userGroup: UserGroup): UserGroup? =
        runCatching { getUpdatedRevision(userGroupId = userGroup.groupId) }.getOrNull()
            ?.let {
                userGroup.copy(revision = it)
            }

    private suspend fun getUpdatedRevision(
        itemGroupId: String? = null,
        userGroupId: String? = null
    ): Long? {
        val authorization = session?.authorization ?: return null
        if (itemGroupId != null && userGroupId != null) return null

        val result = sharingGetService.execute(
            authorization,
            SharingGetService.Request(
            itemGroupIds = itemGroupId?.let { listOf(SharingGetService.Request.ItemGroupId(it)) }
                ?: emptyList(),
            userGroupIds = userGroupId?.let { listOf(SharingGetService.Request.UserGroupId(it)) }
                ?: emptyList()
        )
        )

        return result.data.itemGroups?.singleOrNull()?.revision
            ?: result.data.userGroups?.singleOrNull()?.revision
    }
}
