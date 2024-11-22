package com.dashlane.vault.utils

import com.dashlane.vault.summary.SummaryObject

fun SummaryObject.attachmentsAllowed(
    attachmentAllItems: Boolean,
    isAccountFrozen: Boolean = false,
    hasCollections: Boolean = false
) = when {
    isAccountFrozen -> false
    isShared -> false
    hasCollections -> false
    this is SummaryObject.SecureNote -> true
    this is SummaryObject.Secret -> true
    attachmentAllItems -> true
    else -> false
}
