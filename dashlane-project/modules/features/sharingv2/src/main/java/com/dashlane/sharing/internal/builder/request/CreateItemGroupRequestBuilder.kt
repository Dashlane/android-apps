package com.dashlane.sharing.internal.builder.request

import androidx.annotation.WorkerThread
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateMultipleItemGroupsService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateItemGroupRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val createItemGroupRequestsHelper: CreateItemGroupRequestsHelper
) {
    @Throws(RequestBuilderException.CreateItemRequestException::class)
    suspend fun create(
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        item: ItemToShare,
        itemForEmailing: ItemForEmailing,
        auditLogs: AuditLogDetails?
    ): CreateItemGroupService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val itemGroup = createItemGroupRequestsHelper.generateItemGroup(
                item = item,
                users = users,
                groups = groups,
                itemForEmailing = itemForEmailing,
                auditLogs = auditLogs
            )
            build(itemGroup)
        }
    }

    @WorkerThread
    private fun build(itemGroup: CreateMultipleItemGroupsService.Request.ItemGroup) =
        CreateItemGroupService.Request(
            itemsForEmailing = itemGroup.itemsForEmailing,
            groupId = itemGroup.groupId,
            auditLogDetails = itemGroup.auditLogDetails,
            groups = itemGroup.groups,
            items = itemGroup.items,
            users = itemGroup.users
        )
}