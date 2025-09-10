package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.Origin
import com.dashlane.hermes.generated.definitions.SharingFlowType
import javax.annotation.processing.Generated

@Generated
data class SharingStart(
    val collectionId: String? = null,
    val sharingFlowType: SharingFlowType,
    val itemId: ItemId? = null,
    val origin: Origin,
) : TrackingLog {
    override val name
        get() = "sharing_start"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["collection_id"] = collectionId
        collector["sharing_flow_type"] = sharingFlowType
        collector["item_id"] = itemId
        collector["origin"] = origin
    }
}