package com.dashlane.ui.screens.sharing

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleServerResponse
import com.dashlane.core.sharing.toItemForEmailing
import com.dashlane.core.sharing.toSharedVaultItemLite
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.Username
import com.dashlane.sharing.exception.SharingAlreadyAccessException
import com.dashlane.sharing.exception.SharingException
import com.dashlane.sharing.internal.builder.request.SharingRequestRepository
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.service.FindUsersDataProvider
import com.dashlane.sharing.service.ObjectToJson
import com.dashlane.sharing.service.SharingServiceNew
import com.dashlane.storage.DataStorageProvider
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.util.JsonSerialization
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.model.copyWithLock
import com.dashlane.vault.model.copyWithSpaceId
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.SyncObjectTypeUtils.SHAREABLE
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NewSharePeopleDataProvider @Inject constructor(
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
    private val dataStorageProvider: DataStorageProvider,
    private val sharingRequestRepository: SharingRequestRepository,
    private val sharingServiceNew: SharingServiceNew,
    private val jsonSerialization: JsonSerialization,
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val sharingItemUpdater: SharingItemUpdater,
    private val createItemGroupService: CreateItemGroupService,
    private val inviteItemGroupMembersService: InviteItemGroupMembersService,
    private val findUsersDataProvider: FindUsersDataProvider
) {
    private val sharingDao: SharingDao get() = dataStorageProvider.sharingDao

    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    private val uki: String
        get() = session!!.uki

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
        SharingException::class
    )
    suspend fun share(
        accountIds: Array<String>,
        secureNoteIds: Array<String>,
        contacts: List<SharingContact>,
        permission: Permission
    ) {
        val session = session ?: return
        withContext(defaultDispatcher) {
            if (accountIds.isEmpty() && secureNoteIds.isEmpty() && contacts.isEmpty()) return@withContext
            val users =
                contacts.filterIsInstance<SharingContact.SharingContactUser>().map {
                    Username.ofEmail(it.name).email
                }

            checkMyAccess(users)
            val userGroupsToInvite =
                contacts.filterIsInstance<SharingContact.SharingContactUserGroup>().map { it.id }
                    .let { groupIds ->
                        sharingDao.loadAllUserGroup().filter { it.groupId in groupIds }
                    }.map {
                        GroupToInvite(it, permission)
                    }
            val usersToInvite = if (users.isNotEmpty()) {
                findUsersDataProvider.findUsers(session, users.toSet()).map {
                    val alias = it.key
                    val user = it.value
                    UserToInvite(
                        userId = user.login ?: "", 
                        alias = alias,
                        publicKey = user.publicKey,
                        permission = permission
                    )
                }
                val requestedUsers =
                    sharingServiceNew.findUsers(login, uki, ObjectToJson(users, jsonSerialization))
                requestedUsers.content?.map {
                    val alias = it.key
                    val user = it.value
                    UserToInvite(
                        userId = user.login ?: "", 
                        alias = alias,
                        publicKey = user.publicKey,
                        permission = permission
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }

            if (usersToInvite.isEmpty() && userGroupsToInvite.isEmpty())
                throw SharingException("No valid user found to send the request")

            val itemGroupsActual = sharingDao.loadAllItemGroup()
            val itemGroupsToUpdate = mutableListOf<ItemGroup>()
            val items = mutableListOf<Pair<ItemToShare, ItemForEmailing>>()

            fillItemsToUploadGroupToUpdate(
                itemGroupsToUpdate, items, itemGroupsActual, accountIds,
                SyncObjectType.AUTHENTIFIANT
            )
            fillItemsToUploadGroupToUpdate(
                itemGroupsToUpdate, items, itemGroupsActual, secureNoteIds,
                SyncObjectType.SECURE_NOTE
            )
            sendRequests(items, usersToInvite, userGroupsToInvite, itemGroupsToUpdate)
        }
    }

    private suspend fun sendRequests(
        items: List<Pair<ItemToShare, ItemForEmailing>>,
        usersToInvite: List<UserToInvite>,
        userGroupsToInvite: List<GroupToInvite>,
        itemGroupsToUpdate: List<ItemGroup>
    ) {
        val throwables = mutableListOf<Throwable>()
        val authorization = session?.authorization ?: return
        items.forEach { (itemToShare, email) ->
            runCatching {
                val request = sharingRequestRepository.createItemGroupRequest(
                    users = usersToInvite,
                    groups = userGroupsToInvite,
                    item = itemToShare,
                    itemForEmailing = email
                )
                createItemGroupService.execute(authorization, request).also {
                    sharingItemUpdater.handleServerResponse(it)
                }
            }.onFailure {
                throwables.add(it)
            }
        }

        val myUserGroups: List<UserGroup> = getMyUserGroups(login)

        itemGroupsToUpdate.forEach { itemGroup ->
            runCatching {
                val request = sharingRequestRepository.createInviteItemGroupMembersRequest(
                    itemGroup,
                    getItemsForEmailing(items, itemGroup),
                    usersToInvite,
                    userGroupsToInvite,
                    myUserGroups
                )
                inviteItemGroupMembersService.execute(authorization, request)
                    .also {
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
        contacts: List<String>
    ) = if (login in contacts) throw SharingAlreadyAccessException() else Unit

    private fun getMyUserGroups(login: String): List<UserGroup> =
        sharingDao.loadUserGroupsAcceptedOrPending(login) 

    private fun getItemsForEmailing(
        items: List<Pair<ItemToShare, ItemForEmailing>>,
        itemGroup: ItemGroup
    ): List<ItemForEmailing> {
        return itemGroup.items?.map {
            items.mapNotNull { (itemToShare, email) ->
                if (itemToShare.itemId == it.itemId) email
                else null
            }
        }?.flatten() ?: emptyList()
    }

    private fun fillItemsToUploadGroupToUpdate(
        itemGroupsToUpdate: MutableList<ItemGroup>,
        items: MutableList<Pair<ItemToShare, ItemForEmailing>>,
        itemGroupsActual: List<ItemGroup>,
        itemsUid: Array<String>,
        dataType: SyncObjectType
    ) {
        itemsUid.forEach { itemUid ->
            val itemGroup: ItemGroup? = getItemGroupFor(itemGroupsActual, itemUid)
            if (itemGroup == null) {
                val s = getDataIdentifier(dataType, itemUid) ?: return@forEach
                val dataIdentifierWrapper = preparedDataIdentifierForSharing(s)
                val itemContent: String = getItemContent(dataIdentifierWrapper) ?: return@forEach
                val itemToShare = ItemToShare(itemUid, itemContent)
                val liteItem = dataIdentifierWrapper.vaultItem.toSummary<SummaryObject>()
                    .toSharedVaultItemLite()
                items.add(itemToShare to liteItem.toItemForEmailing())
            } else {
                itemGroupsToUpdate.add(itemGroup)
            }
        }
    }

    private fun getItemContent(item: DataIdentifierExtraDataWrapper<out SyncObject>): String? =
        xmlConverter.toXml(item)

    private fun preparedDataIdentifierForSharing(
        dataIdentifier: DataIdentifierExtraDataWrapper<out SyncObject>
    ): DataIdentifierExtraDataWrapper<out SyncObject> {
        return DataIdentifierExtraDataWrapper(
            dataIdentifier.vaultItem.let {
                it.copyWithSpaceId(null)
                it.copyWithLock(false)
            },
            dataIdentifier.extraData,
            dataIdentifier.backupDate
        )
    }

    private fun getDataIdentifier(
        dataType: SyncObjectType,
        itemUid: String
    ): DataIdentifierExtraDataWrapper<out SyncObject>? = if (dataType in SHAREABLE) {
        sharingDao.getItemWithExtraData(itemUid, dataType)
    } else null

    private fun getItemGroupFor(
        itemGroupsActual: List<ItemGroup>,
        itemUid: String
    ): ItemGroup? = itemGroupsActual.find { itemGroup ->
        itemGroup.items?.find { it.itemId == itemUid } != null
    }

    private suspend fun loadUsers(
        username: Username
    ): List<SharingContact> = withContext(ioDispatcher) {
        val itemGroups = sharingDao.loadAllItemGroup()
        itemGroups.mapNotNull { itemGroup ->
            itemGroup.users?.map { SharingContact.SharingContactUser(it.alias) }
        }.flatten()
            .toSet()
            .filter { !it.name.equals(username.email, ignoreCase = true) }
    }

    private suspend fun loadUserGroups(): List<SharingContact> =
        withContext(ioDispatcher) {
            val userGroups = sharingDao.loadAllUserGroup()
            userGroups.map {
                SharingContact.SharingContactUserGroup(it.groupId, it.name)
            }
        }
}
