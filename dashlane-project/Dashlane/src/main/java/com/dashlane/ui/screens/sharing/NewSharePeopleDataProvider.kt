package com.dashlane.ui.screens.sharing

import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleServerResponse
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateMultipleItemGroupsService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteMultipleItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.sharing.SharingDao
import com.dashlane.sharing.SharingDataProvider
import com.dashlane.sharing.exception.SharingAlreadyAccessException
import com.dashlane.sharing.exception.SharingException
import com.dashlane.sharing.internal.builder.request.SharingRequestRepository
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.service.FindUsersDataProvider
import com.dashlane.sharing.toItemForEmailing
import com.dashlane.sharing.toSharedVaultItemLite
import com.dashlane.sharing.util.AuditLogHelper
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.ui.screens.sharing.SharingContact.SharingContactUser
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.SyncObjectTypeUtils.SHAREABLE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.Collator
import javax.inject.Inject

class NewSharePeopleDataProvider @Inject constructor(
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
    private val sharingDao: SharingDao,
    private val sharingRequestRepository: SharingRequestRepository,
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val sharingDataProvider: SharingDataProvider,
    private val sharingItemUpdater: SharingItemUpdater,
    private val createMultipleItemGroupsService: CreateMultipleItemGroupsService,
    private val inviteMultipleItemGroupMembersService: InviteMultipleItemGroupMembersService,
    private val findUsersDataProvider: FindUsersDataProvider,
    private val auditLogHelper: AuditLogHelper,
) {
    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    suspend fun load(): List<SharingContact> {
        val session = session ?: return emptyList()

        return coroutineScope {
            val users = async { loadUsers(session.username) }
            val userGroups = async { loadUserGroups() }
            users.await() + userGroups.await()
        }
    }

    @Throws(
        SharingAlreadyAccessException::class,
        SharingException::class,
    )
    @Suppress("LongMethod")
    suspend fun share(
        accountIds: Array<String>,
        secureNoteIds: Array<String>,
        secrets: Array<String>,
        contacts: List<SharingContact>,
        permission: Permission,
    ) {
        val session = session ?: return
        withContext(defaultDispatcher) {
            if (accountIds.isEmpty() && secureNoteIds.isEmpty() && contacts.isEmpty()) return@withContext
            val users =
                contacts.filterIsInstance<SharingContactUser>().map {
                    Username.ofEmail(it.name).email
                }.toSet().toList()

            checkMyAccess(users)
            val userGroupsToInvite =
                contacts.filterIsInstance<SharingContact.SharingContactUserGroup>().map { it.id }
                    .let { groupIds ->
                        sharingDao.loadAllUserGroup().filter { it.groupId in groupIds }
                    }.map {
                        GroupToInvite(it, permission)
                    }
            val usersToInvite = if (users.isNotEmpty()) {
                findUsersDataProvider.findUsers(session.authorization, users).map {
                    val alias = it.email
                    val user = it.login
                    UserToInvite(
                        
                        userId = user ?: "",
                        alias = alias,
                        publicKey = it.publicKey,
                        permission = permission,
                    )
                }
            } else {
                emptyList()
            }

            if (usersToInvite.isEmpty() && userGroupsToInvite.isEmpty()) {
                throw SharingException("No valid user found to send the request")
            }

            val itemGroupsActual = sharingDao.loadAllItemGroup()
            val itemGroupsToUpdate = mutableListOf<Pair<ItemGroup, List<ItemForEmailing>>>()
            val items = mutableListOf<Pair<ItemToShare, ItemForEmailing>>()

            fillItemsToUploadGroupToUpdate(
                itemGroupsToUpdate,
                items,
                itemGroupsActual,
                accountIds,
                SyncObjectType.AUTHENTIFIANT,
            )
            fillItemsToUploadGroupToUpdate(
                itemGroupsToUpdate,
                items,
                itemGroupsActual,
                secureNoteIds,
                SyncObjectType.SECURE_NOTE,
            )
            fillItemsToUploadGroupToUpdate(
                itemGroupsToUpdate = itemGroupsToUpdate,
                items = items,
                itemGroupsActual = itemGroupsActual,
                itemsUid = secrets,
                dataType = SyncObjectType.SECRET,
            )
            sendRequests(items, usersToInvite, userGroupsToInvite, itemGroupsToUpdate)
        }
    }

    private suspend fun sendRequests(
        items: List<Pair<ItemToShare, ItemForEmailing>>,
        usersToInvite: List<UserToInvite>,
        userGroupsToInvite: List<GroupToInvite>,
        itemGroupsToUpdate: List<Pair<ItemGroup, List<ItemForEmailing>>>,
    ) {
        val throwables = mutableListOf<Throwable>()
        val authorization = session?.authorization ?: return

        if (items.isNotEmpty()) {
            runCatching {
                val itemIds = items.map { it.first.itemId }
                val auditLogs = auditLogHelper.buildAuditLogDetails(authorization, itemIds)
                val request = sharingRequestRepository.createMultipleItemGroupsRequest(
                    username = login,
                    users = usersToInvite,
                    groups = userGroupsToInvite,
                    items = items.map { (itemToShare, itemForEmailing) ->
                        val auditLog = auditLogs.firstOrNull { (itemId, _) -> itemId == itemToShare.itemId }?.second
                        Triple(
                            itemToShare,
                            itemForEmailing,
                            auditLog,
                        )
                    },
                )

                createMultipleItemGroupsService.execute(authorization, request).also {
                    sharingItemUpdater.handleServerResponse(it)
                }
            }.onFailure {
                throwables.add(it)
            }
        }

        val myUserGroups: List<UserGroup> = getMyUserGroups(login)

        if (itemGroupsToUpdate.isNotEmpty()) {
            runCatching {
                val itemIds = itemGroupsToUpdate.mapNotNull {
                    it.first.items
                        ?.singleOrNull()
                        ?.itemId
                }
                val auditLogs = auditLogHelper.buildAuditLogDetails(authorization, itemIds)
                val request = sharingRequestRepository.createMultipleInviteItemGroupMembersRequest(
                    username = login,
                    itemGroups = itemGroupsToUpdate.map { (itemGroup, itemForEmailing) ->
                        val auditLog = auditLogHelper.getAuditLogDetailForItemGroup(itemGroup, auditLogs)
                        Triple(
                            itemGroup,
                            itemForEmailing,
                            auditLog,
                        )
                    },
                    usersToInvite,
                    userGroupsToInvite,
                    myUserGroups,
                )

                inviteMultipleItemGroupMembersService.execute(authorization, request).also {
                    sharingItemUpdater.handleServerResponse(it)
                }
            }.onFailure {
                throwables.add(it)
            }
        }

        if (throwables.isNotEmpty()) throw throwables.first()
    }

    @Throws(SharingAlreadyAccessException::class)
    private fun checkMyAccess(
        contacts: List<String>,
    ) = if (login in contacts) throw SharingAlreadyAccessException() else Unit

    private suspend fun getMyUserGroups(login: String): List<UserGroup> =
        sharingDao.loadUserGroupsAcceptedOrPending(login) 

    private suspend fun fillItemsToUploadGroupToUpdate(
        itemGroupsToUpdate: MutableList<Pair<ItemGroup, List<ItemForEmailing>>>,
        items: MutableList<Pair<ItemToShare, ItemForEmailing>>,
        itemGroupsActual: List<ItemGroup>,
        itemsUid: Array<String>,
        dataType: SyncObjectType,
    ) {
        itemsUid.forEach { itemUid ->
            val itemGroup: ItemGroup? = getItemGroupFor(itemGroupsActual, itemUid)
            val dataIdentifierWrapper = getDataIdentifier(dataType, itemUid) ?: return@forEach
            val liteItem = dataIdentifierWrapper.vaultItem.toSummary<SummaryObject>().toSharedVaultItemLite()
            if (itemGroup == null) {
                val itemContent: String = getItemContent(dataIdentifierWrapper) ?: return@forEach
                val itemType = dataType.toItemType() ?: return@forEach
                val itemToShare = ItemToShare(itemUid, itemContent, itemType)
                items.add(itemToShare to liteItem.toItemForEmailing())
            } else {
                itemGroupsToUpdate.add(itemGroup to listOf(liteItem.toItemForEmailing()))
            }
        }
    }

    private fun getItemContent(item: DataIdentifierExtraDataWrapper<out SyncObject>): String? =
        xmlConverter.toXml(item)

    private suspend fun getDataIdentifier(
        dataType: SyncObjectType,
        itemUid: String,
    ): DataIdentifierExtraDataWrapper<out SyncObject>? = if (dataType in SHAREABLE) {
        sharingDao.getItemWithExtraData(itemUid, dataType)
    } else {
        null
    }

    private fun getItemGroupFor(
        itemGroupsActual: List<ItemGroup>,
        itemUid: String,
    ): ItemGroup? = itemGroupsActual.find { itemGroup ->
        itemGroup.items?.find { it.itemId == itemUid } != null
    }

    private suspend fun loadUsers(username: Username): List<SharingContact> =
        withContext(ioDispatcher) {
            val itemGroupsUsers = sharingDao.loadAllItemGroup().flatMap { itemGroup ->
                itemGroup.users?.map { it.alias } ?: emptyList()
            }
            val teamUsers = sharingDataProvider.getTeamLogins()
            val allUsers = itemGroupsUsers + teamUsers
            allUsers.toSet().filter {
                !it.equals(username.email, ignoreCase = true)
            }.sortedWith(Collator.getInstance()).map { SharingContactUser(it) }
        }

    private suspend fun loadUserGroups(): List<SharingContact> =
        withContext(ioDispatcher) {
            val userGroups = sharingDao.loadAllUserGroup()
            userGroups.map {
                SharingContact.SharingContactUserGroup(it.groupId, it.name)
            }
        }

    private fun SyncObjectType.toItemType(): ItemUpload.ItemType? =
        when (this) {
            SyncObjectType.AUTHENTIFIANT -> ItemUpload.ItemType.AUTHENTIFIANT
            SyncObjectType.SECURE_NOTE -> ItemUpload.ItemType.SECURENOTE
            SyncObjectType.SECRET -> ItemUpload.ItemType.SECRET
            else -> null
        }
}