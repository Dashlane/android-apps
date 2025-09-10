package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import java.util.UUID
import javax.annotation.processing.Generated

@Generated
data class Flow(
    val sequenceNumber: Int,
    val id: UUID,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["sequence_number"] = sequenceNumber
        collector["id"] = id
    }
}