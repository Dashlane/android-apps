package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Highlight
import javax.annotation.processing.Generated

@Generated
data class SearchVaultItem(
    val highlight: Highlight? = null,
    val totalCount: Int,
    val hasInteracted: Boolean,
    val collectionCount: Int? = null,
    val hasClickedCollection: Boolean? = null,
    val charactersTypedCount: Int,
) : TrackingLog {
    override val name
        get() = "search_vault_item"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["highlight"] = highlight
        collector["total_count"] = totalCount
        collector["has_interacted"] = hasInteracted
        collector["collection_count"] = collectionCount
        collector["has_clicked_collection"] = hasClickedCollection
        collector["characters_typed_count"] = charactersTypedCount
    }
}