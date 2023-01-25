package com.dashlane.core

import com.dashlane.iconcrawler.IconWrapper

fun popularWebsitesToIconWrappers(list: List<String>?): List<IconWrapper>? = list?.map { it.toIconWrapper() }

private fun String.toIconWrapper(): IconWrapper {
    val popularWebsite = this
    return object : IconWrapper {
        override fun getUrlForIcon() = popularWebsite
    }
}