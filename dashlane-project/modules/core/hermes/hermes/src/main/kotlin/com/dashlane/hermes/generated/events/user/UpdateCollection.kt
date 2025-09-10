package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CollectionAction
import javax.annotation.processing.Generated

@Generated
data class UpdateCollection(
    val collectionId: String,
    val itemCount: Int,
    val isShared: Boolean,
    val action: CollectionAction,
) : TrackingLog {
    override val name
        get() = "update_collection"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["collection_id"] = collectionId
        collector["item_count"] = itemCount
        collector["is_shared"] = isShared
        collector["action"] = action
    }
}