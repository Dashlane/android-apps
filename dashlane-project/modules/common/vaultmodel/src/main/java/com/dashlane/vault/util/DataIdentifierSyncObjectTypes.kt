package com.dashlane.vault.util

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

operator fun SyncObjectType.Companion.get(summaryObject: SummaryObject): SyncObjectType {
    return summaryObject.syncObjectType
}

fun SummaryObject?.valueOfFromDataIdentifier() = this?.let { SyncObjectType[it] }
