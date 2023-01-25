package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.util.SearchKeywordUtils
import com.dashlane.util.matchDomain
import com.dashlane.util.userfeatures.UserFeaturesChecker
import java.util.Locale

fun hasCorrectDomain(
    filter: CredentialFilter,
    userFeaturesChecker: UserFeaturesChecker,
    url: String?,
    userSelectedUrl: String?,
    title: String?,
    bundleWebsites: List<String?>?
): Boolean {
    val domains = filter.domains ?: return true 
    val multiDomainEnable = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES)
    val websiteKeywords = domains.flatMap {
        SearchKeywordUtils.fromUrl(url = it, withLinkedDomain = filter.allowSimilarDomains)
    }
    for (keyword in websiteKeywords.distinct()) {
        val hasMatch = url?.matchDomain(keyword) ?: false ||
                userSelectedUrl?.matchDomain(keyword) ?: false ||
                title?.hardMatchDomain(keyword) ?: false ||
                (filter.allowSimilarDomains && multiDomainEnable && bundleWebsites?.any {
                    it?.matchDomain(keyword) ?: false
                } ?: false)
        if (hasMatch) return true
    }
    
    return false
}

private fun String.hardMatchDomain(domain: String) =
    this.lowercase(Locale.getDefault()) == domain.lowercase(Locale.getDefault())