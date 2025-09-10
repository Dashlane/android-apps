package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class ContextAnonymous(
    val app: App,
    val os: Os,
    val browser: AnonymousBrowser? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["app"] = app
        collector["os"] = os
        collector["browser"] = browser
    }
}