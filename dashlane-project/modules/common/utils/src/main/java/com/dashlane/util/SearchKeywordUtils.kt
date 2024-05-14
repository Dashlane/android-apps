package com.dashlane.util

import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomain

object SearchKeywordUtils {

    fun fromUrl(url: String, withLinkedDomain: Boolean): List<String> {
        val urlDomain = tryOrNull { url.toUrlDomain() } ?: return listOf()
        val topDomain = urlDomain.root.value

        val websiteList: Collection<String> = if (withLinkedDomain) {
            KnownLinkedDomains.getWebsitesLinkedTo(topDomain)
        } else {
            listOf(topDomain)
        }

        val keywords = arrayListOf<String>()
        for (website in websiteList) {
            keywords.add(website)

            
            val websiteDomain = tryOrNull { website.toUrlDomain() }?.rootWithoutTld() ?: break
            if (websiteDomain.isNotSemanticallyNull() && website.length >= 3) {
                keywords.add(websiteDomain)
            }
        }
        return keywords
    }

    private fun UrlDomain.rootWithoutTld() = this.root.value.split(".").first()
}