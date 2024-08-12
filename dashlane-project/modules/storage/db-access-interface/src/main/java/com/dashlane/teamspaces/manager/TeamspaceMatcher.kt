package com.dashlane.teamspaces.manager

import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

fun VaultItem<*>.getSuggestedTeamspace(teamspaceAccessor: TeamSpaceAccessor?): TeamSpace? {
    
    teamspaceAccessor ?: return null
    
    
    val teamSpaces: List<TeamSpace> = teamspaceAccessor.allBusinessSpaces
    return when (val item = syncObject) {
        is SyncObject.Authentifiant -> {
            teamSpaces.firstOrNullMatchingDefinedDomain(
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
            teamSpaces.firstOrNullMatchingDefinedDomain(arrayOf(item.email))
        }
        is SyncObject.PersonalWebsite -> {
            teamSpaces.firstOrNullMatchingDefinedDomain(arrayOf(item.website))
        }
        is SyncObject.PaymentPaypal -> {
            teamSpaces.firstOrNullMatchingDefinedDomain(arrayOf(item.login))
        }
        else -> null
    }
}

fun List<TeamSpace>.firstOrNullMatchingDefinedDomain(labels: Array<String?>): TeamSpace? {
    return firstOrNull { teamspace ->
        labels.filterNotNull().any { teamspace.matchDefinedDomain(it) }
    }
}

fun TeamSpace.matchDefinedDomain(label: String): Boolean {
    return domains.any {
        label.contains(it, ignoreCase = true)
    }
}

object TeamspaceMatcher {
    @JvmField
    val DATA_TYPE_TO_MATCH = listOf(
        SyncObjectType.AUTHENTIFIANT,
        SyncObjectType.EMAIL,
        SyncObjectType.PASSKEY
    )
}

fun SummaryObject.matchForceDomains(domains: List<String>): Boolean {
    return when (this) {
        is SummaryObject.Authentifiant -> domains.any { domain -> isAuthentifiantMatchingDomain(this, domain) }
        is SummaryObject.Email -> domains.any { domain -> isEmailMatchingDomain(this, domain) }
        is SummaryObject.Passkey -> domains.any { domain -> isPasskeyMatchingDomain(this, domain) }
        else -> false
    }
}

fun List<SummaryObject>.matchForceDomains(domains: List<String>): List<SummaryObject> {
    
    return filter { summaryObject ->
        summaryObject.matchForceDomains(domains)
    }
}

private fun isAuthentifiantMatchingDomain(authentifiant: SummaryObject.Authentifiant, domain: String): Boolean {
    return authentifiant.email?.contains(domain, ignoreCase = true) == true ||
        authentifiant.login?.contains(domain, ignoreCase = true) == true ||
        authentifiant.userSelectedUrl?.contains(domain, ignoreCase = true) == true ||
        authentifiant.url?.contains(domain, ignoreCase = true) == true ||
        authentifiant.secondaryLogin?.contains(domain, ignoreCase = true) == true ||
        authentifiant.linkedServices?.associatedDomains?.any {
            it.domain.contains(domain, ignoreCase = true)
        } == true
}

private fun isEmailMatchingDomain(email: SummaryObject.Email, domain: String): Boolean {
    return email.email?.contains(domain, ignoreCase = true) == true
}

private fun isPasskeyMatchingDomain(passkey: SummaryObject.Passkey, domain: String): Boolean {
    return passkey.rpId?.contains(domain, ignoreCase = true) == true ||
        passkey.userDisplayName?.contains(domain, ignoreCase = true) == true
}