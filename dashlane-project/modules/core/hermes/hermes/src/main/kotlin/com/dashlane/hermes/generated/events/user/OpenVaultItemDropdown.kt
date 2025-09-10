package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DropdownType
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class OpenVaultItemDropdown(
    val highlight: Highlight? = null,
    val dropdownType: DropdownType,
    val itemType: ItemType,
    val totalCount: Int? = null,
    val index: Double? = null,
) : TrackingLog {
    override val name
        get() = "open_vault_item_dropdown"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["highlight"] = highlight
        collector["dropdown_type"] = dropdownType
        collector["item_type"] = itemType
        collector["total_count"] = totalCount
        collector["index"] = index
    }
}