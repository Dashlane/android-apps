package com.dashlane.auditduplicates.grouping

import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.url.UrlDomain



fun List<AuthentifiantForGrouping>.duplicatesGroupedByServiceByIdentity(): List<List<List<AuthentifiantForGrouping>>> =
    groupBySimilarUrlDomain()
        .filter { it.size > 1 }
        .map { similarDomains ->
            similarDomains.groupBySimilarIdentity().filter { it.size > 1 }
        }



fun List<AuthentifiantForGrouping>.groupBySimilarUrlDomain(): List<List<AuthentifiantForGrouping>> =
    filter { it.hasSimilarServiceData }
        .groupBySimilarContent(AuthentifiantForGrouping::hasSimilarUrlDomain)



fun List<AuthentifiantForGrouping>.groupBySimilarIdentity(): List<List<AuthentifiantForGrouping>> =
    filter { it.similarIdentityData.isNotEmpty() }
        .groupBySimilarContent(AuthentifiantForGrouping::hasSimilarIdentity)

internal fun UrlDomain?.getAssociatedWebsite(): String? = this?.let { KnownLinkedDomains.getMatchingLinkedDomainId(it) }



private inline fun <C> List<C>.groupBySimilarContent(similarContent: (C, C) -> Boolean): List<List<C>> =
    fold(listOf()) { acc, element ->
        
        
        
        val (similarGroups, otherGroups) = acc.partition { group -> group.any { similarContent.invoke(it, element) } }
        
        
        otherGroups.plusElement(similarGroups.flatten() + element)
    }
