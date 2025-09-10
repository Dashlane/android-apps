package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Context(
    val app: App,
    val browser: Browser? = null,
    val user: User? = null,
    val device: Device,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["app"] = app
        collector["browser"] = browser
        collector["user"] = user
        collector["device"] = device
    }
}