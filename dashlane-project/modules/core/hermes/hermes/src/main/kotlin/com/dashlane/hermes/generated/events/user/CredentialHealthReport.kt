package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CredentialSecurityStatus
import com.dashlane.hermes.generated.definitions.ItemId
import javax.annotation.processing.Generated

@Generated
data class CredentialHealthReport(
    val itemId: ItemId,
    val credentialSecurityStatus: CredentialSecurityStatus,
) : TrackingLog {
    override val name
        get() = "credential_health_report"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_id"] = itemId
        collector["credential_security_status"] = credentialSecurityStatus
    }
}