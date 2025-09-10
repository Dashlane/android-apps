package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemSource
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class CopyVaultItemField(
    val highlight: Highlight? = null,
    val itemSource: ItemSource? = null,
    val field: Field,
    val itemId: ItemId,
    val itemType: ItemType,
    val totalCount: Int? = null,
    val index: Double? = null,
    val isProtected: Boolean,
) : TrackingLog {
    override val name
        get() = "copy_vault_item_field"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["highlight"] = highlight
        collector["item_source"] = itemSource
        collector["field"] = field
        collector["item_id"] = itemId
        collector["item_type"] = itemType
        collector["total_count"] = totalCount
        collector["index"] = index
        collector["is_protected"] = isProtected
    }
}