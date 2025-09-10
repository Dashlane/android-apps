package com.dashlane.hermes.storage

import com.dashlane.hermes.TrackingLog
import com.dashlane.server.api.analytics.events.EventService

enum class StorageCategory(
    val logCategory: TrackingLog.Category,
    val eventType: EventService.EventType,
    val directory: String,
) {
    USER(
        TrackingLog.Category.USER,
        EventService.EventType.USER,
        "encrypted",
    ),
    ANONYMOUS(
        TrackingLog.Category.ANONYMOUS,
        EventService.EventType.ANONYMOUS,
        "encryptedAnonymous",
    ),
    ACTIVITY_LOG(
        TrackingLog.Category.ACTIVITY_LOG,
        EventService.EventType.USER,
        "encryptedActivityLog",
    ),
}