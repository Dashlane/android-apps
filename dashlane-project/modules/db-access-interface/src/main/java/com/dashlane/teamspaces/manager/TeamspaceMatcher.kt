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