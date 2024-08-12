package com.dashlane.util

import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull

fun UrlDomain?.valueWithoutWww(): String? = this?.value?.removePrefix("www.")

fun String.toRootUrlDomain(): UrlDomain? = this.toUrlDomainOrNull()?.root
