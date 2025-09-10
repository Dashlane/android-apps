package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.ItemTypeWithLink
import javax.annotation.processing.Generated

@Generated
data class OpenExternalVaultItemLinkAnonymous(
    val itemType: ItemTypeWithLink,
    val domain: Domain,
) : TrackingLog {
    override val name
        get() = "open_external_vault_item_link"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_type"] = itemType
        collector["domain"] = domain
    }
}