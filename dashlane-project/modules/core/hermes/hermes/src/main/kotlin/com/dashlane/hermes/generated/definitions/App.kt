package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class App(
    val version: String,
    val buildType: BuildType,
    val platform: Platform,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["version"] = version
        collector["build_type"] = buildType
        collector["platform"] = platform
    }
}