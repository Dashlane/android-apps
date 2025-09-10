package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemSource
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class DownloadVaultItemAttachment(
    val itemSource: ItemSource? = null,
    val itemId: ItemId,
    val itemType: ItemType,
) : TrackingLog {
    override val name
        get() = "download_vault_item_attachment"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_source"] = itemSource
        collector["item_id"] = itemId
        collector["item_type"] = itemType
    }
}