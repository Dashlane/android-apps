package com.dashlane.autofill.securitywarnings.data

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.url.toUrlDomainOrNull

fun AutoFillFormSource?.getDomain(): Domain = when (this) {
    is ApplicationFormSource -> Domain(Sha256Hash.of(packageName), DomainType.APP)
    is WebDomainFormSource -> Domain(
        id = webDomain.toUrlDomainOrNull()?.root?.value?.let {
            Sha256Hash.of(it)
        },
        type = DomainType.WEB
    )
    else -> Domain(type = DomainType.APP)
}