@file:JvmName("StringUtils")

package com.dashlane.util

import android.annotation.SuppressLint
import com.dashlane.url.toUrlDomainOrNull
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
    "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\" +
            ".[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9]" +
            "(?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9]" +
            "(?:[a-z0-9-]*[a-z0-9])?",
    Pattern.CASE_INSENSITIVE
)



fun isNullOrEmpty(s: String?) = s.isNullOrEmpty()

fun CharSequence?.isNotSemanticallyNull() = !isSemanticallyNull()
fun CharSequence?.isSemanticallyNull() = isNullOrEmpty() || isValueNull()
fun CharSequence?.isValueNull() = this?.trim() == "null"



@SuppressLint("DefaultLocale")
fun String.toUpperCaseToDisplay(): String = uppercase(Locale.getDefault())



@SuppressLint("DefaultLocale")
fun String.capitalizeToDisplay(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun generateUniqueIdentifier() = UUID.randomUUID().let { "{$it}" }

fun String?.isValidEmail() = this != null && EMAIL_ADDRESS_PATTERN.matcher(this).matches()

fun String.ignoreEscapedCharacter() = this
    .replace("\n", "")
    .replace("\t", "")
    .replace("\r", "")

fun String?.toItemUuidOrNull(): UUID? {
    if (this == null) return null

    return try {
        UUID.fromString(removeSurrounding("{", "}"))
    } catch (e: IllegalArgumentException) {
        if (BuildConfig.DEBUG) {
            throw(e)
        }
        return null
    }
}

fun String?.matchDomain(domain: String?): Boolean {
    if (this == null || domain == null) return false
    
    if (this == domain) return true

    
    val urlHost = toUrlDomainOrNull()?.value ?: return false
    val urlDomain = domain.toUrlDomainOrNull()?.value ?: return false

    if (urlDomain == urlHost) return true 
    if (!urlHost.endsWith(urlDomain) && !urlDomain.endsWith(urlHost)) {
        
        return false
    }

    
    
    val indexHost = urlHost.lastIndexOf(urlDomain)
    if (indexHost >= 0) {
        return urlHost[indexHost - 1] == '.'
    }

    val indexDomain = urlDomain.lastIndexOf(urlHost)
    if (indexDomain >= 0) {
        return urlDomain[indexDomain - 1] == '.'
    }
    return false
}



fun String.otpToDisplay() = this.chunked(size = 3).joinToString("\u205f")