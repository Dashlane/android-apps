package com.dashlane.autofill.fillresponse

import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.url.UrlDomain

fun UrlDomain?.relatedOnlyByLinkedDomains(associatedUrlDomain: UrlDomain?): Boolean {
    val urlDomain = this ?: return false
    val otherUrlDomain = associatedUrlDomain ?: return false

    if (urlDomain.root == otherUrlDomain.root) {
        return false
    }

    val formAssociatedWebSite = KnownLinkedDomains.getMatchingLinkedDomainId(urlDomain) ?: return false
    val vaultAssociatedWebSite = KnownLinkedDomains.getMatchingLinkedDomainId(otherUrlDomain) ?: return false

    return formAssociatedWebSite == vaultAssociatedWebSite
}
