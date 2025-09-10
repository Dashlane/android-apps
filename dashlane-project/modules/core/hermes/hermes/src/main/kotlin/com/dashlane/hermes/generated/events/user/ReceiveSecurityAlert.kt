package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.SecurityAlertType
import javax.annotation.processing.Generated

@Generated
data class ReceiveSecurityAlert(
    val itemTypesAffected: List<ItemType>,
    val securityAlertType: SecurityAlertType,
    val securityAlertItemId: ItemId,
) : TrackingLog {
    override val name
        get() = "receive_security_alert"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_types_affected"] = itemTypesAffected
        collector["security_alert_type"] = securityAlertType
        collector["security_alert_item_id"] = securityAlertItemId
    }
}