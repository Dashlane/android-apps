package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CollectionSelectOrigin
import javax.annotation.processing.Generated

@Generated
data class SelectCollection(
    val collectionId: String,
    val collectionSelectOrigin: CollectionSelectOrigin,
) : TrackingLog {
    override val name
        get() = "select_collection"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["collection_id"] = collectionId
        collector["collection_select_origin"] = collectionSelectOrigin
    }
}