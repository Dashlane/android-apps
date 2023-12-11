package com.dashlane.autofill.util

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.url.UrlDomain

object AutofillLogUtil {
    fun extractDomainFrom(urlDomain: UrlDomain?, packageName: String?): DomainWrapper {
        val rootUrlDomain = urlDomain?.root?.value
        return when {
            rootUrlDomain == null && packageName != null -> DomainWrapper(
                domain = Domain(id = Sha256Hash.of(packageName), type = DomainType.APP),
                isNativeApp = true
            )
            rootUrlDomain != null -> DomainWrapper(
                domain = Domain(id = Sha256Hash.of(rootUrlDomain), type = DomainType.WEB),
                isNativeApp = false
            )
            else -> DomainWrapper(
                domain = Domain(id = null, type = DomainType.APP),
                isNativeApp = true
            )
        }
    }
}

data class DomainWrapper(val domain: Domain, val isNativeApp: Boolean)