package com.dashlane.hermes.storage

import com.dashlane.hermes.TrackingLog
import java.time.Instant
import java.util.UUID

data class LogItem(
    val dateTime: Instant,
    val logId: UUID,
    val logContent: String,
    val category: TrackingLog.Category,
) {
    val storageCategory: StorageCategory
        get() =
            when (category) {
                TrackingLog.Category.USER -> StorageCategory.USER
                TrackingLog.Category.ANONYMOUS -> StorageCategory.ANONYMOUS
                TrackingLog.Category.ACTIVITY_LOG -> StorageCategory.ACTIVITY_LOG
            }

    val fileName: String
        get() = "${dateTime.toEpochMilli()}" + FILE_NAME_SEPARATOR + logId

    companion object {
        const val FILE_NAME_SEPARATOR = "_"
    }
}