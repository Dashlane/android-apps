package com.dashlane.core.sharing

import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierReplacedEvent
import com.dashlane.exception.NotLoggedInException
import com.dashlane.session.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.DeleteItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getCollectionDownloads
import com.dashlane.sharing.model.getMaxPermission
import com.dashlane.sharing.model.getMaxStatus
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroupMembers
import com.dashlane.sharing.model.getUserPermission
import com.dashlane.sharing.model.hasCollectionsAcceptedOrPending
import com.dashlane.sharing.model.hasUserGroupsAcceptedOrPending
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.sharing.model.isAlone
import com.dashlane.sharing.util.GroupKeyLazy
import com.dashlane.sharing.util.GroupVerification
import com.dashlane.sharing.util.ProposeSignatureVerification
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.xml.domain.objectType
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("LargeClass")
@Singleton
class SharingItemUpdater @Inject constructor(
    private val sharingCryptographyHelper: SharingCryptographyHelper,
    private val sharingDao: SharingDao,
    private val genericDataQuery: GenericDataQuery,
    private val sessionManager: SessionManager,
    private val xmlConverterLazy: Lazy<DataIdentifierSharingXmlConverter>,
    private val sharingDaoMemoryDataAccessProvider: SharingDaoMemoryDataAccessProvider,
    private val autoAcceptItemGroup: AutoAcceptItemGroup,
    private val sharingInvitePublicKeyUser: SharingInvitePublicKeyUser,
    private val deleteItemGroupService: DeleteItemGroupService,
    private val proposeSignatureVerification: ProposeSignatureVerification,
    private val appEvents: AppEvents
) {
    private val mutex = Mutex()

    
    private val _updatedItemFlow = MutableSharedFlow<Unit>()
    val updatedItemFlow = _updatedItemFlow.asSharedFlow()

    @Throws(NotLoggedInException::class)
    suspend fun update(request: SharingItemUpdaterRequest) {
        val session = sessionManager.session
            ?: throw NotLoggedInException("Unable to apply the sharing request")
        if (!sharingDao.databaseOpen) throw NotLoggedInException("The database is not open")

        val groupVerification = GroupVerification(
            session.userId,
            sharingCryptographyHelper,
            proposeSignatureVerification
        )
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val memory: SharingDaoMemoryDataAccess = sharingDaoMemoryDataAccessProvider.create()
                memory.apply {
                    update(memory, request, session, groupVerification)
                    close()
                }
                _updatedItemFlow.emit(Unit)
            }
        }
    }

    private suspend fun update(
        memory: SharingDaoMemoryDataAccess,
        request: SharingItemUpdaterRequest,
        session: Session,
        groupVerification: GroupVerification,
        iteration: Int = 0
    ) {
        if (iteration > 5) {
            
            throw IllegalAccessException("StackOverflow while updating sharing")
        }

        val login = session.userId

        
        saveUserGroupsDownload(memory, groupVerification, request.userGroupUpdates)
        memory.deleteUserGroups(request.userGroupDeletionIds)

        val myUserGroups = loadMyUserGroups(memory, login)

        
        saveCollections(memory, groupVerification, myUserGroups, request.collectionUpdates)
        memory.deleteCollections(request.collectionsDeletionIds)

        val myCollections = loadMyCollections(memory, login, myUserGroups)

        val itemGroups = saveItemGroups(
            memory,
            groupVerification,
            request.itemGroupUpdates,
            myUserGroups,
            myCollections
        )
        sharingDao.deleteItemGroups(memory, request.itemGroupsDeletionIds)

        val itemContents = request.itemContentUpdates
        val itemGroupsFilter = getItemGroupsToUpdate(memory, itemContents, itemGroups)

        val updatedGroups1 = itemGroupsFilter.map {
            it.updateItemsWithinItemGroup(
                memory,
                itemContents,
                myUserGroups,
                myCollections,
                session
            )
        }

        
        
        val updatedGroups2 =
            invitePublicKeyUsers(session, itemGroupsFilter, myUserGroups, myCollections)

        val allUpdatedGroups = updatedGroups1 + (updatedGroups2.first to updatedGroups2.second)
        val itemGroupsUpdated = allUpdatedGroups.map { it.first }.flatten()
        val userGroupsUpdated = allUpdatedGroups.map { it.second }.flatten()
        val collectionsUpdated = updatedGroups2.third
        if (itemGroupsUpdated.isNotEmpty() || userGroupsUpdated.isNotEmpty() || collectionsUpdated.isNotEmpty()) {
            
            val newRequest = SharingItemUpdaterRequest(
                itemGroupUpdates = itemGroupsUpdated,
                userGroupUpdates = userGroupsUpdated,
                collectionUpdates = collectionsUpdated
            )
            update(memory, newRequest, session, groupVerification, iteration + 1)
        }
    }

    private suspend fun ItemGroup.updateItemsWithinItemGroup(
        memory: SharingDaoMemoryDataAccess,
        itemContentsToUpdate: List<ItemContent>,
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>,
        session: Session
    ): Pair<List<ItemGroup>, List<UserGroup>> {
        val login = session.userId
        val userDownload = getUser(login)

        
        if (isNotForMe(myUserGroups, userDownload, myCollections)) {
            
            sharingDao.deleteLocallyItemGroupAndItems(memory, this)
            return emptyPairListGroup()
        }

        
        
        if (isAlone(login) && Permission.ADMIN == getUserPermission(login)) {
            
            
            deleteItemGroupRemotelyAndKeepItems(memory, session)
            return emptyPairListGroup()
        }

        return updateItemsFromContent(
            memory,
            session,
            itemContentsToUpdate,
            userDownload,
            myUserGroups,
            myCollections
        )
    }

    private fun ItemGroup.isNotForMe(
        myUserGroups: List<UserGroup>,
        userDownload: UserDownload?,
        myCollections: List<Collection>
    ) = !hasUserGroupsAcceptedOrPending(myUserGroups) &&
        (userDownload == null || !userDownload.isAcceptedOrPending) &&
        !hasCollectionsAcceptedOrPending(myCollections)

    private suspend fun ItemGroup.updateItemsFromContent(
        memory: SharingDaoMemoryDataAccess,
        session: Session,
        itemContentsToUpdate: List<ItemContent>,
        userDownload: UserDownload?,
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>
    ): Pair<List<ItemGroup>, List<UserGroup>> {
        if (userDownload == null && myUserGroups.isEmpty() && myCollections.isEmpty()) {
            return emptyPairListGroup()
        }
        val login = session.userId
        val groupKey = GroupKeyLazy.newInstance(
            this,
            login,
            myUserGroups,
            myCollections,
            sharingCryptographyHelper
        ) ?: return emptyPairListGroup()

        val myUserGroupsMember = getUserGroupMembers(myUserGroups)
        val myCollectionDownloads = getCollectionDownloads(myCollections)

        val maxStatus = getMaxStatus(userDownload, myUserGroupsMember, myCollectionDownloads)
        val maxPermission =
            getMaxPermission(userDownload, myUserGroupsMember, myCollectionDownloads)

        
        val updatedGroups = autoAcceptIfNecessary(session, myUserGroups)

        items?.forEach { item ->
            val updatedItem =
                itemContentsToUpdate.save(memory, item, groupKey, maxPermission, maxStatus)

            if (!updatedItem && Status.ACCEPTED == maxStatus) {
                
                val itemId = item.itemId

                
                insertOrUpdate(memory, itemId, maxPermission)
            }
        }

        return updatedGroups
    }

    private fun emptyPairListGroup(): Pair<List<ItemGroup>, List<UserGroup>> =
        Pair(listOf(), listOf())

    private suspend fun List<ItemContent>.save(
        memory: SharingDaoMemoryDataAccess,
        item: ItemGroup.Item,
        groupKeyLazy: GroupKeyLazy,
        maxPermission: Permission,
        maxStatus: Status
    ): Boolean {
        val groupKey = groupKeyLazy.get() ?: return false
        var updatedItem = false
        forEach { itemContent ->
            
            val itemIdInDownloaded = itemContent.itemId
            if (itemIdInDownloaded == item.itemId) {
                val success = itemContent.save(memory, item, groupKey, maxPermission, maxStatus)
                updatedItem = updatedItem or success
            }
        }
        return updatedItem
    }

    private suspend fun ItemContent.save(
        memory: SharingDaoMemoryDataAccess,
        item: ItemGroup.Item,
        groupKey: CryptographyKey.Raw32,
        userPermission: Permission,
        userStatus: Status
    ): Boolean {
        val itemId = item.itemId
        val itemKey = sharingCryptographyHelper.decryptItemKey(
            item.itemKey,
            groupKey
        ) ?: return false

        val extraData = sharingCryptographyHelper.decryptItemContent(
            content,
            itemKey
        )

        if (extraData.isNullOrEmpty()) {
            return false 
        }

        val objectToSave = xmlConverterLazy.get().fromXml(itemId, extraData)
        if (objectToSave != null) {
            val objectType = objectToSave.vaultItem.syncObject.objectType
            if (sharingDao.isDirtyForSharing(itemId, objectType)) {
                return true
            }
        }

        val itemKeyBase64 = itemKey.encodeBase64ToString()

        
        memory.save(itemId, timestamp.toLong(), extraData, itemKeyBase64)

        return if (Status.ACCEPTED == userStatus) {
            
            sharingDao.saveAsLocalItem(objectToSave, userPermission.key)
            true
        } else {
            false
        }
    }

    private suspend fun insertOrUpdate(
        memory: SharingDaoMemoryDataAccess,
        itemUid: String,
        permission: Permission
    ) {
        val vaultItem = genericDataQuery.getSharableItem(itemUid)
        if (vaultItem == null) {
            
            memory.loadItemContentExtraData(itemUid)?.let { extraData ->
                
                sharingDao.saveAsLocalItem(itemUid, extraData, permission.key)
            }
        } else if (permission.key != vaultItem.sharingPermission) {
            
            sharingDao.updatePermission(itemUid, permission.key)
        }
    }

    private suspend fun ItemGroup.autoAcceptIfNecessary(
        session: Session,
        myUserGroups: List<UserGroup>
    ): Pair<List<ItemGroup>, List<UserGroup>> {
        return autoAcceptItemGroup.execute(session, this, myUserGroups)
    }

    private suspend fun invitePublicKeyUsers(
        session: Session,
        itemGroups: List<ItemGroup>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        myCollectionsAcceptedOrPending: List<Collection>
    ) = sharingInvitePublicKeyUser.execute(
        session,
        itemGroups,
        myUserGroupsAcceptedOrPending,
        myCollectionsAcceptedOrPending
    )

    private suspend fun ItemGroup.deleteItemGroupRemotelyAndKeepItems(
        memory: SharingDaoMemoryDataAccess,
        session: Session
    ) {
        items?.forEach { item ->
            val itemId = item.itemId
            
            val newItem = sharingDao.duplicateDataIdentifier(itemId) ?: return@forEach
            
            memory.deleteItemContent(itemId)
            
            sharingDao.updatePrivateCollections(newItem, itemId)
            
            sharingDao.deleteDataIdentifier(itemId)
            appEvents.post(DataIdentifierReplacedEvent(itemId, newItem.uid))
        }
        sendDeleteItemGroup(session)
        memory.delete(this)
    }

    private suspend fun ItemGroup.sendDeleteItemGroup(session: Session) {
        deleteItemGroupService.execute(
            session.authorization,
            DeleteItemGroupService.Request(
                groupId = DeleteItemGroupService.Request.GroupId(groupId),
                revision = revision
            )
        )
    }

    private fun saveUserGroupsDownload(
        memory: SharingDaoMemoryDataAccess,
        groupVerification: GroupVerification,
        list: List<UserGroup>
    ): List<UserGroup> {
        return list.filter { userGroup ->
            groupVerification.isValid(userGroup)
        }.also { memory.saveUserGroups(it) }
    }

    private fun saveCollections(
        memory: SharingDaoMemoryDataAccess,
        groupVerification: GroupVerification,
        myUserGroups: List<UserGroup>,
        list: List<Collection>
    ): List<Collection> {
        return list.filter { collection ->
            groupVerification.isValid(collection, myUserGroups)
        }.also { memory.saveCollections(it) }
    }

    private fun saveItemGroups(
        memory: SharingDaoMemoryDataAccess,
        groupVerification: GroupVerification,
        itemGroups: List<ItemGroup>,
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>
    ): List<ItemGroup> {
        return itemGroups.filter { itemGroup ->
            groupVerification.isValid(itemGroup, myUserGroups, myCollections)
        }.also { memory.saveItemGroups(it) }
    }

    private fun getItemGroupsToUpdate(
        memory: SharingDaoMemoryDataAccess,
        itemContentsToSave: List<ItemContent>,
        itemGroupsToSave: List<ItemGroup>
    ): List<ItemGroup> {
        return if (itemContentsToSave.isEmpty()) {
            itemGroupsToSave
        } else {
            
            val itemGroupsLoaded = memory.itemGroups
            itemGroupsLoaded.mergeWith(itemGroupsToSave)
        }
    }

    private fun List<ItemGroup>.mergeWith(list: List<ItemGroup>): List<ItemGroup> {
        if (list.isEmpty()) {
            return this 
        }
        if (isEmpty()) {
            return list 
        }
        
        val toAdd = list.filter { toAdd -> none { it.groupId == toAdd.groupId } }
        
        return plus(toAdd)
    }

    private fun loadMyUserGroups(
        memory: SharingDaoMemoryDataAccess,
        userId: String
    ): List<UserGroup> {
        return memory.loadUserGroupsAcceptedOrPending(userId)
    }

    private fun loadMyCollections(
        memory: SharingDaoMemoryDataAccess,
        userId: String,
        myUserGroupsAcceptedOrPending: List<UserGroup>
    ): List<Collection> {
        return memory.loadCollectionsAcceptedOrPending(userId, myUserGroupsAcceptedOrPending)
    }
}