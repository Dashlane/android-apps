package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.util.SearchKeywordUtils
import com.dashlane.util.matchDomain
import java.util.Locale

fun hasCorrectDomain(
    filter: CredentialFilter,
    url: String?,
    userSelectedUrl: String?,
    title: String?,
    bundleWebsites: List<String?>?
): Boolean {
    val domains = filter.domains ?: return true 
    val websiteKeywords = domains.flatMap {
        SearchKeywordUtils.fromUrl(url = it, withLinkedDomain = filter.allowSimilarDomains)
    }
    for (keyword in websiteKeywords.distinct()) {
        val hasMatch = url?.matchDomain(keyword) ?: false ||
                userSelectedUrl?.matchDomain(keyword) ?: false ||
                title?.hardMatchDomain(keyword) ?: false ||
                (
                    filter.allowSimilarDomains && bundleWebsites?.any {
                    it?.matchDomain(keyword) ?: false
                } ?: false
                )
        if (hasMatch) return true
    }
    
    return false
}

private fun String.hardMatchDomain(domain: String) =
    this.lowercase(Locale.getDefault()) == domain.lowercase(Locale.getDefault())