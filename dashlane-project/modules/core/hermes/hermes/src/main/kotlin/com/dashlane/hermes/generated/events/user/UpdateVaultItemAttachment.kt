package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class UpdateVaultItemAttachment(
    val itemId: ItemId,
    val itemType: ItemType,
    val attachmentAction: Action,
) : TrackingLog {
    override val name
        get() = "update_vault_item_attachment"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_id"] = itemId
        collector["item_type"] = itemType
        collector["attachment_action"] = attachmentAction
    }
}