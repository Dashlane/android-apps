package com.dashlane.teamspaces.manager

import com.dashlane.teamspaces.isSpaceSupported
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

class DataIdentifierSpaceCategorization(
    teamspaceAccessor: TeamSpaceAccessor,
    currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    forTeamspace: TeamSpace = currentTeamSpaceUiFilter.currentFilter.teamSpace
) {

    val showOnlySpace: TeamSpace?
    val forbiddenTeamspaces: List<TeamSpace>?
    val forbiddenDomains: List<String>?

    init {
        forbiddenDomains = teamspaceAccessor.pastBusinessTeams.flatMap { it.domainsToExcludeNow }

        when (forTeamspace) {
            TeamSpace.Personal -> {
                
                forbiddenTeamspaces = teamspaceAccessor.allBusinessSpaces
                showOnlySpace = forTeamspace
            }
            TeamSpace.Combined -> {
                forbiddenTeamspaces = null
                showOnlySpace = null
            }
            else -> {
                forbiddenTeamspaces = null
                showOnlySpace = forTeamspace
            }
        }
    }

    fun canBeDisplay(vaultItem: VaultItem<*>): Boolean {
        
        return when (vaultItem.syncObject) {
            is SyncObject.GeneratedPassword,
            is SyncObject.Settings,
            is SyncObject.DataChangeHistory -> true
            else -> canBeDisplay(vaultItem.toSummary<SummaryObject>())
        }
    }

    fun canBeDisplay(item: SummaryObject): Boolean {
        if (!item.syncObjectType.isSpaceSupported) return true

        val spaceId = item.spaceId?.takeUnless { it.isSemanticallyNull() }
            ?: TeamSpace.Personal.teamId

        
        showOnlySpace?.let { return spaceId == it.teamId }

        
        forbiddenTeamspaces?.let {
            if (forbiddenTeamspaces.any { spaceId == it.teamId }) {
                
                return false
            }
        }

        
        forbiddenDomains?.let {
            if (isItemAffectedByExcludedDomains(item, it)) {
                
                return false
            }
        }

        
        return true
    }

    private fun isItemAffectedByExcludedDomains(
        item: SummaryObject,
        domainsToExcludeFrom: List<String>
    ): Boolean {
        return item.matchForceDomains(domainsToExcludeFrom)
    }
}
