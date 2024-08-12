package com.dashlane.sharing.model

import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup

fun List<Collection>.getMyCollectionsAcceptedOrPending(
    userId: String,
    myUserGroupsAcceptedOrPending: List<UserGroup>
): List<Collection> {
    return filter { collection ->
        if (collection.getUser(userId)?.isAcceptedOrPending == true) return@filter true
        val userGroups = collection.userGroups ?: return@filter false
        val ids =
            userGroups.map { it.uuid } intersect myUserGroupsAcceptedOrPending.map { it.groupId }
                .toSet()
        if (userGroups.find { userGroup ->
                userGroup.uuid in ids && userGroup.status.isAcceptedOrPending
            } != null
        ) {
            return@filter true
        }
        false
    }
}