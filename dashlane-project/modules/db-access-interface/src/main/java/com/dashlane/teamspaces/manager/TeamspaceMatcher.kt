package com.dashlane.teamspaces.manager

import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

fun VaultItem<*>.getSuggestedTeamspace(teamspaceAccessor: TeamspaceAccessor?): Teamspace? {
    
    teamspaceAccessor ?: return null
    
    
    val teamspaces = teamspaceAccessor.all + teamspaceAccessor.revokedAndDeclinedSpaces
    return when (val item = syncObject) {
        is SyncObject.Authentifiant -> {
            teamspaces.firstOrNullMatchingDefinedDomain(
                arrayOf(
                    item.url,
                    item.login,
                    item.email,
                    item.secondaryLogin,
                    item.userSelectedUrl
                )
            )
        }
        is SyncObject.Email -> {
            teamspaces.firstOrNullMatchingDefinedDomain(arrayOf(item.email))
        }
        is SyncObject.PersonalWebsite -> {
            teamspaces.firstOrNullMatchingDefinedDomain(arrayOf(item.website))
        }
        is SyncObject.PaymentPaypal -> {
            teamspaces.firstOrNullMatchingDefinedDomain(arrayOf(item.login))
        }
        else -> null
    }
}

fun List<Teamspace>.firstOrNullMatchingDefinedDomain(labels: Array<String?>): Teamspace? {
    return firstOrNull { teamspace ->
        labels.filterNotNull().any { teamspace.matchDefinedDomain(it) }
    }
}

fun Teamspace.matchDefinedDomain(label: String): Boolean {
    return domains.any {
        label.contains(it, ignoreCase = true)
    }
}

object TeamspaceMatcher {
    @JvmField
    val DATA_TYPE_TO_MATCH = listOf(
        SyncObjectType.AUTHENTIFIANT,
        SyncObjectType.EMAIL
    )
}



fun SummaryObject.matchForceDomains(domains: List<String>): Boolean {
    when (this) {
        is SummaryObject.Authentifiant -> {
            domains.forEach { domain ->
                if (this.email?.contains(domain, ignoreCase = true) == true) return true
                if (this.login?.contains(domain, ignoreCase = true) == true) return true
                if (this.userSelectedUrl?.contains(domain, ignoreCase = true) == true) return true
                if (this.url?.contains(domain, ignoreCase = true) == true) return true
                if (this.secondaryLogin?.contains(domain, ignoreCase = true) == true) return true
                if (this.linkedServices?.associatedDomains?.any {
                        it.domain.contains(domain, ignoreCase = true)
                    } == true) return true
            }
        }
        is SummaryObject.Email -> {
            domains.forEach { domain ->
                if (this.email?.contains(domain, ignoreCase = true) == true) return true
            }
        }
        else -> return false
    }
    return false
}
