package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import java.util.UUID
import javax.annotation.processing.Generated

@Generated
data class User(
    val id: UUID,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["id"] = id
    }
}