package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Ios(
    val adid: String? = null,
    val idfa: String? = null,
    val advertisingId: String? = null,
    val idfv: String? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["adid"] = adid
        collector["idfa"] = idfa
        collector["advertising_id"] = advertisingId
        collector["idfv"] = idfv
    }
}