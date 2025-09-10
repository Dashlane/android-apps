package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class FilterByCollection(
    val isFromSearch: Boolean,
    val totalCount: Int,
    val hasInteracted: Boolean,
    val collectionCount: Int,
) : TrackingLog {
    override val name
        get() = "filter_by_collection"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_from_search"] = isFromSearch
        collector["total_count"] = totalCount
        collector["has_interacted"] = hasInteracted
        collector["collection_count"] = collectionCount
    }
}