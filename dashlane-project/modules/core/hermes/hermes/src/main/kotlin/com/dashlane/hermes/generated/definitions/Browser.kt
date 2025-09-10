package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Browser(
    val name: String,
    val version: String? = null,
    val userAgent: String,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["version"] = version
        collector["user_agent"] = userAgent
    }
}