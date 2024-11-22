package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleCollectionSharingResult
import com.dashlane.core.sharing.handleServerResponse
import com.dashlane.core.sharing.toItemForEmailing
import com.dashlane.core.sharing.toSharedVaultItemLite
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.session.authorization
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptUserGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CollectionServiceResponse
import com.dashlane.server.api.endpoints.sharinguserdevice.DeleteCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.GetTeamLoginsService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.ProvisioningMethod
import com.dashlane.server.api.endpoints.sharinguserdevice.RefuseCollectionService
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
import com.dashlane.server.api.endpoints.sharinguserdevice.exceptions.InvalidCollectionRevisionException
import com.dashlane.server.api.endpoints.sharinguserdevice.exceptions.InvalidItemGroupRevisionException
import com.dashlane.server.api.endpoints.sharinguserdevice.exceptions.InvalidUserGroupRevisionException
import com.dashlane.server.api.pattern.UserIdFormat
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.internal.builder.request.AcceptCollectionRequestBuilder
import com.dashlane.sharing.internal.builder.request.AcceptItemGroupRequestForUserBuilder
import com.dashlane.sharing.internal.builder.request.AcceptUserGroupRequestBuilder
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.util.AuditLogHelper
import com.dashlane.sharing.util.intersectUserGroupCollectionDownload
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.util.tryOrNull
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummaryOrNull
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("LargeClass")
class SharingDataProviderImpl @Inject constructor(
    private val credentialDataQuery: CredentialDataQuery,
    private val genericDataQuery: GenericDataQuery,
    private val sharingDao: SharingDao,
    private val sharingXmlConverter: DataIdentifierSharingXmlConverter,
    private val sessionManager: SessionManager,
    private val sharingItemUpdater: SharingItemUpdater,
    private val acceptItemGroupRequestBuilder: AcceptItemGroupRequestForUserBuilder,
    private val acceptUserGroupRequestBuilder: AcceptUserGroupRequestBuilder,
    private val acceptCollectionRequestBuilder: AcceptCollectionRequestBuilder,
    private val sharingItemGroupDataProvider: SharingDataUpdateProvider,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher,
    private val refuseItemGroupService: RefuseItemGroupService,
    private val refuseUserGroupService: RefuseUserGroupService,
    private val refuseCollectionService: RefuseCollectionService,
    private val acceptItemGroupService: AcceptItemGroupService,
    private val acceptUserGroupService: AcceptUserGroupService,
    private val acceptCollectionService: AcceptCollectionService,
    private val deleteCollectionService: DeleteCollectionService,
    private val updateItemGroupMembersService: UpdateItemGroupMembersService,
    private val revokeItemGroupMembersService: RevokeItemGroupMembersService,
    private val resendItemGroupInvitesService: ResendItemGroupInvitesService,
    private val teamLoginsService: GetTeamLoginsService,
    private val auditLogHelper: AuditLogHelper
) : SharingDataProvider {
    private val session: Session?
        get() = sessionManager.session

    override val updatedItemFlow: SharedFlow<Unit>
        get() = sharingItemUpdater.updatedItemFlow

    override suspend fun getItemGroups() =
        withContext(ioCoroutineDispatcher) { sharingDao.loadAllItemGroup() }

    override suspend fun getUserGroups() =
        withContext(ioCoroutineDispatcher) { sharingDao.loadAllUserGroup() }

    override suspend fun getUserGroupsAccepted(login: String) = withContext(ioCoroutineDispatcher) {
        sharingDao.loadUserGroupsAccepted(login) ?: emptyList()
    }

    override suspend fun getCollections() =
        withContext(ioCoroutineDispatcher) { sharingDao.loadAllCollection() }

    override suspend fun getCollections(itemId: String) =
        withContext(ioCoroutineDispatcher) {
            val foundItemGroup = getItemGroups().firstOrNull {
                val collections = it.collections
                !collections.isNullOrEmpty() &&
                    it.items?.any { sharedItem -> sharedItem.itemId == itemId } == true
            } ?: return@withContext emptyList()
            return@withContext getAcceptedCollections(false).filter {
                foundItemGroup.collections?.any { groupCollection ->
                    groupCollection.uuid == it.uuid
                } == true
            }
        }

    override suspend fun getAcceptedCollections(needsAdminRights: Boolean) =
        withContext(ioCoroutineDispatcher) {
            val userId = session?.userId ?: return@withContext emptyList()
            return@withContext getAcceptedCollections(userId, needsAdminRights = needsAdminRights)
        }

    override suspend fun getAcceptedCollections(userId: String, needsAdminRights: Boolean) =
        withContext(ioCoroutineDispatcher) {
            val collections = sharingDao.loadAllCollection()
            val myCollectionUserGroups =
                sharingDao.loadUserGroupsAccepted(userId)?.let { acceptedUserGroups ->
                    collections.mapNotNull { it.userGroups }.flatten()
                        .filter {
                            if (needsAdminRights) it.isAccepted && it.isAdmin else it.isAccepted
                        }
                        .intersectUserGroupCollectionDownload(acceptedUserGroups)
                } ?: emptyList()
            collections.filter {
                it.userGroups?.any { group -> myCollectionUserGroups.contains(group) } == true ||
                    it.users?.any { user ->
                        if (needsAdminRights) {
                            user.login == userId && user.isAccepted && user.isAdmin
                        } else {
                            user.login == userId && user.isAccepted
                        }
                    } == true
            }
        }

    override suspend fun getAcceptedCollectionsForGroup(userGroupId: String) =
        withContext(ioCoroutineDispatcher) {
            sharingDao.loadAllCollection().filter { collection ->
                collection.userGroups?.any {
                    it.isAccepted && it.uuid == userGroupId
                } == true
            }
        }

    override suspend fun getAcceptedCollectionsItems(uuid: String) =
        withContext(ioCoroutineDispatcher) {
            val collections =
                getAcceptedCollections(needsAdminRights = false).filter { it.uuid == uuid }
            if (collections.isEmpty()) {
                return@withContext emptyList()
            }
            val items = mutableListOf<SummaryObject>()
            val credentials = credentialDataQuery.queryAll()
            val secureNotes =
                genericDataQuery.queryAll(GenericFilter(dataTypeFilter = SpecificDataTypeFilter(SyncObjectType.SECURE_NOTE)))

            sharingDao.loadAllItemGroup().forEach { itemGroup ->
                itemGroup.collections?.forEach { collection ->
                    if (collection.uuid == uuid) {
                        itemGroup.items?.map { it.itemId }?.let { itemIds ->
                            items.addAll(credentials.filter { itemIds.contains(it.id) })
                            items.addAll(secureNotes.filter { itemIds.contains(it.id) })
                        }
                    }
                }
            }
            return@withContext items
        }

    override fun isDeleteAllowed(collection: Collection): Boolean {
        val hasNoUserGroups = collection.userGroups.isNullOrEmpty()
        val hasNoOtherUser = collection.users?.filter { it.login != session?.userId }?.size == 0
        val isSharedToSelf = collection.users?.filter { it.login == session?.userId }?.size == 1
        return hasNoUserGroups && hasNoOtherUser && isSharedToSelf
    }

    override suspend fun deleteCollection(
        collection: Collection,
        handleConflict: Boolean
    ) {
        runOnConflictCollectionRevision(
            block = { sendDeleteCollectionRequest(collection = collection) },
            onConflict = {
                if (!handleConflict) throw DeleteCollectionException()
                val collectionUpdated =
                    sharingItemGroupDataProvider.getUpdatedCollection(collection)
                if (collectionUpdated == null) {
                    throw DeleteCollectionException()
                } else {
                    deleteCollection(collectionUpdated, false)
                }
            }
        )
    }

    private suspend fun sendDeleteCollectionRequest(collection: Collection): Response<CollectionServiceResponse> {
        val authorization = session?.authorization ?: throw DeclineCollectionException()
        return deleteCollectionService.execute(
            authorization,
            DeleteCollectionService.Request(
                collectionId = UuidFormat(collection.uuid),
                revision = collection.revision
            )
        )
    }

    override suspend fun getTeamLogins() = session?.authorization?.let { authorization ->
        tryOrNull { teamLoginsService.execute(authorization).data.teamLogins }
    } ?: emptyList()

    override suspend fun isAdmin(collection: Collection): Boolean =
        withContext(ioCoroutineDispatcher) {
            val userId = session?.userId
            val userAdmin = collection.users?.any {
                it.login == userId && it.isAccepted && it.isAdmin
            } ?: false
            val myUserGroups = userId?.let { sharingDao.loadUserGroupsAccepted(it) } ?: emptyList()
            return@withContext userAdmin || collection.userGroups
                ?.filter { it.isAccepted && it.isAdmin }
                ?.intersectUserGroupCollectionDownload(myUserGroups)
                ?.isNotEmpty() == true
        }

    override fun getSummaryObject(itemId: String) =
        sharingXmlConverter.fromXml(identifier = itemId, xml = sharingDao.loadItemContentExtraDataLegacy(itemId))
            ?.vaultItem?.toSummaryOrNull<SummaryObject>()

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
                if (itemGroupUpdated == null) {
                    throw AcceptItemGroupException()
                } else {
                    acceptItemGroupInvite(itemGroupUpdated, summaryObject, false)
                }
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
                if (userGroupUpdated == null) {
                    throw AcceptUserGroupException()
                } else {
                    acceptUserGroupInvite(userGroupUpdated, false)
                }
            }
        )
    }

    override suspend fun acceptCollectionInvite(collection: Collection, handleConflict: Boolean) {
        runOnConflictCollectionRevision(
            block = { sendAcceptCollectionRequest(collection) },
            onConflict = {
                if (!handleConflict) throw AcceptCollectionException()
                val collectionUpdated =
                    sharingItemGroupDataProvider.getUpdatedCollection(collection)
                if (collectionUpdated == null) {
                    throw AcceptCollectionException()
                } else {
                    acceptCollectionInvite(collectionUpdated, false)
                }
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
                if (itemGroupUpdated == null) {
                    throw DeclineItemGroupException()
                } else {
                    declineItemGroupInvite(itemGroupUpdated, summaryObject, false, loggerAction)
                }
            }
        )
    }

    override suspend fun declineUserGroupInvite(userGroup: UserGroup, handleConflict: Boolean) {
        runOnConflictItemGroupRevision(
            block = { sendDeclineUserGroupRequest(userGroup) },
            onConflict = {
                if (!handleConflict) throw DeclineUserGroupException()
                val userGroupUpdated = sharingItemGroupDataProvider.getUpdatedUserGroup(userGroup)
                if (userGroupUpdated == null) {
                    throw DeclineUserGroupException()
                } else {
                    declineUserGroupInvite(userGroup, false)
                }
            }
        )
    }

    override suspend fun declineCollectionInvite(collection: Collection, handleConflict: Boolean) {
        runOnConflictCollectionRevision(
            block = { sendDeclineCollectionRequest(collection) },
            onConflict = {
                if (!handleConflict) throw DeclineCollectionException()
                val collectionUpdated =
                    sharingItemGroupDataProvider.getUpdatedCollection(collection)
                if (collectionUpdated == null) {
                    throw DeclineCollectionException()
                } else {
                    declineCollectionInvite(collection, false)
                }
            }
        )
    }

    private suspend fun sendDeclineCollectionRequest(collection: Collection): Response<CollectionServiceResponse> {
        val authorization = session?.authorization ?: throw DeclineCollectionException()
        return refuseCollectionService.execute(
            authorization,
            RefuseCollectionService.Request(
                collectionId = UuidFormat(collection.uuid),
                revision = collection.revision
            )
        )
    }

    private suspend fun sendDeclineUserGroupRequest(userGroup: UserGroup): Response<SharingServerResponse> {
        val authorization = session?.authorization ?: throw DeclineUserGroupException()
        return refuseUserGroupService.execute(
            authorization,
            RefuseUserGroupService.Request(
                groupId = UuidFormat(userGroup.groupId),
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
                groupId = UuidFormat(itemGroup.groupId),
                itemsForEmailing = listOf(sharedVaultItemLite.toItemForEmailing()),
                revision = itemGroup.revision,
                auditLogDetails = auditLogHelper.buildAuditLogDetails(itemGroup, summaryObject)
            )
        )
    }

    private suspend fun sendAcceptCollectionRequest(
        collection: Collection
    ): Response<CollectionServiceResponse> {
        val authorization = session?.authorization ?: throw AcceptCollectionException()
        val request = acceptCollectionRequestBuilder.build(collection)
        return acceptCollectionService.execute(authorization, request)
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
            } else {
                null
            }
        }

        resendItemGroupInvitesService.execute(
            authorization,
            ResendItemGroupInvitesService.Request(
                groupId = UuidFormat(itemGroup.groupId),
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
                        groupId = UuidFormat(itemGroup.groupId),
                        revision = itemGroup.revision,
                        users = userIds?.map {
                            UserIdFormat(it)
                        },
                        groups = userGroupIds?.map {
                            UuidFormat(it)
                        },
                        auditLogDetails = auditLogHelper.buildAuditLogDetails(itemGroup)
                    )
                )
            },
            onConflict = {
                if (!handleConflict) throw CancelInvitationException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) {
                    throw CancelInvitationException()
                } else {
                    cancelInvitationUsersAndUserGroups(
                        itemGroupUpdated,
                        userIds,
                        userGroupIds,
                        false
                    )
                }
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
                        groupId = UuidFormat(itemGroup.groupId),
                        revision = itemGroup.revision,
                        users = listOf(
                            UserUpdate(
                                userId = UserIdFormat(userId),
                                permission = newPermission
                            )
                        )
                    )
                )
            },
            onConflict = {
                if (!handleConflict) throw UpdateItemGroupException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) {
                    throw UpdateItemGroupException()
                } else {
                    updateItemGroupMember(itemGroupUpdated, newPermission, userId, false)
                }
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
                        groupId = UuidFormat(itemGroup.groupId),
                        revision = itemGroup.revision,
                        groups = listOf(
                            UserGroupUpdate(
                                groupId = UuidFormat(userGroupId),
                                permission = newPermission
                            )
                        )
                    )
                )
            },
            onConflict = {
                if (!handleConflict) throw UpdateItemGroupException()
                val itemGroupUpdated = sharingItemGroupDataProvider.getUpdatedItemGroup(itemGroup)
                if (itemGroupUpdated == null) {
                    throw UpdateItemGroupException()
                } else {
                    updateItemGroupMember(itemGroupUpdated, newPermission, userGroupId, false)
                }
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

    private suspend fun runOnConflictCollectionRevision(
        block: suspend () -> Response<CollectionServiceResponse>,
        onConflict: suspend () -> Unit
    ) {
        runCatching {
            val collections = block().data.collections!!
            sharingItemUpdater.handleCollectionSharingResult(collections)
        }.onFailure {
            if (it is InvalidCollectionRevisionException) {
                onConflict()
            } else {
                throw it
            }
        }
    }

    class AcceptItemGroupException : Exception()
    class AcceptUserGroupException : Exception()
    class AcceptCollectionException : Exception()
    class DeclineItemGroupException : Exception()
    class DeclineUserGroupException : Exception()
    class DeclineCollectionException : Exception()
    class ResendItemInvitesException : Exception()
    class CancelInvitationException : Exception()
    class UpdateItemGroupException : Exception()
    class DeleteCollectionException : Exception()
}
