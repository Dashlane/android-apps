package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemSource
import com.dashlane.hermes.generated.definitions.ItemTypeWithLink
import javax.annotation.processing.Generated

@Generated
data class OpenExternalVaultItemLink(
    val domainType: DomainType,
    val itemSource: ItemSource? = null,
    val itemId: ItemId,
    val itemType: ItemTypeWithLink,
) : TrackingLog {
    override val name
        get() = "open_external_vault_item_link"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["domain_type"] = domainType
        collector["item_source"] = itemSource
        collector["item_id"] = itemId
        collector["item_type"] = itemType
    }
}