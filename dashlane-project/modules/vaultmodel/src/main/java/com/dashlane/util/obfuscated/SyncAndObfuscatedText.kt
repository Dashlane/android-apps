package com.dashlane.util.obfuscated

import com.dashlane.xml.domain.SyncObfuscatedValue

fun String?.toSyncObfuscatedValue() = SyncObfuscatedValue(this ?: "")

fun SyncObfuscatedValue?.matchesNullAsEmpty(value: String?): Boolean {
    if (this == null) return value.isNullOrEmpty()
    return equalsString(value)
}

fun SyncObfuscatedValue?.isNullOrEmpty() = this == null || this.isEmpty()