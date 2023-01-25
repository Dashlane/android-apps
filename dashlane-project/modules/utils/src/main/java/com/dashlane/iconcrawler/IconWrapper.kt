@file:JvmName("IconWrapperUtils")

package com.dashlane.iconcrawler

import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull

interface IconWrapper {
    fun getUrlForIcon(): String?
}

fun List<*>?.mapIconWrappersToUrlDomainIcons(): List<UrlDomain> =
    this?.filterIsInstance<IconWrapper>()?.mapNotNull { it.getUrlForIcon()?.toUrlDomainOrNull() } ?: emptyList()