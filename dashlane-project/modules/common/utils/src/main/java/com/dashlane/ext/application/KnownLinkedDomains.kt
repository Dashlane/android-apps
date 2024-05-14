package com.dashlane.ext.application

import com.dashlane.url.UrlDomain
import com.dashlane.url.domain.hardcoded.ImmutableHardcodedLinkedDomainsRepository
import com.dashlane.url.domain.links.ExternalRepositoryUrlDomainLinks
import com.dashlane.url.domain.links.UrlDomainLinks
import com.dashlane.url.toUrlDomainOrNull

object KnownLinkedDomains {

    internal val urlDomainLinks: UrlDomainLinks =
        ExternalRepositoryUrlDomainLinks(ImmutableHardcodedLinkedDomainsRepository())

    @JvmStatic
    fun getWebsitesLinkedTo(website: String): Set<String> {
        val websiteDomain = website.toUrlDomainOrNull() ?: return emptySet()

        val websiteFromKnownApps =
            urlDomainLinks.matchLinkedDomains(websiteDomain)?.linkedDomains
                ?: emptySet()

        return websiteFromKnownApps.map {
            it.value
        }.toSet() + website
    }

    fun getMatchingLinkedDomainSet(urlDomainString: String?): Set<UrlDomain>? {
        return urlDomainString?.toUrlDomainOrNull()?.let {
            urlDomainLinks.matchLinkedDomains(it)?.linkedDomains
        }
    }

    fun getMatchingLinkedDomainId(urlDomainString: String?): String? {
        return urlDomainString?.toUrlDomainOrNull()?.let {
            getMatchingLinkedDomainId(it)
        }
    }

    fun getMatchingLinkedDomainId(urlDomain: UrlDomain): String? {
        return urlDomainLinks.matchLinkedDomains(urlDomain)?.linkedDomainsId
    }

    fun findLinkedDomainsIdsContaining(search: String): List<String> {
        return urlDomainLinks.filterLinkedDomains(search).map { it.linkedDomainsId }
    }
}