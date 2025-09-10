package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Domain(
    val id: Sha256Hash? = null,
    val type: DomainType,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["id"] = id
        collector["type"] = type
    }
}