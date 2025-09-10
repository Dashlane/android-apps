package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemSource
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class UseVaultItem(
    val itemSource: ItemSource? = null,
    val itemId: ItemId,
    val itemType: ItemType,
    val fieldsUsed: List<Field>,
    val action: Action,
) : TrackingLog {
    override val name
        get() = "use_vault_item"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_source"] = itemSource
        collector["item_id"] = itemId
        collector["item_type"] = itemType
        collector["fields_used"] = fieldsUsed
        collector["action"] = action
    }
}