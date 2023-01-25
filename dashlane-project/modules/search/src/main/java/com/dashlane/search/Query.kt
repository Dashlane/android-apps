package com.dashlane.search

import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.url.toUrlDomainOrNull

class Query(val queryString: String, private val startWithOnly: Boolean = false) {
    private val matchingQueryLinkedDomainsIds by lazy(LazyThreadSafetyMode.NONE) {
        if (startWithOnly) {
            
            null
        } else {
            KnownLinkedDomains.findLinkedDomainsIdsContaining(queryString)
        }
    }

    fun matchLinkedDomains(urlDomainString: String?): Boolean {
        
        val urlDomain = urlDomainString?.toUrlDomainOrNull() ?: return false

        
        if (startWithOnly) {
            return urlDomain.value.startsWith(queryString, ignoreCase = true)
        }

        val linkedDomainsIds = matchingQueryLinkedDomainsIds ?: return false
        return KnownLinkedDomains.getMatchingLinkedDomainId(urlDomain)?.let {
            linkedDomainsIds.contains(it)
        } ?: false
    }

    fun match(value: String?): Boolean {
        return value != null && if (startWithOnly) {
            value.startsWith(queryString, true)
        } else {
            value.contains(queryString, true)
        }
    }
}