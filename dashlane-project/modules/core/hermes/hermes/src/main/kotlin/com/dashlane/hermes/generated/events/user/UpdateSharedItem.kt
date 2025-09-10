package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Rights
import com.dashlane.hermes.generated.definitions.SharingItemType
import com.dashlane.hermes.generated.definitions.UpdateStatus
import javax.annotation.processing.Generated

@Generated
data class UpdateSharedItem(
    val updateStatus: UpdateStatus,
    val itemType: SharingItemType,
    val rights: Rights,
) : TrackingLog {
    override val name
        get() = "update_shared_item"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["update_status"] = updateStatus
        collector["item_type"] = itemType
        collector["rights"] = rights
    }
}