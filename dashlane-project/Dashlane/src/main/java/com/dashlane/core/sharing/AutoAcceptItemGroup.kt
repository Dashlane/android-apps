package com.dashlane.core.sharing

import com.dashlane.exception.NotLoggedInException
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.sharing.internal.builder.request.AcceptItemGroupRequestForUserBuilder
import com.dashlane.sharing.model.getUserGroupMember
import com.dashlane.sharing.model.isPending
import javax.inject.Inject

class AutoAcceptItemGroup @Inject constructor(
    private val acceptItemGroupService: AcceptItemGroupService,
    private val acceptItemGroupRequestBuilder: AcceptItemGroupRequestForUserBuilder
) {

    @Throws(NotLoggedInException::class)
    suspend fun execute(session: Session, itemGroup: ItemGroup, myUserGroups: List<UserGroup>):
            Pair<List<ItemGroup>, List<UserGroup>> {
        
        val items = myUserGroups
            .filter { itemGroup.getUserGroupMember(it.groupId)?.isPending == true }
            .map { userGroup ->
                val authorization = session.authorization
                val request = acceptItemGroupRequestBuilder.buildForUserGroup(
                    itemGroup,
                    userGroup
                )
                val response = acceptItemGroupService.execute(authorization, request)
                response.data
            }
        return items.mapNotNull { it.itemGroups }.flatten() to items.mapNotNull { it.userGroups }
            .flatten()
    }
}
