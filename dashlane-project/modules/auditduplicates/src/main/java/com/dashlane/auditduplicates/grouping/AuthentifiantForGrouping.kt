package com.dashlane.auditduplicates.grouping

import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.valueWithoutWww
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject



class AuthentifiantForGrouping constructor(
    val authentifiant: SummaryObject.Authentifiant,
    val password: String? = null,
    private val secondaryLogin: String? = null
) {
    val urlDomain = authentifiant.urlForGoToWebsite?.toUrlDomainOrNull()
    private val urlDomainRoot by lazy { urlDomain?.root }
    private val associatedWebsite by lazy {
        urlDomain.getAssociatedWebsite()
    }

    

    val hasSimilarServiceData = urlDomain != null

    

    val similarIdentityData by lazy {
        setOfNotNull(
            authentifiant.email.takeIf { it.isNotSemanticallyNull() },
            authentifiant.login.takeIf { it.isNotSemanticallyNull() }
        )
    }

    

    val exactDuplicatesData by lazy {
        listOf(
            urlDomain?.valueWithoutWww(),
            authentifiant.email.takeIf { it.isNotSemanticallyNull() },
            authentifiant.login.takeIf { it.isNotSemanticallyNull() },
            secondaryLogin.takeIf { it.isNotSemanticallyNull() },
            password
        )
    }

    constructor(authentifiant: SyncObject.Authentifiant) :
            this(authentifiant.toSummary(), authentifiant.password.toString(), authentifiant.secondaryLogin)

    

    fun hasSimilarUrlDomain(other: AuthentifiantForGrouping): Boolean {
        if (urlDomain == null || other.urlDomain == null) {
            return urlDomain == other.urlDomain
        }

        return urlDomain == other.urlDomain ||
                urlDomainRoot == other.urlDomainRoot ||
                sameAssociatedWebsite(other)
    }

    

    private fun sameAssociatedWebsite(other: AuthentifiantForGrouping): Boolean {
        val associatedWebsite = associatedWebsite ?: return false
        val otherAssociatedWebsite = other.associatedWebsite ?: return false

        return associatedWebsite == otherAssociatedWebsite
    }

    

    fun hasSimilarIdentity(other: AuthentifiantForGrouping) =
        similarIdentityData.intersect(other.similarIdentityData).isNotEmpty()
}
