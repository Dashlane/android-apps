package com.dashlane.item.collection

import com.dashlane.sharing.SharingDataProvider
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import javax.inject.Inject

class CollectionSelectorDataProvider @Inject constructor(
    private val genericDataQuery: GenericDataQuery,
    private val sharingDataProvider: SharingDataProvider,
    private val collectionDataQuery: CollectionDataQuery,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
) {
    suspend fun fetchExistingCollections(itemId: String): List<CollectionSelectorState.Collection> {
        val summaryObject = genericDataQuery.queryFirst(GenericFilter(itemId)) ?: return emptyList()
        val privateCollections = getAllCollectionsInItemSpace(summaryObject.spaceId)
            .mapNotNull { collectionSummary ->
                val name = collectionSummary.name ?: return@mapNotNull null
                val selected = collectionSummary.vaultItems?.map { it.id }?.contains(itemId) ?: false
                CollectionSelectorState.Collection(
                    id = collectionSummary.id,
                    name = name,
                    shared = false,
                    selected = selected,
                    initiallySelected = selected,
                    forCreation = false,
                )
            }
        val sharedCollections = if ((summaryObject.spaceId ?: TeamSpace.Personal.teamId) == TeamSpace.Personal.teamId) {
            emptyList()
        } else {
            val alreadySelectedCollections = sharingDataProvider.getCollections(itemId)
            sharingDataProvider.getAcceptedCollections(needsAdminRights = false).map { sharedCollection ->
                val selected = sharedCollection.uuid in alreadySelectedCollections.map { it.uuid }
                CollectionSelectorState.Collection(
                    id = sharedCollection.uuid,
                    name = sharedCollection.name,
                    shared = true,
                    selected = selected,
                    initiallySelected = selected,
                    forCreation = false,
                )
            }
        }
        return privateCollections + sharedCollections
    }

    private fun getAllCollectionsInItemSpace(spaceId: String?) = collectionDataQuery.queryAll(
        CollectionFilter().apply {
            specificSpace(
                teamSpaceAccessorProvider.get()?.get(spaceId) ?: TeamSpace.Personal,
            )
        },
    )
}