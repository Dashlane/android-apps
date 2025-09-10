package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class ItemTypeCounts(
    val collectionsCount: Int? = null,
    val singleCollectionSharedCount: Int? = null,
    val totalCount: Int,
    val multipleCollectionsSharedCount: Int? = null,
    val collectionsSharedCount: Int? = null,
    val sharedCount: Int? = null,
    val singleCollectionCount: Int? = null,
    val multipleCollectionsCount: Int? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["collections_count"] = collectionsCount
        collector["single_collection_shared_count"] = singleCollectionSharedCount
        collector["total_count"] = totalCount
        collector["multiple_collections_shared_count"] = multipleCollectionsSharedCount
        collector["collections_shared_count"] = collectionsSharedCount
        collector["shared_count"] = sharedCount
        collector["single_collection_count"] = singleCollectionCount
        collector["multiple_collections_count"] = multipleCollectionsCount
    }
}