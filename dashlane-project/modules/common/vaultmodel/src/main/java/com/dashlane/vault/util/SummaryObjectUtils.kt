package com.dashlane.vault.util

import com.dashlane.vault.summary.SummaryObject

val SummaryObject.isProtected
    get() = (this as? SummaryObject.SecureNote)?.secured ?: false