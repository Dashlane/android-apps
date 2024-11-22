package com.dashlane.vault.util

import com.dashlane.vault.summary.SummaryObject

val SummaryObject.isProtected: Boolean
    get() = when (this) {
        is SummaryObject.SecureNote -> secured ?: false
        is SummaryObject.Secret -> secured ?: false
        else -> false
    }