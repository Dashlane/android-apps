package com.dashlane.ui.screens.fragments.userdata.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.session.Session
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import javax.inject.Inject



class SharingRevokeAllMembers @Inject constructor(
    private val sharingDataProvider: SharingDataProvider
) {
    suspend fun execute(
        session: Session,
        itemGroup: ItemGroup
    ) {
        val userId = session.userId
        val userIds = itemGroup.users?.filter { it.isAcceptedOrPending && it.userId != userId }
            ?.map { it.userId }
        val userGroupIds = itemGroup.groups?.filter { it.isAcceptedOrPending }
            ?.map { it.groupId }
        sharingDataProvider.cancelInvitationUsersAndUserGroups(itemGroup, userIds, userGroupIds, true)
    }
}