package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.RequestStatus
import com.dashlane.hermes.generated.definitions.Rights
import com.dashlane.hermes.generated.definitions.SharingItemType
import javax.annotation.processing.Generated

@Generated
data class ShareItem(
    val requestStatus: RequestStatus,
    val individualsCount: Int,
    val itemType: SharingItemType,
    val groupsCount: Int,
    val rights: Rights,
) : TrackingLog {
    override val name
        get() = "share_item"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["request_status"] = requestStatus
        collector["individuals_count"] = individualsCount
        collector["item_type"] = itemType
        collector["groups_count"] = groupsCount
        collector["rights"] = rights
    }
}