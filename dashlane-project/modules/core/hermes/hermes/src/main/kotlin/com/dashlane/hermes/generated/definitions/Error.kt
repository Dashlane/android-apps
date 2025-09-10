package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class Error(
    val name: ErrorName,
    val description: ErrorDescription? = null,
    val step: ErrorStep,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["description"] = description
        collector["step"] = step
    }
}