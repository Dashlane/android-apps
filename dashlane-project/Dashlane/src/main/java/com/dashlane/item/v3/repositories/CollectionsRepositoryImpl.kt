package com.dashlane.item.v3.repositories

import android.content.Context
import com.dashlane.collections.sharing.item.CollectionSharingItemDataProvider
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CollectionAction
import com.dashlane.hermes.generated.events.user.UpdateCollection
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.util.fillDefaultValue
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createCollection
import com.dashlane.vault.model.toCollectionDataType
import com.dashlane.vault.model.toCollectionVaultItem
import com.dashlane.vault.summary.CollectionVaultItems
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CollectionsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val collectionSharingItemDataProvider: CollectionSharingItemDataProvider,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val sharingDataProvider: SharingDataProvider,
    private val hermesLogRepository: LogRepository,
    private val collectionDataQuery: CollectionDataQuery,
    private val activityLogger: VaultActivityLogger,
    private val dataSaver: DataSaver
) : CollectionsRepository {

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    override suspend fun getCollections(item: SummaryObject): List<CollectionData> {
        val collections = collectionDataQuery.queryAll(
            CollectionFilter().apply {
                withVaultItem = CollectionVaultItems(item.toCollectionDataType(), item.id)
                specificSpace(
                    teamSpaceAccessorProvider.get()?.getOrDefault(item.spaceId) ?: return@apply
                )
            }
        ).mapNotNull {
            val name = it.name ?: return@mapNotNull null
            CollectionData(id = it.id, name = name, shared = false)
        }
        val sharedCollections = sharingDataProvider.getCollections(item.id).map {
            CollectionData(id = it.uuid, name = it.name, shared = true)
        }
        return (collections + sharedCollections).sortedBy { it.name }
    }

    override suspend fun saveCollections(item: VaultItem<SyncObject>, data: CredentialFormData) {
        
        val itemToSave = item.fillDefaultValue(context, sessionManager.session)
        
        val privateCollections = data.collections.filter { !it.shared }.map { it.name }
        savePrivateCollectionsToSave(itemToSave, privateCollections)

        
        val sharedCollections = data.collections.filter { it.shared }
        saveSharedCollections(itemToSave, sharedCollections)
    }

    private suspend fun saveSharedCollections(
        itemToSave: VaultItem<*>,
        sharedCollections: List<CollectionData>,
    ) {
        val sharedCollectionsForItem = sharingDataProvider.getCollections(itemToSave.uid)
        val sharedCollectionToAdd = sharedCollections.filter {
            it.id != null && sharedCollectionsForItem.none { c -> c.uuid == it.id }
        }
        
        val allCollections = sharingDataProvider.getAcceptedCollections(needsAdminRights = false)
        runCatching {
            if (sharedCollectionToAdd.isNotEmpty()) {
                collectionSharingItemDataProvider.addItemToSharedCollections(
                    sharedCollectionToAdd.mapNotNull { collection ->
                        allCollections.firstOrNull { it.uuid == collection.id }
                    },
                    itemToSave.toSummary()
                )
            }
            
            val sharedCollectionToRemove = sharedCollectionsForItem.filter {
                sharedCollections.none { c -> c.id == it.uuid }
            }
            if (sharedCollectionToRemove.isNotEmpty()) {
                collectionSharingItemDataProvider.removeItemFromSharedCollections(
                    itemToSave.uid,
                    sharedCollectionToRemove
                )
            }
        }
    }

    private suspend fun savePrivateCollectionsToSave(
        item: VaultItem<*>,
        privateCollections: List<String>
    ) {
        val alreadyExistingCollections = collectionDataQuery.queryAll(
            CollectionFilter().apply {
                withVaultItem = CollectionVaultItems(item.toCollectionDataType(), item.uid)
            }
        )

        val collectionNamesToAdd = privateCollections.filterNot { name ->
            alreadyExistingCollections.filter { it.spaceId == item.syncObject.spaceId }
                .map { it.name }.contains(name)
        }.toSet()

        val collectionsSummaryToRemove =
            alreadyExistingCollections.filterNot { privateCollections.contains(it.name) && it.spaceId == item.syncObject.spaceId }

        val syncCollectionsToAdd = buildAddCollectionVaultItemList(item, collectionNamesToAdd)
        val syncCollectionsToRemove =
            buildRemovedCollectionVaultItemList(item, collectionsSummaryToRemove)

        logCollectionUpdates(syncCollectionsToAdd, syncCollectionsToRemove, item)

        dataSaver.save(syncCollectionsToAdd + syncCollectionsToRemove)
    }

    private fun buildAddCollectionVaultItemList(
        item: VaultItem<*>,
        collectionNamesToAdd: Set<String>
    ): List<VaultItem<SyncObject.Collection>> {
        return collectionNamesToAdd.map { name ->
            collectionDataQuery.queryByName(
                name,
                CollectionFilter().apply {
                    val spaceFilter = teamSpaceAccessor?.currentBusinessTeam
                        ?.takeIf { item.syncObject.spaceId == it.teamId }
                        ?: TeamSpace.Personal
                    specificSpace(spaceFilter)
                }
            )?.let {
                it.copySyncObject {
                    vaultItems = (vaultItems ?: emptyList()) + item.toCollectionVaultItem()
                }.copyWithAttrs {
                    syncState = SyncState.MODIFIED
                }
            } ?: createCollection(
                dataIdentifier = CommonDataIdentifierAttrsImpl(teamSpaceId = item.syncObject.spaceId),
                name = name,
                vaultItems = listOf(item.toCollectionVaultItem())
            )
        }
    }

    private fun buildRemovedCollectionVaultItemList(
        item: VaultItem<*>,
        collectionsSummaryToRemove: List<SummaryObject.Collection>
    ): List<VaultItem<SyncObject.Collection>> =
        collectionDataQuery.queryByIds(collectionsSummaryToRemove.map { it.id })
            .map { syncCollection ->
                syncCollection.copy(
                    syncObject = syncCollection.syncObject.copy {
                        vaultItems = vaultItems?.filterNot { it.id == item.uid }
                    }
                )
            }

    private fun logCollectionUpdates(
        collectionsToAdd: List<VaultItem<SyncObject.Collection>>,
        collectionsToRemove: List<VaultItem<SyncObject.Collection>>,
        item: VaultItem<*>
    ) {
        for (collection in collectionsToAdd) {
            if (collection.syncState != SyncState.MODIFIED) { 
                
                hermesLogRepository.queueEvent(
                    UpdateCollection(
                        collectionId = collection.uid,
                        action = CollectionAction.ADD,
                        itemCount = 1,
                        isShared = false
                    )
                )
                activityLogger.sendCollectionCreatedActivityLog(collection.toSummary())
            }
            
            hermesLogRepository.queueEvent(
                UpdateCollection(
                    collectionId = collection.uid,
                    action = CollectionAction.ADD_CREDENTIAL,
                    itemCount = 1,
                    isShared = false
                )
            )

            activityLogger.sendAddItemToCollectionActivityLog(
                collection = collection.toSummary(),
                item = item.toSummary()
            )
        }

        for (collection in collectionsToRemove) {
            
            hermesLogRepository.queueEvent(
                UpdateCollection(
                    collectionId = collection.uid,
                    action = CollectionAction.DELETE_CREDENTIAL,
                    itemCount = 1,
                    isShared = false
                )
            )
            activityLogger.sendRemoveItemFromCollectionActivityLog(
                collection = collection.toSummary(),
                item = item.toSummary()
            )
        }
    }
}