package com.dashlane.vault.summary

import com.dashlane.vault.model.SyncState
import com.dashlane.xml.domain.SyncObjectType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class SyncSummaryItem(
    @Json(name = "id")
    val id: String,
    @Json(name = "type")
    val type: SyncObjectType,
    @Json(name = "syncState")
    val syncState: SyncState,
    @Json(name = "backupDate")
    val backupDate: Instant? = null,
    @Json(name = "hasDirtySharedField")
    val hasDirtySharedField: Boolean = false,
    @Json(name = "userContentData")
    val userContentData: Int?,
)