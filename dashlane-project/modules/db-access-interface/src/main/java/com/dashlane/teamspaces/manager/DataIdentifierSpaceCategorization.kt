package com.dashlane.teamspaces.manager

import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.isSpaceSupported
import com.dashlane.xml.domain.SyncObject

class DataIdentifierSpaceCategorization(
    private val teamspaceAccessor: TeamspaceAccessor,
    forTeamspace: Teamspace = checkNotNull(teamspaceAccessor.current) { "No current space selected" }
) {

    val showOnlySpace: Teamspace?
    val forbiddenTeamspaces: List<Teamspace>?
    val forbiddenDomains: List<String>?

    init {
        forbiddenDomains = teamspaceAccessor.revokedAndDeclinedSpaces.flatMap { it.domainsToExcludeNow }

        when (forTeamspace.type) {
            Teamspace.Type.PERSONAL -> {
                
                forbiddenTeamspaces = teamspaceAccessor.all.filter {
                    it.type == Teamspace.Type.COMPANY && Teamspace.Status.ACCEPTED == it.status
                }
                showOnlySpace = null
            }
            Teamspace.Type.COMBINED -> {
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
            ?: PersonalTeamspace.teamId

        
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
