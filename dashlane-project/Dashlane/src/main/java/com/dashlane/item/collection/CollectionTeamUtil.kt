package com.dashlane.item.collection

import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.toCollectionDataType
import com.dashlane.vault.summary.CollectionVaultItems

suspend fun TeamSpaceAccessor.getAllCollections(
    item: VaultItem<*>,
    collectionDataQuery: CollectionDataQuery,
    sharingDataProvider: SharingDataProvider
): List<ItemCollectionListSubView.Collection> {
    val collections = collectionDataQuery.queryAll(
        CollectionFilter().apply {
            withVaultItem = CollectionVaultItems(item.toCollectionDataType(), item.uid)
            specificSpace(
                getOrDefault(item.syncObject.spaceId)
            )
        }
    ).mapNotNull {
        val name = it.name ?: return@mapNotNull null
        ItemCollectionListSubView.Collection(id = it.id, name = name, shared = false)
    }
    val sharedCollections = sharingDataProvider.getCollections(item.uid).map {
        ItemCollectionListSubView.Collection(id = it.uuid, name = it.name, shared = true)
    }
    return (collections + sharedCollections).sortedBy { it.name }
}