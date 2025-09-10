package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Android(
    val adid: String? = null,
    val advertisingId: String? = null,
    val androidId: String,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["adid"] = adid
        collector["advertising_id"] = advertisingId
        collector["android_id"] = androidId
    }
}