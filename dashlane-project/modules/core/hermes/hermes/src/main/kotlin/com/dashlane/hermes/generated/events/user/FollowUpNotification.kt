package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FollowUpNotificationActions
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class FollowUpNotification(
    val itemType: ItemType? = null,
    val action: FollowUpNotificationActions,
) : TrackingLog {
    override val name
        get() = "follow_up_notification"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_type"] = itemType
        collector["action"] = action
    }
}