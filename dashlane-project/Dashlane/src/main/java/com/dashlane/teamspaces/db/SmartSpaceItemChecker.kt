package com.dashlane.teamspaces.db

import com.dashlane.sharing.model.isAccepted
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.matchForceDomains
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class SmartSpaceItemChecker @Inject constructor(
    private val sharingDataProvider: SharingDataProvider,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val databaseAccessor: SmartSpaceCategorizationDatabaseAccessor
) {

    suspend fun checkForceSpace(itemId: String): Boolean {
        return isForcedByTeam(itemId) || isForcedByCollectionSharing(itemId)
    }

    private fun isForcedByTeam(itemId: String): Boolean {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return false
        val currentTeam = teamSpaceAccessor.currentBusinessTeam
        return if (currentTeam?.isForcedDomainsEnabled == true) {
            val itemsToMove: List<String> = databaseAccessor
                .getSummaryCandidatesForCategorization()
                .matchForceDomains(currentTeam.domains)
                .map { it.id }
            itemId in itemsToMove
        } else {
            false
        }
    }

    private suspend fun isForcedByCollectionSharing(itemId: String): Boolean {
        val itemIdsToForce = sharingDataProvider.getItemGroups().mapNotNull { group ->
            if (group.collections?.any { it.isAccepted } == true) group.items?.map { it.itemId } else null
        }.flatten()
        return itemId in itemIdsToForce
    }
}
