package com.dashlane.storage.userdata.internal

import com.dashlane.xml.domain.SyncObject
import java.time.Instant

data class ChangeSetForDb(
    val sqliteId: Long = 0,
    val uid: String,
    val dataChangeHistoryUID: String? = null,
    val modificationTimestampSeconds: Instant? = null,
    val user: String? = null,
    val platform: SyncObject.Platform? = null,
    val deviceName: String? = null,
    val isRemoved: Boolean = false
)