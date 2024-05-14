package com.dashlane.collections.sharing.item

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.sharing.handleCollectionSharingResult
import com.dashlane.core.sharing.toItemForEmailing
import com.dashlane.core.sharing.toSharedVaultItemLite
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.AddItemGroupsToCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateMultipleItemGroupsService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.RemoveItemGroupsFromCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.SessionManager
import com.dashlane.sharing.internal.builder.request.SharingRequestRepository
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataUpdateProvider
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class CollectionSharingItemDataProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingDataProvider: SharingDataProvider,
    private val sharingDataUpdateProvider: SharingDataUpdateProvider,
    private val sharingItemUpdater: SharingItemUpdater,
    private val sharingRequestRepository: SharingRequestRepository,
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val createItemGroupService: CreateItemGroupService,
    private val createMultipleItemGroupsService: CreateMultipleItemGroupsService,
    private val addItemGroupsToCollectionService: AddItemGroupsToCollectionService,
    private val removeItemGroupsFromCollectionService: RemoveItemGroupsFromCollectionService,
    private val sharingDao: SharingDao
) {
    private val authorization: Authorization.User
        get() = sessionManager.session!!.authorization
    private val login: String
        get() = sessionManager.session!!.userId

    suspend fun addItemToSharedCollections(
        sharedCollections: List<Collection>,
        item: SummaryObject
    ): Boolean {
        
        if (item.syncObjectType != SyncObjectType.AUTHENTIFIANT) return false
        if (sharedCollections.isEmpty()) return true
        
        
        var hasFailure = false
        val itemGroups = sharingDataProvider.getItemGroups()
        val userGroups = sharingDataProvider.getUserGroupsAccepted(login)
        val createdItemGroups = mutableListOf<ItemGroup>()
        sharedCollections.forEach { collection ->
            val itemGroup = findItemGroup(itemGroups, item.id) ?: createItemGroup(item)?.also {
                createdItemGroups.add(it)
            }
            if (!hasFailure) hasFailure = itemGroup == null
            itemGroup ?: return@forEach
            val response = addItemsGroupsToCollection(
                sharedCollection = collection,
                itemGroups = listOf(itemGroup),
                userGroups = userGroups
            )?.also { (collections, groups) ->
                sharingItemUpdater.handleCollectionSharingResult(collections, groups)
            }
            if (!hasFailure) hasFailure = response == null
        }
        if (hasFailure) {
            
            
            val items = sharingDataUpdateProvider.getUpdatedItemGroups(createdItemGroups)
                ?: createdItemGroups
            sharingItemUpdater.handleCollectionSharingResult(updatedItemGroups = items)
        }
        return hasFailure
    }

    suspend fun removeItemFromSharedCollections(
        itemId: String,
        sharedCollections: List<Collection>,
    ): Boolean {
        if (sharedCollections.isEmpty()) return true
        
        
        var hasFailure = false
        val itemGroups = sharingDataProvider.getItemGroups()
        val updatedCollections = mutableListOf<Collection>()
        val updatedItemGroups = mutableListOf<ItemGroup>()
        sharedCollections.forEach { collectionToRemove ->
            val existingItemGroup =
                findItemGroupInCollection(itemGroups, collectionToRemove, itemId)
            if (!hasFailure) hasFailure = existingItemGroup == null
            existingItemGroup ?: return@forEach
            val response = runCatching {
                removeItemGroupsFromCollectionService.execute(
                    userAuthorization = authorization,
                    request = RemoveItemGroupsFromCollectionService.Request(
                        collectionId = UuidFormat(collectionToRemove.uuid),
                        itemGroupsIds = listOf(UuidFormat(existingItemGroup.groupId)),
                        revision = collectionToRemove.revision
                    )
                )
            }.onFailure {
            }.getOrNull()?.also { collectionResponse ->
                updatedCollections.addAll(collectionResponse.data.collections!!)
                updatedItemGroups.add(existingItemGroup)
            }
            if (!hasFailure) hasFailure = response == null
        }
        sharingDataUpdateProvider.getUpdatedItemGroups(updatedItemGroups).let { groups ->
            sharingItemUpdater.handleCollectionSharingResult(
                collections = updatedCollections,
                updatedItemGroups = groups
            )
        }
        return hasFailure
    }

    suspend fun addItemsGroupsToCollection(
        sharedCollection: Collection,
        itemGroups: List<ItemGroup>,
        userGroups: List<UserGroup>
    ): Pair<List<Collection>, List<ItemGroup>>? {
        return runCatching {
            val request = sharingRequestRepository.createAddItemGroupsToCollectionRequest(
                collection = sharedCollection,
                itemGroups = itemGroups,
                userGroups = userGroups
            )
            addItemGroupsToCollectionService.execute(authorization, request).let {
                sharingDataUpdateProvider.getUpdatedItemGroups(itemGroups).let { updatedGroups ->
                    if (updatedGroups == null) {
                    }
                    it.data.collections!! to updatedGroups!!
                }
            }
        }.onFailure {
        }.getOrNull()
    }

    suspend fun createItemGroup(item: SummaryObject): ItemGroup? {
        
        if (item.syncObjectType != SyncObjectType.AUTHENTIFIANT) return null
        val dataIdentifierWithExtraData =
            sharingDao.getItemWithExtraData(item.id, item.syncObjectType)
        return runCatching {
            val request = sharingRequestRepository.createItemGroupRequest(
                users = emptyList(), 
                groups = emptyList(),
                item = ItemToShare(
                    item.id,
                    xmlConverter.toXml(dataIdentifierWithExtraData)!!,
                    ItemUpload.ItemType.AUTHENTIFIANT
                ),
                itemForEmailing = item.toSharedVaultItemLite().toItemForEmailing(),
                auditLogs = null
            )
            createItemGroupService.execute(authorization, request)
            
            
        }.onFailure {
                "ItemGroup can't be created for ${item.anonymousId}",
                throwable = it
            )
        }.getOrNull()?.data?.itemGroups?.first()
    }

    suspend fun createMultipleItemGroups(itemList: List<SummaryObject>): List<ItemGroup> {
        
        val items = itemList.filter { it.syncObjectType == SyncObjectType.AUTHENTIFIANT }
        
        if (items.isEmpty()) return emptyList()
        return runCatching {
            val itemsForSharing = items.map {
                val itemExtraData = sharingDao.getItemWithExtraData(it.id, it.syncObjectType)
                val itemToShare = ItemToShare(
                    it.id,
                    xmlConverter.toXml(itemExtraData)!!,
                    ItemUpload.ItemType.AUTHENTIFIANT
                )
                val itemForEmailing = it.toSharedVaultItemLite().toItemForEmailing()
                itemToShare to itemForEmailing
            }
            val request = sharingRequestRepository.createMultipleItemGroupsRequest(
                users = emptyList(), 
                groups = emptyList(),
                items = itemsForSharing,
                auditLogs = null
            )
            createMultipleItemGroupsService.execute(authorization, request)
        }.onFailure {
                "ItemGroups can't be created for ${items.map { it.anonymousId }}",
                throwable = it
            )
        }.getOrNull()?.data?.itemGroups ?: emptyList()
    }

    fun findItemGroup(itemGroups: List<ItemGroup>, itemId: String) =
        itemGroups.firstOrNull { itemGroup -> itemGroup.items?.first()?.itemId == itemId }

    private fun findItemGroupInCollection(
        itemGroups: List<ItemGroup>,
        collection: Collection,
        itemUid: String
    ) = itemGroups.firstOrNull { group ->
        group.collections?.any { it.uuid == collection.uuid } == true &&
            group.items?.any { sharedItem -> sharedItem.itemId == itemUid } == true
    }
}