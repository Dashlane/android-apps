package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import java.util.UUID
import javax.annotation.processing.Generated

@Generated
data class Device(
    val os: Os,
    val installationId: UUID,
    val id: UUID? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["os"] = os
        collector["installation_id"] = installationId
        collector["id"] = id
    }
}