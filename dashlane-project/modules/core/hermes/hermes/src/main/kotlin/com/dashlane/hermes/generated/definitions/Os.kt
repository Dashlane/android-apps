package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Os(
    val type: OsType,
    val locale: String,
    val version: String,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["type"] = type
        collector["locale"] = locale
        collector["version"] = version
    }
}