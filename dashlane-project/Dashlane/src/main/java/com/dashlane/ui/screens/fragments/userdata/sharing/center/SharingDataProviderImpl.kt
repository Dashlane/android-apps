package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleServerResponse
import com.dashlane.core.sharing.toItemForEmailing
import com.dashlane.core.sharing.toSharedVaultItemLite
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptUserGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.ProvisioningMethod
import com.dashlane.server.api.endpoints.sharinguserdevice.RefuseItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.RefuseUserGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ResendItemGroupInvitesService
import com.dashlane.server.api.endpoints.sharinguserdevice.RevokeItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.SharingServerResponse
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupUpdate
import com.dashlane.server.api.endpoints.sharinguserdevice.UserInviteResend
import com.dashlane.server.api.endpoints.sharinguserdevice.UserUpdate
import com.dashlane.server.api.endpoints.sharinguserdevice.exceptions.InvalidItemGroupRevisionException
import com.dashlane.server.api.endpoints.sharinguserdevice.exceptions.InvalidUserGroupRevisionException
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.internal.builder.request.AcceptItemGroupRequestForUserBuilder
import com.dashlane.sharing.internal.builder.request.AcceptUserGroupRequestBuilder
import com.dashlane.storage.DataStorageProvider
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingDataProviderImpl @Inject constructor(
    private val dataStorageProvider: DataStorageProvider,
    private val sharingXmlConverter: DataIdentifierSharingXmlConverter,
    private val sessionManager: SessionManager,
    private val sharingItemUpdater: SharingItemUpdater,
    private val acceptItemGroupRequestBuilder: AcceptItemGroupRequestForUserBuilder,
    private val acceptUserGroupRequestBuilder: AcceptUserGroupRequestBuilder,
    private val sharingItemGroupDataProvider: SharingDataUpdateProvider,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher,
    private val refuseItemGroupService: RefuseItemGroupService,
    private val refuseUserGroupService: RefuseUserGroupService,
    private val acceptItemGroupService: AcceptItemGroupService,
    private val acceptUserGroupService: AcceptUserGroupService,
    private val updateItemGroupMembersService: UpdateItemGroupMembersService,
    private val revokeItemGroupMembersService: RevokeItemGroupMembersService,
    private val resendItemGroupInvitesService: ResendItemGroupInvitesService
) : SharingDataProvider {
    private val sharingDao: SharingDao
        get() = dataStorageProvider.sharingDao

    private val session: Session?
        get() = sessionManager.session

    override suspend fun getItemGroups() =
        withContext(ioCoroutineDispatcher) { sharingDao.loadAllItemGroup() }

    override suspend fun getUserGroups() =
        withContext(ioCoroutineDispatcher) { sharingDao.loadAllUserGroup() }

    override fun getSummaryObject(itemId: String) =
        sharingXmlConverter.fromXml(identifier = itemId, xml = sharingDao.getExtraData(itemId))
            ?.vaultItem?.toSummary<SummaryObject>()

    override suspend fun acceptItemGroupInvite(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject,
        handleConflict: Boolean
    ) {
        runOnConflictItemGroupRevision(
            block = { sendAcceptItemGroupRequest(summaryObject, itemGroup) },
            onConflict = {
                if (!handleConflict) throw AcceptItemGroupException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) throw AcceptItemGroupException()
                else acceptItemGroupInvite(itemGroupUpdated, summaryObject, false)
            }
        )
    }

    override suspend fun acceptUserGroupInvite(
        userGroup: UserGroup,
        handleConflict: Boolean
    ) {
        runOnConflictUserGroupRevision(
            block = { sendAcceptUserGroupRequest(userGroup) },
            onConflict = {
                if (!handleConflict) throw AcceptUserGroupException()
                val userGroupUpdated = sharingItemGroupDataProvider.getUpdatedUserGroup(userGroup)
                if (userGroupUpdated == null) throw AcceptUserGroupException()
                else acceptUserGroupInvite(userGroupUpdated, false)
            }
        )
    }

    override suspend fun declineItemGroupInvite(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject,
        handleConflict: Boolean,
        loggerAction: String?
    ) {
        runOnConflictItemGroupRevision(
            block = { sendDeclineItemGroupRequest(summaryObject, itemGroup) },
            onConflict = {
                if (!handleConflict) throw DeclineItemGroupException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) throw DeclineItemGroupException()
                else declineItemGroupInvite(itemGroupUpdated, summaryObject, false, loggerAction)
            }
        )
    }

    override suspend fun declineUserGroupInvite(userGroup: UserGroup, handleConflict: Boolean) {
        runOnConflictItemGroupRevision(
            block = { sendDeclineUserGroupRequest(userGroup) },
            onConflict = {
                if (!handleConflict) throw DeclineUserGroupException()
                val userGroupUpdated = sharingItemGroupDataProvider.getUpdatedUserGroup(userGroup)
                if (userGroupUpdated == null) throw DeclineUserGroupException()
                else declineUserGroupInvite(userGroup, false)
            }
        )
    }

    private suspend fun sendDeclineUserGroupRequest(userGroup: UserGroup): Response<SharingServerResponse> {
        val authorization = session?.authorization ?: throw DeclineUserGroupException()
        return refuseUserGroupService.execute(
            authorization, RefuseUserGroupService.Request(
                groupId = RefuseUserGroupService.Request.GroupId(userGroup.groupId),
                provisioningMethod = ProvisioningMethod.USER,
                revision = userGroup.revision
            )
        )
    }

    private suspend fun sendAcceptItemGroupRequest(
        summaryObject: SummaryObject,
        itemGroup: ItemGroup
    ): Response<SharingServerResponse> {
        val sharedVaultItemLite = summaryObject.toSharedVaultItemLite()
        val authorization = session?.authorization ?: throw AcceptItemGroupException()
        val request = acceptItemGroupRequestBuilder.buildForUser(
            itemGroup,
            sharedVaultItemLite.toItemForEmailing()
        )
        return acceptItemGroupService.execute(authorization, request)
    }

    private suspend fun sendAcceptUserGroupRequest(
        userGroup: UserGroup
    ): Response<SharingServerResponse> {
        val authorization = session?.authorization ?: throw AcceptUserGroupException()
        val request = acceptUserGroupRequestBuilder.build(userGroup)
        return acceptUserGroupService.execute(authorization, request)
    }

    private suspend fun sendDeclineItemGroupRequest(
        summaryObject: SummaryObject,
        itemGroup: ItemGroup
    ): Response<SharingServerResponse> {
        val sharedVaultItemLite = summaryObject.toSharedVaultItemLite()
        val authorization = session?.authorization ?: throw DeclineItemGroupException()

        return refuseItemGroupService.execute(
            authorization,
            RefuseItemGroupService.Request(
                groupId = RefuseItemGroupService.Request.GroupId(itemGroup.groupId),
                itemsForEmailing = listOf(sharedVaultItemLite.toItemForEmailing()),
                revision = itemGroup.revision
            )
        )
    }

    override suspend fun resendInvite(itemGroup: ItemGroup, memberLogin: String) {
        val authorization = session?.authorization ?: throw ResendItemInvitesException()
        val userDownloads =
            itemGroup.users ?: throw ResendItemInvitesException()
        val inviteResends = userDownloads.mapNotNull {
            if (it.alias == memberLogin || it.userId == memberLogin) {
                UserInviteResend(
                    alias = UserInviteResend.Alias(it.alias),
                    userId = UserInviteResend.UserId(it.userId)
                )
            } else null
        }

        resendItemGroupInvitesService.execute(
            authorization,
            ResendItemGroupInvitesService.Request(
                groupId = ResendItemGroupInvitesService.Request.GroupId(itemGroup.groupId),
                revision = itemGroup.revision,
                users = inviteResends
            )
        ) 
    }

    override suspend fun cancelInvitationUsersAndUserGroups(
        itemGroup: ItemGroup,
        userIds: List<String>?,
        userGroupIds: List<String>?,
        handleConflict: Boolean
    ) {
        val authorization = session?.authorization ?: throw CancelInvitationException()

        runOnConflictItemGroupRevision(
            block = {
                revokeItemGroupMembersService.execute(
                    authorization,
                    RevokeItemGroupMembersService.Request(
                        groupId = RevokeItemGroupMembersService.Request.GroupId(itemGroup.groupId),
                        revision = itemGroup.revision,
                        users = userIds?.map {
                            RevokeItemGroupMembersService.Request.User(it)
                        },
                        groups = userGroupIds?.map {
                            RevokeItemGroupMembersService.Request.Group(it)
                        }
                    )
                )
            },
            onConflict = {
                if (!handleConflict) throw CancelInvitationException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) throw CancelInvitationException()
                else cancelInvitationUsersAndUserGroups(
                    itemGroupUpdated,
                    userIds,
                    userGroupIds,
                    false
                )
            }
        )
    }

    override suspend fun updateItemGroupMember(
        itemGroup: ItemGroup,
        newPermission: Permission,
        userId: String,
        handleConflict: Boolean
    ) {
        val authorization = session?.authorization ?: throw UpdateItemGroupException()
        runOnConflictItemGroupRevision(
            block = {
                updateItemGroupMembersService.execute(
                    authorization,
                    UpdateItemGroupMembersService.Request(
                        groupId = UpdateItemGroupMembersService.Request.GroupId(itemGroup.groupId),
                        revision = itemGroup.revision,
                        users = listOf(
                            UserUpdate(
                                userId = UserUpdate.UserId(userId),
                                permission = newPermission
                            )
                        )
                    )
                )
            },
            onConflict = {
                if (!handleConflict) throw UpdateItemGroupException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) throw UpdateItemGroupException()
                else updateItemGroupMember(itemGroupUpdated, newPermission, userId, false)
            }
        )
    }

    override suspend fun updateItemGroupMemberUserGroup(
        itemGroup: ItemGroup,
        newPermission: Permission,
        userGroupId: String,
        handleConflict: Boolean
    ) {
        val authorization = session?.authorization ?: throw UpdateItemGroupException()
        runOnConflictItemGroupRevision(
            block = {
                updateItemGroupMembersService.execute(
                    authorization,
                    UpdateItemGroupMembersService.Request(
                        groupId = UpdateItemGroupMembersService.Request.GroupId(itemGroup.groupId),
                        revision = itemGroup.revision,
                        groups = listOf(
                            UserGroupUpdate(
                                groupId = UserGroupUpdate.GroupId(userGroupId),
                                permission = newPermission
                            )
                        )
                    )
                )
            },
            onConflict = {
                if (!handleConflict) throw UpdateItemGroupException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) throw UpdateItemGroupException()
                else updateItemGroupMember(itemGroupUpdated, newPermission, userGroupId, false)
            }
        )
    }

    private suspend fun runOnConflictItemGroupRevision(
        block: suspend () -> Response<SharingServerResponse>,
        onConflict: suspend () -> Unit
    ) {
        runCatching {
            sharingItemUpdater.handleServerResponse(block())
        }.onFailure {
            if (it is InvalidItemGroupRevisionException) {
                onConflict()
            } else {
                throw it
            }
        }
    }

    private suspend fun runOnConflictUserGroupRevision(
        block: suspend () -> Response<SharingServerResponse>,
        onConflict: suspend () -> Unit
    ) {
        runCatching {
            sharingItemUpdater.handleServerResponse(block())
        }.onFailure {
            if (it is InvalidUserGroupRevisionException) {
                onConflict()
            } else {
                throw it
            }
        }
    }

    class AcceptItemGroupException : Exception()
    class AcceptUserGroupException : Exception()
    class DeclineItemGroupException : Exception()
    class DeclineUserGroupException : Exception()
    class ResendItemInvitesException : Exception()
    class CancelInvitationException : Exception()
    class UpdateItemGroupException : Exception()
}
