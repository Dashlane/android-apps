package com.dashlane.securearchive

import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.filter.collectionFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpecificSpaceFilter
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.toVaultItem
import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toObject
import javax.inject.Inject

class SecureArchiveDataImporter @Inject constructor(
    private val collectionDataQuery: CollectionDataQuery
) {

    fun generateNewVaultList(rawData: XmlBackup): List<VaultItem<SyncObject>> {
        val oldToNewIds = mutableMapOf<String, String>()
        val transactionList = rawData.toTransactionList()
        return transactionList.mapNotNull { transactionXml ->
            SyncObjectType.forXmlNameOrNull(transactionXml.type)?.takeUnless {
                
                
                it == SyncObjectType.SETTINGS || it == SyncObjectType.COLLECTION
            }?.let { type ->
                val syncObject = transactionXml.toObject(type)
                val newId = generateUniqueIdentifier()
                
                syncObject.id?.let { oldToNewIds[it] = newId }
                syncObject.toVaultItem(
                    overrideUid = newId,
                    overrideAnonymousUid = generateUniqueIdentifier(),
                    syncState = SyncState.MODIFIED
                )
            }
        } + collectionsWithItemLinkUpdated(transactionList, oldToNewIds)
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectionsWithItemLinkUpdated(
        transactionList: List<XmlTransaction>,
        oldToNewIds: Map<String, String>
    ): List<VaultItem<SyncObject.Collection>> {
        val collectionsToSave = transactionList.mapNotNull { transactionXml ->
            SyncObjectType.forXmlNameOrNull(transactionXml.type)
                ?.takeIf { it == SyncObjectType.COLLECTION }?.let { type ->
                    val collection = transactionXml.toObject(type).toVaultItem(
                        overrideUid = generateUniqueIdentifier(),
                        overrideAnonymousUid = generateUniqueIdentifier(),
                        syncState = SyncState.MODIFIED
                    ) as VaultItem<SyncObject.Collection>
                    collection.copySyncObject {
                        val newVaultItems = vaultItems?.mapNotNull newVaultItem@{ vaultItem ->
                            val newId = vaultItem.id?.let { oldToNewIds[vaultItem.id] }
                            
                                ?: return@newVaultItem null
                            vaultItem.copy { id = newId }
                        }
                        vaultItems = newVaultItems
                    }
                }
        }
        return mergeIntoExistingCollection(collectionsToSave)
    }

    private fun mergeIntoExistingCollection(
        collectionsToSave: List<VaultItem<SyncObject.Collection>>
    ): List<VaultItem<SyncObject.Collection>> = collectionsToSave.mapNotNull { collectionToSave ->
        val name = collectionToSave.syncObject.name ?: return@mapNotNull null
        val existingCollection =
            collectionDataQuery.queryByName(
                name,
                collectionFilter {
                    spaceFilter = SpecificSpaceFilter(listOf(TeamSpace.Personal))
                }
            )
                ?: return@mapNotNull collectionToSave
        mergeCollection(source = collectionToSave, target = existingCollection)
    }

    private fun mergeCollection(
        source: VaultItem<SyncObject.Collection>,
        target: VaultItem<SyncObject.Collection>
    ): VaultItem<SyncObject.Collection> {
        return target.copySyncObject {
            vaultItems = (vaultItems ?: emptyList()) + (source.syncObject.vaultItems ?: emptyList())
        }.copy(syncState = SyncState.MODIFIED)
    }
}