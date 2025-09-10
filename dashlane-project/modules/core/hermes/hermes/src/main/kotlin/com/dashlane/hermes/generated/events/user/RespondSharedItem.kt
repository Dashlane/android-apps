package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ResponseStatus
import com.dashlane.hermes.generated.definitions.Rights
import com.dashlane.hermes.generated.definitions.SharingItemType
import javax.annotation.processing.Generated

@Generated
data class RespondSharedItem(
    val responseStatus: ResponseStatus,
    val itemType: SharingItemType,
    val rights: Rights,
    val hasAccepted: Boolean,
) : TrackingLog {
    override val name
        get() = "respond_shared_item"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["response_status"] = responseStatus
        collector["item_type"] = itemType
        collector["rights"] = rights
        collector["has_accepted"] = hasAccepted
    }
}