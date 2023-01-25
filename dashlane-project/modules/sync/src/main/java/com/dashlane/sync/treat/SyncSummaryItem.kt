package com.dashlane.sync.treat

import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant

data class SyncSummaryItem(
    val objectId: String,
    val lastUpdateTime: Instant,
    val syncObjectType: SyncObjectType
)
