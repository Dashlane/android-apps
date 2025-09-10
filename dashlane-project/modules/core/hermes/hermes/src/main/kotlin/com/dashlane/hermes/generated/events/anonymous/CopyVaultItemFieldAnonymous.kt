package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class CopyVaultItemFieldAnonymous(
    val field: Field,
    val itemType: ItemType,
    val domain: Domain,
) : TrackingLog {
    override val name
        get() = "copy_vault_item_field"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["field"] = field
        collector["item_type"] = itemType
        collector["domain"] = domain
    }
}