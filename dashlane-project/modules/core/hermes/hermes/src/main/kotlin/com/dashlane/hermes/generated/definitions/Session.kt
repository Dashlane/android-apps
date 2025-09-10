package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import java.time.ZonedDateTime
import java.util.UUID
import javax.annotation.processing.Generated

@Generated
data class Session(
    val sequenceNumber: Int,
    val id: UUID,
    val serviceWorkerStartDateTime: ZonedDateTime? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["sequence_number"] = sequenceNumber
        collector["id"] = id
        collector["service_worker_start_date_time"] = serviceWorkerStartDateTime
    }
}