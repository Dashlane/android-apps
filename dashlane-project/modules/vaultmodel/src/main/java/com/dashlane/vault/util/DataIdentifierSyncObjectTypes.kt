package com.dashlane.vault.util

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.SyncObjectTypeUtils

operator fun SyncObjectType.Companion.get(summaryObject: SummaryObject): SyncObjectType {
    return summaryObject.syncObjectType
}

val SyncObjectType.isSpaceSupported: Boolean
    get() {
        return this in SyncObjectTypeUtils.WITH_TEAMSPACES
    }

fun SummaryObject?.valueOfFromDataIdentifier() = this?.let { SyncObjectType[it] }
