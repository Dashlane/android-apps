package com.dashlane.sharing.internal.builder.request

import androidx.annotation.WorkerThread
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateMultipleItemGroupsService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateMultipleItemGroupsRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val createItemGroupRequestsHelper: CreateItemGroupRequestsHelper
) {
    @Throws(RequestBuilderException.CreateItemRequestException::class)
    suspend fun create(
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        items: List<Pair<ItemToShare, ItemForEmailing>>,
        auditLogs: AuditLogDetails?
    ): CreateMultipleItemGroupsService.Request = withContext(defaultCoroutineDispatcher) {
        val itemGroups = items.map {
            createItemGroupRequestsHelper.generateItemGroup(
                item = it.first,
                users = users,
                groups = groups,
                itemForEmailing = it.second,
                auditLogs = auditLogs
            )
        }
        build(itemGroups)
    }

    @WorkerThread
    private fun build(itemGroups: List<CreateMultipleItemGroupsService.Request.ItemGroup>): CreateMultipleItemGroupsService.Request {
        return CreateMultipleItemGroupsService.Request(itemGroups)
    }
}